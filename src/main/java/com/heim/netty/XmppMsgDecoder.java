package com.heim.netty;

import com.heim.models.client.Iq;
import com.heim.models.client.Iqs;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.eclipse.persistence.internal.jaxb.WrappedValue;
import org.eclipse.persistence.jaxb.JAXBContextFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.List;

public class XmppMsgDecoder extends ByteToMessageDecoder {

    static Unmarshaller unmarshaller;
    static Marshaller marshaller;
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

    private Object iqHandher(String out) throws Exception {


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
                    //Bind b = (Bind) ((Iq) iq).getAny();
                    //  b.setResource(null);
                    //b.setJid("test@localhost/" + b.getResource());
//                    Iq i = new Iq();
//
//                    i.setAny(b);
//                    i.setType("result");
//                    i.setId(((Iq) iq).getId());
                    return iq;
                    //return unmarshaller().unmarshal
                    //    (new StreamSource(new StringReader(String.format(iqres, b.getResource()))
                    ////  ));
//
//                    marshaller().marshal(i, sw);
//                    String res =
//                            sw.toString()
//                                    .replaceAll(":ns0", "")
//                                    .replaceAll("ns0:", "")
//                                    .replaceAll(":ns3", "")
//                                    .replaceAll(":ns2", "")
//                                    .replaceAll(":ns1", "")
//                                    .replaceAll("ns3:", "")
//                                    .replaceAll("ns2:", "")
//                                    .replaceAll("ns1:", "");

                    //System.out.println(res);
                    //  System.out.println(iqres);
//                    ch.write(welcome(String.format(iqres, b.getResource())));
                }
                //  if(iqstr.contains("<session")){
                else if (iq.getType().equals("set") &&
                        ((WrappedValue) iq.getAny()).getName().getLocalPart().equals("session")//                    ||
//                    (iq.getAny() instanceof   org.eclipse.persistence.jaxb.generated23
//                    )
                        ) {

                    return "<iq type=\"result\" id=\"" + iq.getId() + "\"/>";
                } else if (iq.getType().equals("get")
                        && ((ElementNSImpl) iq.getAny()).getLocalName().equals("query")
                        && ((ElementNSImpl) iq.getAny()).getNamespaceURI().contains("roster")
                        ) {
                    return String.format(rooster, iq.getId());
                }
            } else {
                iqs.getAny()
                        .forEach(i ->
                                System.out.println(((Iq) i).getType())
                        );
            }

        }
        return null;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        final int messageLength = in.readableBytes();
//        if (in.readableBytes() < messageLength) {
//            return;
//        }

        byte[] ba = new byte[messageLength];
        in.readBytes(ba, 0, messageLength);  // block until read 16 bytes from sockets
        //LoopBackTimeStamp loopBackTimeStamp = new LoopBackTimeStamp();
        //loopBackTimeStamp.fromByteArray(ba);

        String str = new String(ba);
        System.out.println(str);
        if (!str.contains("<stream") && !str.contains("<iq")) {
            Object o = unmarshaller().unmarshal(new StreamSource(new StringReader(str)));
            out.add(o);
        } else {
            if (str.contains("<iq")) {
                Object val = iqHandher(str);
                if (val != null)
                    out.add(val);
            } else {
                out.add(str);
            }
        }
    }
}