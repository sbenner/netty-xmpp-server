//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.05.28 at 07:27:09 PM MSK 
//


package com.heim.models.auth;


import com.heim.models.bind.Bind;
import com.heim.models.xmpp_stanzas.Text;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the ietf.params.xml.ns.xmpp_sasl package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Response_QNAME = new QName("urn:ietf:params:xml:ns:xmpp-sasl", "response");
    private final static QName _Success_QNAME = new QName("urn:ietf:params:xml:ns:xmpp-sasl", "success");
    private final static QName _Abort_QNAME = new QName("urn:ietf:params:xml:ns:xmpp-sasl", "abort");
    private final static QName _Challenge_QNAME = new QName("urn:ietf:params:xml:ns:xmpp-sasl", "challenge");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ietf.params.xml.ns.xmpp_sasl
     */
    public ObjectFactory() {
    }


    public Auth createAuth() {
        return new Auth();
    }

    /**
     * Create an instance of {@link Failure }
     */
    public Failure createFailure() {
        return new Failure();
    }

    public Bind createBind() {
        return new Bind();
    }
    /**
     * Create an instance of {@link Text }
     */
    public Text createText() {
        return new Text();
    }


    /**
     * Create an instance of {@link Mechanisms }
     */
    public Mechanisms createMechanisms() {
        return new Mechanisms();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:xmpp-sasl", name = "response")
    public JAXBElement<String> createResponse(String value) {
        return new JAXBElement<String>(_Response_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:xmpp-sasl", name = "success")
    public JAXBElement<String> createSuccess(String value) {
        return new JAXBElement<String>(_Success_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:xmpp-sasl", name = "abort")
    public JAXBElement<String> createAbort(String value) {
        return new JAXBElement<String>(_Abort_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "urn:ietf:params:xml:ns:xmpp-sasl", name = "challenge")
    public JAXBElement<String> createChallenge(String value) {
        return new JAXBElement<String>(_Challenge_QNAME, String.class, null, value);
    }

}
