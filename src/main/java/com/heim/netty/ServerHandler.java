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

public class ServerHandler extends ChannelInboundHandlerAdapter {


    //final static List<String> val = Collections.synchronizedList(new ArrayList());

    final static StringBuffer buffer = new StringBuffer();

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
            "    <item jid='serg@10.255.0.67'" +
            "          name='Sergey'" +
            "          subscription='both'>" +
            "      <group>Friends</group>" +
            "    </item>" +
            "  </query>" +
            "</iq>";
    static String message
            = "<message xmlns=\"jabber:client\" from=\"%1$s\" type=\"chat\" to=\"%2$s\" id=\"%3$s\">\n" +
            "<subject>%4$s</subject>\n" +
            "<body>%5$s</body>\n" +
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

        if (msg == null) {
            ctx.writeAndFlush("");
            return;
        }


        List<Object> objects = new ArrayList<>();
        System.out.println("INPUT CHANNEL READ: " + msg.toString());
        String xmlstring = msg.toString().trim();

        buffer.append(xmlstring);
        if (XmppStreamReader.validate(buffer.toString())) {
            objects = XmppStreamReader.read(buffer.toString());
            buffer.setLength(0);
        } else {
            return;
        }
//        if (!xmlstring.startsWith("<?xml") &&
//                !xmlstring.endsWith("</stream:stream>") &&
//                !xmlstring.startsWith("<stream:stream")
//                && !xmlstring.endsWith("/>")
//                && !xmlstring.endsWith("</iq>")
//                && !xmlstring.endsWith("</presence>")
//                && !xmlstring.endsWith("</priority>")
//                && !xmlstring.endsWith("</message>")
//                && !xmlstring.endsWith("</auth>")) {
//            val.add(xmlstring);
//        } else if (val.size() > 0) {
//            xmlstring = val.get(val.size() - 1) + xmlstring;
//            if(!xmlstring.endsWith(">")){return;}
//            else{
//                val.clear();
//            }
//        }
//        if (val.size() == 0) {
//
//        }

        SessionContext sessionContext = sessionContextMap.get(ctx.channel().id());

        for (Object obj : objects) {
            if (obj instanceof Stream) {
                if (sessionContext == null || !sessionContext.isAuthorized()) {
                    ctx.writeAndFlush(String.format(text, ((Stream) obj).getTo())
                            + features);
                    SessionContext context = new SessionContext();
                    context.setCtx(ctx);
                    context.setTo(((Stream) obj).getTo());
                    sessionContextMap.put(ctx.channel().id(), context);
                } else if (sessionContext.isAuthorized()) {
                    ctx.writeAndFlush(String.format(authOk, sessionContext.getTo()));
                    return;
                }
            }
            if (obj instanceof Auth) {

                String user = Base64Utils.decode(((Auth) obj).getValue());
                if (user != null) {
                    sessionContext.setAuthorized(true);
                    sessionContext.setUser(user);
                    sessionContext.setCtx(ctx);
                    authorizedUserChannels.put(sessionContext.getUser() + "@" + sessionContext.getTo(),
                            sessionContext.getCtx().channel().id());
                    ctx.writeAndFlush(success + String.format(authOk, sessionContext.getTo()));
                    return;
                }
                //client.put(ctx.channel().id(), true);


            }

            if (obj instanceof Message) {

                ChannelId channelId = authorizedUserChannels.get(((Message) obj).getTo());
                if (channelId != null) {
                    SessionContext userSessionContext =
                            sessionContextMap.get(channelId);


                    Optional body =
                            ((Message) obj).getSubjectOrBodyOrThread()
                                    .stream().
                                    filter(i -> i instanceof Body).findFirst();
                    Optional subj =
                            ((Message) obj).getSubjectOrBodyOrThread()
                                    .stream().
                                    filter(i -> i instanceof Subject).findFirst();

                    if (subj.isPresent() || body.isPresent()) {
                        String newMessage = String.format(message,
                                sessionContext.getUser() + "@" + sessionContext.getTo()
                                , ((Message) obj).getTo()
                                , ((Message) obj).getId(),
                                subj.isPresent() ? ((Subject) subj.get()).getValue() : "",
                                body.isPresent() ? ((Body) body.get()).getValue() : "");

                        userSessionContext.getCtx().writeAndFlush(newMessage);
                        return;
                    }
                }
                //client.put(ctx.channel().id(), true);


            }

            if (obj instanceof Iq) {
                if (((Iq) obj).getAny() == null) {
                    return;
                }
                switch (((Iq) obj).getAny().getClass().getName().toLowerCase()) {
                    case "bind":
                        break;
                    case "session":
                        break;
                    case "query":
                        break;
                }


                Iq res = (Iq) obj;
                if (((Iq) obj).getAny() instanceof Bind &&
                        ((Iq) obj).getType().equals("set")) {
                    res.setType("result");
                    Bind b = (Bind) res.getAny();
                    String user = sessionContext.getUser() + "@" + sessionContext.getTo();
                    String jid = user + "/" + b.getResource();
                    // b.setJid();

                    sessionContext.setJid(jid);

                    //StringWriter sw = new StringWriter();
//                marshaller().marshal(res, sw);
//                String r =
//                        sw.toString()
//                                .replaceAll(":ns0", "")
//                                .replaceAll("ns0:", "")
//                                .replaceAll(":ns3", "")
//                                .replaceAll(":ns2", "")
//                                .replaceAll(":ns1", "")
//                                .replaceAll("ns3:", "")
//                                .replaceAll("ns2:", "")
//                                .replaceAll("ns1:", "");

                    //  System.out.println(r);
                    ctx.writeAndFlush(String.format(bindOk, res.getId(), user, jid));
                    return;
                }
                if (((Iq) obj).getAny() instanceof Session) {
                    if (((Iq) obj).getType().equals("set")) {
                        ctx.writeAndFlush("<iq type=\"result\" id=\"" + ((Iq) obj).getId() + "\"/>");
                        return;
                    }
                }
                if (((Iq) obj).getAny() instanceof Query) {
                    Query q = (Query) ((Iq) obj).getAny();
                    if (((Iq) obj).getType().equals("get") &&
                            q.getNamespace().endsWith(":roster")) {
                        ctx.writeAndFlush(String.format(userRoster.get(sessionContext.getUser()),
                                sessionContext.getJid(),
                                sessionContext.getTo(),
                                ((Iq) obj).getId()));
                        return;
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