package com.heim.netty;

import com.heim.models.auth.Auth;
import com.heim.models.bind.Bind;
import com.heim.models.client.*;
import com.heim.models.client.Thread;
import org.jivesoftware.smack.packet.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * User: sbenner
 * Date: 3/1/17
 * Time: 3:00 PM
 */
@Component
public class XmppStreamReader {


    private static final Logger logger = LoggerFactory.getLogger(XmppStreamReader.class);


    private static String buildXmlString(String xmlstring) {
        String xml = null;
        if ((xmlstring.startsWith("<?xml") || xmlstring.contains("<stream:stream"))
                && !xmlstring.contains("</stream:stream>")) {
            xml = xmlstring += "</stream:stream>";
        } else if (xmlstring.contains("</stream:stream>")
                && !xmlstring.contains("<stream:stream")) {
            xml = "<stream:stream>" + xmlstring;
        }

        if (!xmlstring.startsWith("<?xml"))
            xml = "<xmpp>" + xmlstring + "</xmpp>";

        return xml;
    }

    static boolean validate(String xmlstring) {
        boolean isValid = false;
        try {

            xmlstring = buildXmlString(xmlstring);

            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader =
                    factory.createXMLStreamReader(
                            new ByteArrayInputStream(xmlstring.getBytes()));


            while (reader.hasNext()) {
                reader.next();
            }
            isValid = true;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("ERRORED " + xmlstring);
        }
        return isValid;
    }

    static List<Object> read(String xmlstring) {

        List<Object> objects = new ArrayList<>();

        xmlstring = buildXmlString(xmlstring);

        System.out.println("XML READER: " + xmlstring);
//        Object obj = null;
//        Object any = null;
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
                                Stream s = new Stream();
                                objects.add(s);
                                System.out.println("we found CDR!!!");
                                for (int i = 0; i < reader.getAttributeCount(); i++) {
                                    String name = reader.getAttributeLocalName(i);
                                    String val = reader.getAttributeValue(i);
                                    switch (name) {
                                        case "to":
                                            s.setTo(val);
                                            break;
                                        case "version":
                                            s.setVersion(val);
                                            break;
                                        case "lang":
                                            s.setLang(val);
                                            break;
                                    }
                                }
                                break;
                            case "auth":
                                Auth a = new Auth();

                                for (int i = 0; i < reader.getAttributeCount(); i++) {
                                    String name = reader.getAttributeLocalName(i);
                                    String val = reader.getAttributeValue(i);
                                    switch (name) {
                                        case "mechanism":
                                            a.setMechanism(val);
                                            break;
                                    }
                                }
                                objects.add(a);
                                break;
                            case "session":
                                Optional optionalIq =
                                        objects.stream().filter(
                                                i -> i instanceof Iq
                                        ).findFirst();
                                if (optionalIq.isPresent() && ((Iq) optionalIq.get()).getType().equals("set")) {
                                    ((Iq) optionalIq.get()).setAny(new Session());
                                }
                                break;
                            case "message":
                                Message msg = new Message();
                                objects.add(msg);
                                for (int i = 0; i < reader.getAttributeCount(); i++) {
                                    String name = reader.getAttributeLocalName(i);
                                    String val = reader.getAttributeValue(i);
                                    switch (name) {
                                        case "to":
                                            msg.setTo(val);
                                            break;
                                        case "id":
                                            msg.setId(val);
                                            break;
                                        case "from":
                                            msg.setFrom(val);
                                            break;
                                        case "type":
                                            msg.setType(val);
                                            break;
                                    }
                                }
                                break;
                            case "subject":

                                optionalIq =
                                        objects.stream().filter(
                                                i -> i instanceof Message
                                        ).findFirst();
                                if (optionalIq.isPresent()) {
                                    ((Message) optionalIq.get()).getSubjectOrBodyOrThread().add(new Subject());
                                }

                                break;
                            case "body":
                                optionalIq =
                                        objects.stream().filter(
                                                i -> i instanceof Message
                                        ).findFirst();
                                if (optionalIq.isPresent()) {
                                    ((Message) optionalIq.get()).getSubjectOrBodyOrThread().add(new Body());
                                }
                                break;
                            case "thread":

                                optionalIq =
                                        objects.stream().filter(
                                                i -> i instanceof Message
                                        ).findFirst();
                                if (optionalIq.isPresent()) {
                                    ((Message) optionalIq.get()).getSubjectOrBodyOrThread().add(new Thread());
                                }
                                break;
                            case "iq":
                                Iq iq = new Iq();
                                objects.add(iq);
                                for (int i = 0; i < reader.getAttributeCount(); i++) {
                                    String name = reader.getAttributeLocalName(i);
                                    String val = reader.getAttributeValue(i);
                                    switch (name) {
                                        case "type":
                                            iq.setType(val);
                                            break;
                                        case "to":
                                            iq.setTo(val);
                                            break;
                                        case "id":
                                            iq.setId(val);
                                            break;
                                    }
                                }
                                break;
                            case "bind":
                                optionalIq =
                                        objects.stream().filter(
                                                i -> i instanceof Iq
                                        ).findFirst();
                                if (optionalIq.isPresent()) {
                                    ((Iq) optionalIq.get()).setAny(new Bind());
                                }
                                for (int i = 0; i < reader.getAttributeCount(); i++) {
                                    String name = reader.getAttributeLocalName(i);
                                    String val = reader.getAttributeValue(i);
                                    switch (name) {
                                        case "jid":
                                            Bind b = (Bind) ((Iq) optionalIq.get()).getAny();
                                            if (val != null)
                                                b.setJid(val);
                                            break;
                                    }
                                }
                                if (objects.get(0) instanceof Stream) {
                                    objects.remove(0);
                                }

                                break;
                            case "query":
                                optionalIq =
                                        objects.stream().filter(
                                                i -> i instanceof Iq
                                        ).findFirst();
                                if (optionalIq.isPresent()) {
                                    Query q = new Query();
                                    q.setNamespace(reader.getNamespaceURI());
                                    ((Iq) optionalIq.get()).setAny(q);
                                }
                                break;

                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        tagContent = reader.getText().trim();
                        break;

                    case XMLStreamConstants.END_ELEMENT:
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        System.out.println("END ELEMENT " + reader.getLocalName());
                        if (objects.size() > 0) {
                            switch (reader.getLocalName()) {
                                case "stream":
                                    break;
                                case "resource":
                                    Optional
                                            optionalIq =
                                            objects.stream().filter(
                                                    i -> i instanceof Iq
                                            ).filter(i -> ((Iq) i).getAny() instanceof Bind).findFirst();
                                    if (optionalIq.isPresent() && tagContent != null) {
                                        ((Bind) ((Iq) optionalIq.get()).getAny()).setResource(tagContent);
                                    }
                                    break;
                                case "iq":
                                    System.out.println(reader.getLocalName());
                                    break;
                                case "auth":
                                    System.out.println(tagContent);
                                    optionalIq =
                                            objects.stream().filter(
                                                    i -> i instanceof Auth
                                            ).findFirst();
                                    if (optionalIq.isPresent() && tagContent != null) {
                                        ((Auth) optionalIq.get()).setValue(tagContent);
                                    }

                                    break;
                                case "subject":
                                    final String subj = tagContent;
                                    optionalIq =
                                            objects.stream().filter(
                                                    i -> i instanceof Message
                                            ).findFirst();
                                    if (optionalIq.isPresent() && tagContent != null) {
                                        ((Message) optionalIq.get()).getSubjectOrBodyOrThread()
                                                .stream().filter(i -> i instanceof Subject).forEach(
                                                i -> ((Subject) i).setValue(subj)
                                        );
                                    }
                                    break;
                                case "body":
                                    final String body = tagContent;
                                    optionalIq =
                                            objects.stream().filter(
                                                    i -> i instanceof Message
                                            ).findFirst();
                                    if (optionalIq.isPresent()) {
                                        ((Message) optionalIq.get()).getSubjectOrBodyOrThread()
                                                .stream().filter(i -> i instanceof Body).forEach(
                                                i -> ((Body) i).setValue(body)
                                        );
                                    }
                                    break;
                                case "thread":
                                    break;
                            }
                        }
                        break;

                    case XMLStreamConstants.START_DOCUMENT:
                        break;
                    case XMLStreamConstants.ATTRIBUTE:
                        break;
                }

            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return objects;
    }
}



