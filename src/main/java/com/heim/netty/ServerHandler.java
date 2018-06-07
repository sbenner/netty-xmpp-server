package com.heim.netty;

import com.heim.models.auth.Auth;
import com.heim.models.bind.Bind;
import com.heim.models.client.*;
import com.heim.models.client.Thread;
import com.heim.utils.Base64Utils;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.jivesoftware.smack.packet.Session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler extends ChannelInboundHandlerAdapter {


    //final static List<String> val = Collections.synchronizedList(new ArrayList());



    public static String features = "<stream:features>" +
            "  <mechanisms xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>" +
            "    <mechanism>PLAIN</mechanism>" +
            "  </mechanisms>" +
            "</stream:features>";
    public static String text = "<stream:stream" +
            "    xmlns='jabber:client'" +
            "    xmlns:stream='http://etherx.jabber.org/streams'" +
            "    from='%s'" +
            "    id='c2s_234'" +
            "    version='1.0'" +
            ">"; //
    public static String authOk =
            "<stream:stream xmlns='jabber:client' " +
                    "xmlns:stream='http://etherx.jabber.org/streams' " +
                    "id='c2s_345' from='%s' version='1.0'> " +
                    "<stream:features>" +
                    "<bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'/>" +
                    "<session xmlns='urn:ietf:params:xml:ns:xmpp-session'/>" +
                    "</stream:features>";

    public static String bindOk =
            "<iq type=\"result\" id=\"%1$s\" to=\"%2$s\">" +
                    " <bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\">" +
                    " <jid>%3$s</jid>" +
                    " </bind>" +
                    " </iq>";
    static String success = "<success xmlns='urn:ietf:params:xml:ns:xmpp-sasl'/>";
    static String res1 = "<stream:stream" +
            "xmlns='jabber:server'" +
            "xmlns:stream='http://etherx.jabber.org/streams'" +
            "from='%s'" +
            "id='s2s_123'" +
            "version='1.0'>";
    //jid,fromServer,id
    static String rosterTest = "<iq to='%1$s'" +
            " from='%2$s' type='result' id='%3$s'>" +
            "  <query xmlns='jabber:iq:roster'>" +
            "    <item jid='serg@%2$s'" +
            "          name='Sergey'" +
            "          subscription='both'>" +
            "      <group>Friends</group>" +
            "    </item>" +
            "  </query>" +
            "</iq>";
    static String message
            = "<message xmlns=\"jabber:client\" " +
            "from=\"%1$s\" type=\"chat\" to=\"%2$s\" id=\"%3$s\">\n" +
            "<subject>%4$s</subject>" +
            "<body>%5$s</body>" +
            "<thread>%6$s</thread>" +
            "</message>";


    static String rosterSergey = "<iq to='%1$s'" +
            " from='%2$s' type='result' id='%3$s'>" +
            "  <query xmlns='jabber:iq:roster'>" +
            "    <item jid='test@localhost'" +
            "          name='Tester'" +
            "          subscription='both'>" +
            "      <group>Friends</group>" +
            "    </item>" +
            "  </query>" +
            "</iq>";


    static Map<ChannelId, SessionContext>
            sessionContextMap = new ConcurrentHashMap<>();

    static Map<String, ChannelId>
            authorizedUserChannels = new ConcurrentHashMap<>();

    static Map<String, Chat>
            chatMap = new ConcurrentHashMap<>();

    static Map<String, String> userRoster = new HashMap<>();

    static {
        userRoster.put("serg", rosterSergey);
        userRoster.put("test", rosterTest);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ChannelFuture closeFuture = ctx.channel().closeFuture();

        closeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                sessionContextMap.remove(ctx.channel().id());
            }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg == null || msg.toString().trim().equals("</stream:stream>")) {
            ctx.writeAndFlush("");
            return;
        }
        String xmlstring = msg.toString();
        List<Object> objects = new ArrayList<>();
        System.out.println("INPUT CHANNEL READ: " + msg.toString());

        SessionContext sessionContext = sessionContextMap.get(ctx.channel().id());
        StringBuilder buffer = null;
        if (sessionContext == null) {
            sessionContext = new SessionContext();
            sessionContext.setPacketBuffer(new StringBuilder());
            sessionContextMap.put(ctx.channel().id(), sessionContext);
        }
        buffer = sessionContext.getPacketBuffer();


        if (xmlstring.contains("auth"))//we cleanup the messy stuff
        {
            buffer.setLength(0);
        }

        if (xmlstring.startsWith("xmlns=") ||
                xmlstring.startsWith("type=") ||
                xmlstring.startsWith("id=") ||
                xmlstring.startsWith("ver=") ||
                xmlstring.startsWith("ext=") ||
                xmlstring.startsWith("node=") ||
                xmlstring.startsWith("version=") ||
                xmlstring.startsWith("to=") ||
                xmlstring.startsWith("from=")) {
            buffer.append(" ");
        }
        buffer.append(xmlstring.replaceAll("[\n\t]", " "));

        if (XmppStreamReader.validate(buffer.toString())) {
            objects = XmppStreamReader.read(buffer.toString());
            buffer.setLength(0);
        } else {
            if (buffer.toString().contains("auth"))//we cleanup the messy stuff
            {
                buffer.setLength(0);
            }
            return;
        }


        System.out.println("Objects SIZE " + objects.size());

        //  objects.forEach(System.out::println);
        //check if user is online
        //cleanup map with channels if offlin
        //store not sent messages into a queue then clean it up dump

        for (Object obj : objects) {

            System.out.println("object: " + obj.toString());

            if (obj instanceof Stream) {
                if (!sessionContext.isAuthorized()) {
                    ctx.writeAndFlush(String.format(text, ((Stream) obj).getTo())
                            + features);
                    sessionContext.setCtx(ctx);
                    sessionContext.setTo(((Stream) obj).getTo());
                } else if (sessionContext.isAuthorized()) {
                    ctx.writeAndFlush(String.format(authOk, sessionContext.getTo()));
                    System.out.println("mek1");
                    return;
                }
            }
            if (obj instanceof Auth) {

                String user = Base64Utils.decode(((Auth) obj).getValue());
                if (user != null) {
                    sessionContext.setAuthorized(true);
                    sessionContext.setUser(user);
                    authorizedUserChannels.put(sessionContext.getUser() + "@" + sessionContext.getTo(),
                            sessionContext.getCtx().channel().id());
                    ctx.writeAndFlush(success + String.format(authOk, sessionContext.getTo()));
                    System.out.println("mek2");
                    return;
                }


            }
            if (obj instanceof Presence) {
                Presence p = (Presence) obj;
                if (p.getType() != null && p.getType().equals("unavailable")) {
                    SessionContext c = sessionContextMap.remove(ctx.channel().id());
                    authorizedUserChannels.remove(c.getUser() + "@" + c.getTo());
                    ctx.close();
                }
            }

            if (obj instanceof Message) {

                Message incMessge = (Message) obj;

                Optional thread =
                        ((Message) obj).getSubjectOrBodyOrThread()
                                .stream().
                                filter(i -> i instanceof Thread).findFirst();

                thread.ifPresent(i -> {
                    String threadId = ((Thread) i).getValue();
                    Chat c = chatMap.get(threadId);
                    if (c == null) {
                        c = new Chat();
                        c.setThreadId(((Thread) i).getValue());
                        Set<String> peers = new HashSet<>();
                        peers.add(incMessge.getTo());
                        peers.add(incMessge.getFrom());
                        c.setPeers(peers);
                        chatMap.put(threadId, c);

                    } else {
                        Set<String> peers = c.getPeers();
                        peers.add(incMessge.getTo());
                        peers.add(incMessge.getFrom());
                        c.setPeers(peers);
                        chatMap.put(threadId, c);
                    }
                });

                ChannelId channelId = authorizedUserChannels.get(((Message) obj).getTo());
                System.out.println(channelId);
                if (channelId != null) {
                    SessionContext userSessionContext = sessionContextMap.get(channelId);

                    if (userSessionContext == null) {
                        sessionContextMap.remove(channelId);
                    }

                    if (userSessionContext != null && userSessionContext.getCtx() != null) {


                        System.out.println(userSessionContext.toString());


                        Optional body =
                                ((Message) obj).getSubjectOrBodyOrThread()
                                        .stream().
                                        filter(i -> i instanceof Body).findFirst();
                        Optional subj =
                                ((Message) obj).getSubjectOrBodyOrThread()
                                        .stream().
                                        filter(i -> i instanceof Subject).findFirst();


                        System.out.println("body present: " + body.isPresent());
                        System.out.println("subj:" + subj.isPresent());

                        if (subj.isPresent() || body.isPresent()) {


                            if (sessionContext.isAuthorized()) {
                                String newMessage = String.format(message,
                                        sessionContext.getUser() + "@" + sessionContext.getTo()
                                        , ((Message) obj).getTo()
                                        , ((Message) obj).getId(),
                                        subj.isPresent() ? ((Subject) subj.get()).getValue() : "",
                                        body.isPresent() ?
                                                ((Body) body.get()).getValue() : "",
                                        thread.isPresent() ?
                                                ((Thread) thread.get()).getValue() : "");

                                System.out.println("NEW MESSAGE:\n " + newMessage);

                                if (userSessionContext.getCtx().channel().isWritable()) {
                                    userSessionContext.getCtx().writeAndFlush(newMessage);
                                } else {
                                    sessionContextMap.remove(channelId);
                                    //store to be sent
                                }
                            } else {

                                //addto queue

                            }
                            // return;
                        }
                    }
                }
                //client.put(ctx.channel().id(), true);


            }

            if (obj instanceof Iq) {

                Iq res = (Iq) obj;
                if ((((Iq) obj).getAny() != null) && ((Iq) obj).getAny() instanceof Bind &&
                        ((Iq) obj).getType().equals("set")) {
                    res.setType("result");
                    Bind b = (Bind) res.getAny();
                    String user = sessionContext.getUser() + "@" + sessionContext.getTo();
                    String jid = user + "/" + b.getResource();
                    // b.setJid();

                    sessionContext.setJid(jid);

                    ctx.writeAndFlush(String.format(bindOk, res.getId(), user, jid));

                }
                if ((((Iq) obj).getAny() != null) && ((Iq) obj).getAny() instanceof Session) {
                    if (((Iq) obj).getType().equals("set")) {
                        ctx.writeAndFlush("<iq type=\"result\" id=\"" + ((Iq) obj).getId() + "\"/>");

                    }
                }
                if ((((Iq) obj).getAny() != null) && ((Iq) obj).getAny() instanceof Query) {
                    Query q = (Query) ((Iq) obj).getAny();
                    if (((Iq) obj).getType().equals("get") &&
                            q.getNamespace().endsWith(":roster")) {
                        ctx.writeAndFlush(String.format(
                                userRoster.get(sessionContext.getUser()),
                                sessionContext.getJid(),
                                sessionContext.getTo(),
                                ((Iq) obj).getId(),
                                sessionContext.getTo()));

                    }
                }

            }
        }

    }


    // Here is how we send out heart beat for idle to long
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.ALL_IDLE) { // idle for no read and write
                //  ctx.writeAndFlush(new LoopBackTimeStamp());
                ctx.channel().close();
            }

        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}