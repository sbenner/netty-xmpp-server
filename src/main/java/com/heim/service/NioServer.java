package com.heim.service;


import com.heim.models.auth.Auth;
import com.heim.models.bind.Bind;
import com.heim.models.client.Iq;
import com.heim.models.client.Iqs;
import com.heim.utils.Base64Utils;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


    public static void main(String[] args) throws IOException {
        NioServer server = new NioServer(5222);
        (new Thread(server)).start();
    }

    static Unmarshaller unmarshaller;
    static Marshaller marshaller;
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


    static String rooster = "<iq to='camel_producer@localhost/Psi' from='localhost' type='result' id='%s'>" +
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


    private static Marshaller marshaller() throws Exception {
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

            ByteBuffer body = ByteBuffer.allocate(8092);

            while ((read = ch.read(body)) > 0) {
             //   logger.info("read bytes : " + read);

                body.flip();
                byte chunk[] = new byte[read];
                body.get(chunk);
                String out = new String(body.array()).trim()
                        .replaceAll("xmlns=\"jabber:client\"", "");
                System.out.println(out);


//                if (!StringUtils.isEmpty(addy)) {
//                    String val = sessionsState.get(addy);
//
//                    if (!StringUtils.isEmpty(val)) {
//                        ch.write(welcome(val));
//                        //     return;
//                    }
//                }

                if (out.contains("<auth")) {
                    String authStr = out.substring(0, out.indexOf("</auth>") + 7);
                    Auth auth = (Auth) unmarshaller().unmarshal(new StreamSource(new StringReader(authStr)));
                    System.out.println(auth);
                    Base64Utils.decode(auth.getValue());
                    ch.write(welcome(success));
                    ch.write(welcome(finish));
                    sessionsState.put(makeAddress(ch), finish);
                }
                if (out.contains("<presence")) {
                    String presense = out.substring(0, out.indexOf("</presence>") + 11);
                    Object presence = unmarshaller().unmarshal(new StreamSource(new StringReader(presense)));
                }

                iqHandher(ch, out);

            }

            // logger.info(messageMap);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    private void iqHandher(SocketChannel ch, String out) throws Exception {


        if (out.contains("<iq")) {

            String iqstr = out.substring(out.indexOf("<iq"),
                    out.length()).replaceAll("xmlns=\"jabber:client\"", "");

            iqstr = "<iqs>" + iqstr + "</iqs>";

            Iqs iqs = (Iqs) unmarshaller()
                    .unmarshal
                            (new StreamSource(new StringReader(iqstr)));

            Iq iq = null;
            if (iqs.getAny().size() == 1) {
                iq = (Iq) iqs.getAny().get(0);

                if (iq != null && iqstr.contains("<bind")) {

                    System.out.println(iq);
                    Bind b = (Bind) ((Iq) iq).getAny();
                    //  b.setResource(null);
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
                    //System.out.println(res);
                    System.out.println(iqres);
                    ch.write(welcome(String.format(iqres, b.getResource())));
                }
                //  if(iqstr.contains("<session")){
                else if (iq.getType().equals("set") && (iq.getAny() instanceof ElementNSImpl
                        && ((ElementNSImpl) iq.getAny()).getLocalName().equals("session"))
//                    ||
//                    (iq.getAny() instanceof   org.eclipse.persistence.jaxb.generated23
//                    )
                        ) {
                    ch.write(welcome("<iq type=\"result\" id=\"" + iq.getId() + "\"/>"));
                } else if (iq.getType().equals("get") && ((ElementNSImpl) iq.getAny()).getLocalName().equals("query")) {
                    ch.write(welcome(String.format(rooster, iq.getId())));
                }
            }

        }
    }

}