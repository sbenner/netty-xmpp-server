package com.heim.netty;

import com.heim.models.auth.Auth;
import com.heim.models.bind.Bind;
import com.heim.models.client.Iq;
import com.heim.utils.Base64Utils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.eclipse.persistence.jaxb.JAXBContextFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerHandler extends ChannelInboundHandlerAdapter {


    public static String text = "<stream:stream" +
            "    xmlns='jabber:client'" +
            "    xmlns:stream='http://etherx.jabber.org/streams'" +
            "    from='localhost'" +
            "    id='c2s_234'" +
            "    version='1.0'" +
            ">"; //

    public static String features = "<stream:features>" +
            "  <mechanisms xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>" +
            "    <mechanism>PLAIN</mechanism>" +
            "  </mechanisms>" +
            "</stream:features>";
    public static String finish =
            "<stream:stream xmlns='jabber:client' " +
                    "xmlns:stream='http://etherx.jabber.org/streams' " +
                    "id='c2s_345' from='localhost' version='1.0'> " +
                    "<stream:features>" +
                    "<bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'/>" +
                    "<session xmlns='urn:ietf:params:xml:ns:xmpp-session'/>" +
                    "</stream:features>";
    public static String iqres =
            "<iq type=\"result\" id=\"bind_1\" to=\"localhost\">" +
                    " <bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\">" +
                    " <jid>camel_producer@localhost/%s</jid>" +
                    " </bind>" +
                    " </iq>";
    static String res1 = "<stream:stream" +
            "xmlns='jabber:server'" +
            "xmlns:stream='http://etherx.jabber.org/streams'" +
            "from='localhost'" +
            "id='s2s_123'" +
            "version='1.0'>";
    static Unmarshaller unmarshaller;
    static Marshaller marshaller;
    static String success = "<success xmlns='urn:ietf:params:xml:ns:xmpp-sasl'/>";
    static Map<ChannelId, Boolean> client =
            new ConcurrentHashMap<>();

    public static Unmarshaller unmarshaller() throws Exception {
        if (unmarshaller == null) {
            JAXBContext jc = JAXBContextFactory
                    .createContext("com.heim.models.auth:com.heim.models.bind:com.heim.models.client:com.heim.models.xmpp_stanzas", null, null);
            unmarshaller = jc.createUnmarshaller();

        }

        return unmarshaller;
    }

    public static Marshaller marshaller() throws Exception {
        if (marshaller == null) {
            JAXBContext jc = JAXBContextFactory
                    .createContext("com.heim.models.auth:com.heim.models.bind:com.heim.models.client:com.heim.models.xmpp_stanzas", null, null);
            marshaller = jc.createMarshaller();

        }

        return marshaller;
    }

    private ByteBuffer welcome(String t) {
        return ByteBuffer.wrap(t.getBytes());
    }

    private String makeAddress(SocketChannel sc) {
        return (new StringBuilder(sc.socket().getInetAddress().toString()))
                .append(":").append(sc.socket().getPort()).toString();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg == null) {
            ctx.writeAndFlush("");
            return;
        }


        Boolean auth = client.get(ctx.channel().id());

        if (msg.toString().contains("<stream") && !msg.toString().contains("<iq")) {
            if (auth == null || !auth)
                ctx.writeAndFlush(text + features);
        }

        System.out.println(msg);
        if ((auth != null && auth) && msg.toString().contains("<iq")
                && msg.toString().contains("result") ||
                msg.toString().contains("rooster")) {
            ctx.writeAndFlush(msg);
        }


        if (msg instanceof Auth) {

            Base64Utils.decode(((Auth) msg).getValue());
            ctx.writeAndFlush(success + finish);
            client.put(ctx.channel().id(), true);
            // ctx.writeAndFlush(finish);
            // sessionsState.put(makeAddress(ch), finish);

        }
        if (msg instanceof Iq) {
            Iq res = (Iq) msg;
            res.setType("result");
            Bind b = (Bind) ((Iq) res).getAny();

            b.setJid("test@localhost/" + b.getResource());

            StringWriter sw = new StringWriter();
            marshaller().marshal(res, sw);
            String r =
                    sw.toString()
                            .replaceAll(":ns0", "")
                            .replaceAll("ns0:", "")
                            .replaceAll(":ns3", "")
                            .replaceAll(":ns2", "")
                            .replaceAll(":ns1", "")
                            .replaceAll("ns3:", "")
                            .replaceAll("ns2:", "")
                            .replaceAll("ns1:", "");


            ctx.writeAndFlush(String.format(iqres, b.getJid()));
        }
//     else {
//         ctx.writeAndFlush(msg);
//     }

        //LoopBackTimeStamp ts = (LoopBackTimeStamp) msg;
        //ts.setRecvTimeStamp(System.nanoTime());
        //System.out.println("loop delay in ms : " + 1.0 * ts.timeLapseInNanoSecond() / 1000000L);
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