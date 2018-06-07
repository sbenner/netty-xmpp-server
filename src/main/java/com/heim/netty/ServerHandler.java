package com.heim.netty;

import com.heim.models.auth.Auth;
import com.heim.models.bind.Bind;
import com.heim.models.client.*;
import com.heim.utils.Base64Utils;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.jivesoftware.smack.packet.Session;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    static Queue<Message> messageQueue = new LinkedBlockingQueue();
    ThreadPoolExecutor executor = new ThreadPoolExecutor(20, 150,
            5, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>());


    public ServerHandler() {
        new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    Thread.sleep(100);
                    Message m = messageQueue.poll();
                    if (m != null)
                        executor.execute(new MessageHandler(m));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ).start();

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ChannelFuture closeFuture = ctx.channel().closeFuture();

        closeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                SessionContext c = sessionContextMap.remove(ctx.channel().id());
                authorizedUserChannels.remove(c.getUser() + "@" + c.getTo());
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
            objects = XmppStreamReader.read(buffer.toString(),
                    ctx.channel().id());
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

                    ctx.writeAndFlush(success + String.format(authOk, sessionContext.getTo()));
                    System.out.println("mek2");
                    //TODO:
                    //Handle messages from queue
                    //Compare channelId,
                    //Set the from values to messages from the channel and send them
                    //Don;t keep messages forever in queue
                    //Create  message queue validation thread

                    return;
                }


            }
            if (obj instanceof Presence) {
                Presence p = (Presence) obj;
                if (p.getType() != null && p.getType().equals("unavailable")) {
                    //SessionContext c = sessionContextMap.remove(ctx.channel().id());
                    //authorizedUserChannels.remove(c.getUser() + "@" + c.getTo());
                    ctx.close();
                }
            }

            if (obj instanceof Message) {
                messageQueue.add((Message) obj);
                //handleMessage(sessionContext, (Message) obj);
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
                    authorizedUserChannels.put(user,
                            sessionContext.getCtx().channel().id());

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

    private void handleMessage(Message obj) {

        Message incMessge = obj;
        Optional thread =
                obj.getSubjectOrBodyOrThread()
                        .stream().
                        filter(i -> i instanceof ChatThread).findFirst();

        thread.ifPresent(i -> {
            String threadId = ((ChatThread) i).getValue();
            Chat c = chatMap.get(threadId);
            if (c == null) {
                c = new Chat();
                c.setThreadId(((ChatThread) i).getValue());
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


        ChannelId channelId = obj.getChannelId();
        SessionContext userFromSessionContext = sessionContextMap.get(channelId);
        ChannelId channelToId =
                authorizedUserChannels.get(obj.getTo());
        SessionContext userToSessionContext = null;
        if (channelToId != null)
            userToSessionContext =
                    sessionContextMap.get(channelToId);

//            if (userSessionContext == null) {
//                sessionContextMap.remove(channelId);
//            }

        if (userFromSessionContext != null
                && userFromSessionContext.getCtx() != null) {


            System.out.println(userFromSessionContext.toString());


            Optional body =
                    obj.getSubjectOrBodyOrThread()
                            .stream().
                            filter(i -> i instanceof Body).findFirst();
            Optional subj =
                    obj.getSubjectOrBodyOrThread()
                            .stream().
                            filter(i -> i instanceof Subject).findFirst();


            System.out.println("body present: " + body.isPresent());
            System.out.println("subj:" + subj.isPresent());

            if (subj.isPresent() || body.isPresent()) {


                if (userFromSessionContext.isAuthorized()
                        && userToSessionContext != null) {
                    //we get channelId of a sender
                    //
                    String sendFromUser =
                            userFromSessionContext.getUser()
                                    + "@" +
                                    userFromSessionContext.getTo();

                    String sendToUser = userToSessionContext.getUser() + "@" +
                            userToSessionContext.getTo();

                    if (userToSessionContext.getCtx().channel().isWritable()
                            &&
                            sendToUser.equals(obj.getTo())) {
                        String newMessage = String.format(
                                message,
                                sendFromUser
                                , obj.getTo()
                                , obj.getId(),
                                subj.isPresent() ? ((Subject) subj.get()).getValue() : "",
                                body.isPresent() ?
                                        ((Body) body.get()).getValue() : "",
                                thread.isPresent() ?
                                        ((ChatThread) thread.get()).getValue() : "");

                        System.out.println("NEW MESSAGE:\n " + newMessage);
                        userToSessionContext.getCtx().writeAndFlush(newMessage);
                        messageQueue.remove(obj);
                    } else {
                        sessionContextMap.remove(userToSessionContext.getCtx().channel().id());
                        authorizedUserChannels.remove(sendToUser);
                        //store to be sent
                    }
                }
                // return;
            }
        }
        //   }

    }

    class MessageHandler implements Runnable {

        private Message message;

        MessageHandler(Message message) {
            this.message = message;

        }

        @Override
        public void run() {
            handleMessage(message);
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