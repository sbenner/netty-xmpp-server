package com.heim.netty;

import com.heim.models.auth.Auth;
import com.heim.models.bind.Bind;
import com.heim.models.client.Iq;
import com.heim.models.client.Query;
import com.heim.models.client.Stream;
import org.jivesoftware.smack.packet.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: sbenner
 * Date: 3/1/17
 * Time: 3:00 PM
 */
@Component
public class XmppStreamReader {


    private static final Logger logger = LoggerFactory.getLogger(XmppStreamReader.class);


    static Object read(String xmlstring) {


        if ((xmlstring.startsWith("<?xml") || xmlstring.startsWith("<stream:stream"))
                && !xmlstring.contains("</stream:stream>")) {
            xmlstring += "</stream:stream>";
        } else if (xmlstring.contains("</stream:stream>")
                && !xmlstring.contains("<stream:stream")) {
            xmlstring = "<stream:stream>" + xmlstring;
        } else {
            xmlstring = "<xmpp>" + xmlstring + "</xmpp>";
        }

        System.out.println("XML READER: " + xmlstring);
        Object obj = null;
        Object any = null;
        String tagContent = null;

        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader =
                    factory.createXMLStreamReader(
                            new ByteArrayInputStream(xmlstring.getBytes()));


            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        System.out.println("localname: " + reader.getLocalName());
                        switch (reader.getLocalName()) {
                            case "stream":
                                obj = new Stream();
                                System.out.println("we found CDR!!!");
                                for (int i = 0; i < reader.getAttributeCount(); i++) {
                                    String name = reader.getAttributeLocalName(i);
                                    String val = reader.getAttributeValue(i);
                                    switch (name) {
                                        case "to":
                                            ((Stream) obj).setTo(val);
                                            break;
                                        case "version":
                                            ((Stream) obj).setVersion(val);
                                            break;
                                        case "lang":
                                            ((Stream) obj).setLang(val);
                                            break;
                                    }
                                }
                                break;
                            case "auth":
                                obj = new Auth();
                                for (int i = 0; i < reader.getAttributeCount(); i++) {
                                    String name = reader.getAttributeLocalName(i);
                                    String val = reader.getAttributeValue(i);
                                    switch (name) {
                                        case "mechanism":
                                            ((Auth) obj).setMechanism(val);
                                            break;
                                    }
                                }
                                break;
                            case "session":
                                if (obj instanceof Iq) {
                                    any = new Session();

                                    ((Iq) obj).setAny(any);
                                }
                                break;
                            case "iq":
                                obj = new Iq();
                                for (int i = 0; i < reader.getAttributeCount(); i++) {
                                    String name = reader.getAttributeLocalName(i);
                                    String val = reader.getAttributeValue(i);
                                    switch (name) {
                                        case "type":
                                            ((Iq) obj).setType(val);
                                            break;
                                        case "to":
                                            ((Iq) obj).setTo(val);
                                            break;
                                        case "id":
                                            ((Iq) obj).setId(val);
                                            break;
                                    }
                                }
                                break;
                            case "bind":
                                if (obj instanceof Iq) {
                                    any = new Bind();
                                    ((Iq) obj).setAny(any);
                                }
                                for (int i = 0; i < reader.getAttributeCount(); i++) {
                                    String name = reader.getAttributeLocalName(i);
                                    String val = reader.getAttributeValue(i);
                                    switch (name) {
                                        case "jid":
                                            if (any instanceof Bind && val != null)
                                                ((Bind) any).setJid(val);
                                            break;
                                    }
                                }
                                break;
                            case "query":
                                if (obj instanceof Iq) {
                                    any = new Query();
                                    ((Iq) obj).setAny(any);
                                }
                                ((Query) any).setNamespace(reader.getNamespaceURI());
//                                for (int i = 0; i < reader.getAttributeCount(); i++) {
//                                    String name = reader.getAttributeLocalName(i);
//                                    String val = reader.getAttributeValue(i);
//                                    switch (name) {
//                                        case "xmlns":
//                                            if(any instanceof Query)
//                                             ((Query)any).setNamespace(val);
//                                            break;
//                                    }
//                                }

                                break;

                        }
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        tagContent = reader.getText().trim();
                        break;

                    case XMLStreamConstants.END_ELEMENT:
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        System.out.println("END ELEMENT " + reader.getLocalName());
                        if (obj != null) {
                            switch (reader.getLocalName()) {
                                case "stream":
                                    break;
                                case "resource":
                                    if (any instanceof Bind && tagContent != null) {
                                        ((Bind) any).setResource(tagContent);
                                    }
                                    break;
                                case "iq":
                                    System.out.println(reader.getLocalName());
                                    break;
                                case "auth":
                                    if (tagContent != null) {
                                        if (obj instanceof Auth)
                                            ((Auth) obj).setValue(tagContent);
                                    }
                                    break;
//                                case "query":
//                                    if (obj instanceof Iq) {
//                                        any = new Query();
//                                        ((Iq) obj).setAny(any);
//                                    }
//                                    for (int i = 0; i < reader.getAttributeCount(); i++) {
//                                        String name = reader.getAttributeLocalName(i);
//                                        String val = reader.getAttributeValue(i);
//                                        switch (name) {
//                                            case "xmlns":
//                                                if(any instanceof Query)
//                                                    ((Query)any).setNamespace(val);
//                                                break;
//                                        }
//                                    }
//                                    break;

                            }
                        }
                        break;

                    case XMLStreamConstants.START_DOCUMENT:
                        String r = reader.getLocalName();
                        //   relatedCDRMap = new HashMap<>();
                        break;
                    case XMLStreamConstants.ATTRIBUTE:


                        break;
                }

            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return obj;
    }
}



