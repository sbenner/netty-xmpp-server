package com.heim.service;

import com.sun.org.apache.xerces.internal.impl.PropertyManager;
import com.sun.org.apache.xerces.internal.impl.XMLStreamReaderImpl;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;

import javax.xml.stream.XMLStreamException;

/**
 * Created with IntelliJ IDEA.
 * User: sbenner
 * Date: 5/29/18
 * Time: 12:10 AM
 */
public class IqXmlStreamReader extends XMLStreamReaderImpl {

    public IqXmlStreamReader(XMLInputSource inputSource, PropertyManager props) throws XMLStreamException {
        super(inputSource, props);
    }
}
