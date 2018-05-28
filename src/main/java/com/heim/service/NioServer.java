package com.heim.service;


import com.heim.models.auth.Auth;
import com.heim.models.bind.Bind;
import com.heim.models.client.Iq;
import com.heim.utils.Base64Utils;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NioServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(NioServer.class);
    static String auth = "<iq type='result' id='auth_1'>" +
            "<query xmlns='jabber:iq:auth'>" +
            "<username/>" +
            "<password/>" +
            "<resource/>" +
            "</query>" +
            "</iq>";


    static String text = "<stream:stream" +
            "    xmlns='jabber:client'" +
            "    xmlns:stream='http://etherx.jabber.org/streams'" +
            "    from='localhost'" +
            "    id='c2s_234'" +
            "    version='1.0'" +
            ">"; //

    static String features = "<stream:features>" +
            "  <mechanisms xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>" +
            "    <mechanism>PLAIN</mechanism>" +
            "  </mechanisms>" +
            "</stream:features>";
    static String res1 = "<stream:stream" +
            "xmlns='jabber:server'" +
            "xmlns:stream='http://etherx.jabber.org/streams'" +
            "from='localhost'" +
            "id='s2s_123'" +
            "version='1.0'>";
    private final int port;
    private final ByteBuffer welcomeBuf = ByteBuffer.wrap("Welcome to MsgServer!\n".getBytes());
    private volatile byte[] headerChunk = null;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    //   Customer customer = (Customer) jaxbUnmarshaller.unmarshal(file);

    public NioServer(int port) throws IOException {
        this.port = port;
        this.serverSocketChannel = ServerSocketChannel.open();

        // this.serverSocketChannel.setOption(ChannelOption.TCP_NODELAY,true);
        this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(false);
        this.selector = Selector.open();
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    static Unmarshaller unmarshaller;
    static Marshaller marshaller;
    static String finish =
            "<stream:stream xmlns='jabber:client' " +
                    "xmlns:stream='http://etherx.jabber.org/streams' " +
                    "id='c2s_345' from='localhost' version='1.0'> " +
                    "<stream:features>" +
            "<bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'/>" +
            "<session xmlns='urn:ietf:params:xml:ns:xmpp-session'/>" +
            "</stream:features>";
    static String iqres =
            "<iq type='result' id='bind_1'>" +
                    "<bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'>" +
                    "<jid>test@localhost/frosty</jid>" +
                    "</bind>" +
                    "</iq>";


    public static void main(String[] args) throws IOException {
        NioServer server = new NioServer(5222);
        (new Thread(server)).start();
    }

    public static Unmarshaller unmarshaller() throws Exception {
        if (unmarshaller == null) {
            JAXBContext jc = JAXBContextFactory
                    .createContext("com.heim.models.auth:com.heim.models.bind:com.heim.models.client:com.heim.models.xmpp_stanzas", null, null);
            unmarshaller = jc.createUnmarshaller();

        }

        return unmarshaller;
    }

    public void run() {
        try {
            logger.info("Server starting on port " + this.port);

            Iterator<SelectionKey> iter;
            SelectionKey key;
            while (this.serverSocketChannel.isOpen()) {
                selector.select();
                iter = this.selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    key = iter.next();
                    iter.remove();
                    if (key.isAcceptable()) {
                        this.handleAccept(key);
                    }
                    if (key.isReadable()) {
                        this.handleRead(key);
                    }

                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    static String error ="<failure xmlns='urn:ietf:params:xml:ns:xmpp-sasl'>" +
            "<invalid-authzid/>" +
            "</failure>";
    static String success="<success xmlns='urn:ietf:params:xml:ns:xmpp-sasl'/>";

    static String ok="<stream:stream" +
            "     from='localhost'" +
            "     id='gPybzaOzBmaADgxKXu9UClbprp0='" +
            "     to='localhost'" +
            "     version='1.0'" +
            "     xml:lang='en'" +
            "     xmlns='jabber:client'" +
            "     xmlns:stream='http://etherx.jabber.org/streams'>";
    Map<String, String> sessionsState = new ConcurrentHashMap<>();

//    static String features = "<stream:features>" +
//            "<bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'/>" +
//            "</stream:features>";



    private ByteBuffer welcome(String t) {
        return ByteBuffer.wrap(t.getBytes());
    }

    private String makeAddress(SocketChannel sc) {
        return (new StringBuilder(sc.socket().getInetAddress().toString()))
                .append(":").append(sc.socket().getPort()).toString();
    }


    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
        String address = makeAddress(sc);
        sc.setOption(StandardSocketOptions.TCP_NODELAY, true);

        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ, address);
        sc.write(welcome(text));
        //   sc.write(welcome(auth));
        sc.write(welcome(features));
        sessionsState.put(address, "");
        //  welcomeBuf.rewind();
        logger.info("accepted connection from: " + address);

    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel ch = (SocketChannel) key.channel();

        readHeader(ch);

    }

    public static Marshaller marshaller() throws Exception {
        if (marshaller == null) {
            JAXBContext jc = JAXBContextFactory
                    .createContext("com.heim.models.auth:com.heim.models.bind:com.heim.models.client:com.heim.models.xmpp_stanzas", null, null);
            marshaller = jc.createMarshaller();

        }

        return marshaller;
    }

    private void readHeader(SocketChannel ch) throws IOException {
        int read;
        String addy = makeAddress(ch);

        try {

            ByteBuffer body = ByteBuffer.allocate(512);

            while ((read = ch.read(body)) > 0) {
             //   logger.info("read bytes : " + read);

                body.flip();
                byte chunk[] = new byte[read];
                body.get(chunk);
                String out =   new String(body.array()).trim();
                System.out.println(out);

                if (!StringUtils.isEmpty(addy)) {
                    String val = sessionsState.get(addy);
                    if (!StringUtils.isEmpty(val)) {
                        ch.write(welcome(val));
                        //     return;
                    }
                }

                if (out.startsWith("<iq")) {

                    Object iq = (Iq) unmarshaller()
                            .unmarshal
                                    (new StreamSource(new StringReader(out)));
                    System.out.println(iq);
                    Bind b = (Bind) ((Iq) iq).getAny();
                    b.setResource(null);
                    b.setJid("test@localhost/" + b.getResource());
                    Iq i = new Iq();

                    i.setAny(b);
                    i.setType("result");
                    i.setId(((Iq) iq).getId());
                    StringWriter sw = new StringWriter();
                    marshaller().marshal(i, sw);
                    String res =
                            sw.toString()
                                    .replaceAll(":ns0", "")
                                    .replaceAll("ns0:", "")
                                    .replaceAll(":ns3", "")
                                    .replaceAll(":ns2", "")
                                    .replaceAll(":ns1", "")
                                    .replaceAll("ns3:", "")
                                    .replaceAll("ns2:", "")
                                    .replaceAll("ns1:", "");
                    System.out.println(res);
                    System.out.println(iqres);
                    ch.write(welcome(iqres));
//                    Object bind = (Bind) unmarshaller()
//                            .unmarshal(new StreamSource(new StringReader(out)));
//                    System.out.println(bind);
//<iq type='result' id='bind_1'>
//<bind xmlns='urn:ietf:params:xml:ns:xmpp-bind'>
//<jid>test@localhost/someresource</jid>
//</bind>
//</iq>
                }


                if (out.startsWith("<auth")) {
                    Object auth = unmarshaller().unmarshal(new StreamSource(new StringReader(out)));
                    System.out.println(auth);
                    Base64Utils.decode(((Auth) auth).getValue());
                    ch.write(welcome(success));
                    sessionsState.put(makeAddress(ch), finish);
                }
            }

            // logger.info(messageMap);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

}