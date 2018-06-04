package com.heim.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.springframework.util.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.util.ArrayList;
import java.util.List;

public class XmppMsgDecoder extends ByteToMessageDecoder {

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

    public static Marshaller marshaller() throws Exception {
        if (marshaller == null) {
            JAXBContext jc = JAXBContextFactory
                    .createContext("com.heim.models.auth:com.heim.models.bind:com.heim.models.client:com.heim.models.xmpp_stanzas", null, null);
            marshaller = jc.createMarshaller();

        }

        return marshaller;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int messageLength = -1;
//        if (in.readableBytes() < messageLength) {
//            return;
//        }

        List<Object> b = new ArrayList<>();
        while ((messageLength = in.readableBytes()) > 0) {
            byte[] ba = new byte[messageLength];
            in.readBytes(ba, 0, messageLength);  // block until read 16 bytes from sockets
            //LoopBackTimeStamp loopBackTimeStamp = new LoopBackTimeStamp();
            //loopBackTimeStamp.fromByteArray(ba);

            String str = new String(ba);
            str = str.trim();
            System.out.println(str);
            if (!StringUtils.isEmpty(str))
                b.add(str);
        }

//        byte[] ba = new byte[messageLength];
//        in.readBytes(ba, 0, messageLength);  // block until read 16 bytes from sockets
//        //LoopBackTimeStamp loopBackTimeStamp = new LoopBackTimeStamp();
//        //loopBackTimeStamp.fromByteArray(ba);
//
//        String str = new String(ba);
//        str=str.trim();
//        System.out.println(str);
//        if(!StringUtils.isEmpty(str))
        if (b.size() > 0)
            out.addAll(b);

    }
}