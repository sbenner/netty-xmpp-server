package com.heim.netty;

import com.heim.models.auth.Auth;
import com.heim.models.bind.Bind;
import com.heim.models.client.Iq;
import com.heim.models.client.Query;
import com.heim.models.client.Stream;
import com.heim.utils.Base64Utils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.jivesoftware.smack.packet.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler extends ChannelInboundHandlerAdapter {


    final static List<String> val = Collections.synchronizedList(new ArrayList());

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
    static String roster = "<iq to='%1$s'" +
            " from='%2$s' type='result' id='%3$s'>" +
            "  <query xmlns='jabber:iq:roster'>" +
            "    <item jid='123456789@icq.example.com'" +
            "          name='Romeo'" +
            "          subscription='both'>" +
            "      <group>Friends</group>" +
            "    </item>" +
            "    <item jid='554323654@icq.example.com'" +
            "          name='Mercutio'" +
            "          subscription='from'>" +
            "      <group>Friends</group>" +
            "    </item>" +
            "    <item jid='997665667@icq.example.com'" +
            "          name='Benvolio'" +
            "          subscription='both'>" +
            "      <group>Friends</group>" +
            "    </item>" +
            "  </query>" +
            "</iq>";
    static Map<ChannelId, SessionContext>
            sessionContextMap = new ConcurrentHashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg == null) {
            ctx.writeAndFlush("");
            return;
        }

        System.out.println("INPUT CHANNEL READ: " + msg.toString());
        String xmlstring = msg.toString();
        if (!xmlstring.startsWith("<?xml") &&
                !xmlstring.endsWith("</stream:stream>") &&
                !xmlstring.startsWith("<stream:stream")
                && !xmlstring.endsWith("/>")
                && !xmlstring.endsWith("</iq>")
                && !xmlstring.endsWith("</presence>")
                && !xmlstring.endsWith("</priority>")
                && !xmlstring.endsWith("</auth>")) {
            val.add(xmlstring);
        } else if (val.size() > 0) {
            xmlstring = val.get(val.size() - 1) + xmlstring;
            val.clear();
        }
        if (val.size() == 0) {
            msg = XmppStreamReader.read(xmlstring);
        }

        SessionContext sessionContext = sessionContextMap.get(ctx.channel().id());

        if (msg instanceof Stream) {
            if (sessionContext == null || !sessionContext.isAuthorized()) {
                ctx.writeAndFlush(String.format(text, ((Stream) msg).getTo())
                        + features);
                SessionContext context = new SessionContext();
                context.setCtx(ctx);
                context.setTo(((Stream) msg).getTo());
                sessionContextMap.put(ctx.channel().id(), context);
            } else {
                ctx.writeAndFlush(String.format(authOk, sessionContext.getTo()));
            }
        }
        if (msg instanceof Auth) {

            String user = Base64Utils.decode(((Auth) msg).getValue());
            if (user != null) {
                sessionContext.setAuthorized(true);
                sessionContext.setUser(user);
                ctx.writeAndFlush(success + String.format(authOk, sessionContext.getTo()));

            }
            //client.put(ctx.channel().id(), true);


        }


        if (msg instanceof Iq) {
            if (((Iq) msg).getAny() == null) {
                return;
            }
            switch (((Iq) msg).getAny().getClass().getName().toLowerCase()) {
                case "bind":
                    break;
                case "session":
                    break;
                case "query":
                    break;
            }


            Iq res = (Iq) msg;
            if (((Iq) msg).getAny() instanceof Bind &&
                    ((Iq) msg).getType().equals("set")) {
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
            }
            if (((Iq) msg).getAny() instanceof Session) {
                if (((Iq) msg).getType().equals("set")) {
                    ctx.writeAndFlush("<iq type=\"result\" id=\"" + ((Iq) msg).getId() + "\"/>");
                }
            }
            if (((Iq) msg).getAny() instanceof Query) {
                Query q = (Query) ((Iq) msg).getAny();
                if (((Iq) msg).getType().equals("get") &&
                        q.getNamespace().endsWith(":roster"))
                    ctx.writeAndFlush(String.format(roster,
                            sessionContext.getJid(), sessionContext.getTo(), ((Iq) msg).getId()));
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