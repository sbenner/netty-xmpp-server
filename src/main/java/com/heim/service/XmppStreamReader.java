package com.heim.service;

import com.heim.models.auth.Auth;
import com.heim.models.bind.Bind;
import com.heim.models.client.Iq;
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


    public static Object read(String xmlstring) {

        if (xmlstring.contains("<?xml") && !xmlstring.contains("</stream:stream>")) {
            xmlstring += "</stream:stream>";
        } else if (xmlstring.contains("</stream:stream>") && !xmlstring.contains("<stream:stream")) {
            xmlstring = "<stream:stream>" + xmlstring;
        }


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
                                obj = new Session();
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
                                            if (any != null && val != null)
                                                ((Bind) any).setJid(val);
                                            break;
                                    }
                                }
                                break;


                        }
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        tagContent = reader.getText().trim();
                        break;

                    case XMLStreamConstants.END_ELEMENT:
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

//                                    if (!StringUtils.isEmpty(cdr.getUserNumber())) {
//                                        if (cdr.getUserNumber().equals(cdr.getCallingNumber())) {
//                                            cdr.setCallDirection("OUTBOUND");
//                                        }
//                                        if (cdr.getUserNumber().equals(cdr.getCalledNumber())) {
//                                            cdr.setCallDirection("INBOUND");
//                                        }
//                                    }
//                                    callHistoryService.save(cdr);
                                    //    System.out.println(cdr.toString());
                                    //saveToDatabase()

                                    break;


                                case "auth":
                                    if (tagContent != null) {
                                        ((Auth) obj).setValue(tagContent);
                                    }
                                    break;


//                                case "calledNumber":
//                                    if (!StringUtils.isEmpty(tagContent))
//                                        cdr.setCalledNumber(tagContent);
//                                    break;
//                                case "userNumber":
//                                    if (!StringUtils.isEmpty(tagContent))
//                                        cdr.setUserNumber(tagContent);
//                                    break;
//                                case "userId":
//                                    if (!StringUtils.isEmpty(tagContent))
//                                        cdr.setUserSip(tagContent);
//                                    break;
//                                case "callingNumber":
//                                    if (!StringUtils.isEmpty(tagContent))
//                                        cdr.setCallingNumber(tagContent);
//                                    break;
//                                case "direction":
//                                    if (!StringUtils.isEmpty(tagContent))
//                                        cdr.setCallDirection(tagContent);
//                                    break;
//                                case "startTime":
//                                    if (!StringUtils.isEmpty(tagContent))
//                                        cdr.setStartTime(cdr.formatDate(tagContent));
//                                    break;
//                                case "answerTime":
//                                    if (!StringUtils.isEmpty(tagContent))
//                                        cdr.setAnswerTime(cdr.formatDate(tagContent));
//                                    break;
//                                case "releaseTime":
//                                    if (!StringUtils.isEmpty(tagContent))
//                                        cdr.setReleaseTime(cdr.formatDate(tagContent));
//                                    break;
//                                case "userTimeZone":
//                                    if (!StringUtils.isEmpty(tagContent))
//                                        cdr.setCdrEventTimezone(tagContent);
//                                    break;
//                                case "answerIndicator":
//                                    if (!StringUtils.isEmpty(tagContent)) {
//                                        cdr.setAnswerIndicator(tagContent);
//                                        cdr.setAnswered(tagContent.toLowerCase().contains("yes"));
//                                    }
//                                    break;
//                                case "localCallId":
//                                    if (!StringUtils.isEmpty(tagContent))
//                                        cdr.setLocalCallId(tagContent);
//                                    break;
//                                case "networkCallID":
//                                    if (!StringUtils.isEmpty(tagContent))
//                                        cdr.setNetworkCallID(tagContent);
//                                    break;
//                                case "extTrackingId":
//                                    if (!StringUtils.isEmpty(tagContent))
//                                        cdr.setChainId(tagContent);
//                                    break;
//                                case "remoteCallId":
//                                    if (!StringUtils.isEmpty(tagContent))
//                                        cdr.setRemoteCallId(tagContent);
//                                    break;
//                                case "releasingParty":   //originator is a call to service number then forward to normal sip
//                                    if (!StringUtils.isEmpty(tagContent))
//                                        cdr.setReleasingParty(tagContent);
//                                    break;
//                                case "relatedCallId":   //originator is a call to service number then forward to normal sip
//                                    if (!StringUtils.isEmpty(tagContent))
//                                        cdr.setRelatedCallId(tagContent);
//                                    break;
//                                case "terminationCause":   //originator is a call to service number then forward to normal sip
//                                    if (!StringUtils.isEmpty(tagContent))
//                                        cdr.setTerminationCause(TerminationCause.fromString(tagContent));
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



