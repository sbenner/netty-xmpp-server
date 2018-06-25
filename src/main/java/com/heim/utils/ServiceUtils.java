package com.heim.utils;

import org.jivesoftware.smack.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class ServiceUtils {

    private static Logger logger = LoggerFactory.getLogger(ServiceUtils.class);

    public static Properties loadStanzas() throws Exception {
        Properties properties = new Properties();

        try (
                InputStream xmlStream =
                        ServiceUtils.class.getClassLoader().getResourceAsStream("server_stanzas.xml")
        ) {


            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader =
                    factory.createXMLStreamReader(xmlStream);
            StringBuilder tagContent = null;
            String propertyName = null;
            while (reader.hasNext()) {
                int event = reader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        if ("property".equals(reader.getLocalName())) {
                            propertyName = reader.getAttributeValue(null, "name");
                        }
                        if ("value".equals(reader.getLocalName())) {
                            tagContent = new StringBuilder();
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                    case XMLStreamConstants.CDATA:
                        if (tagContent != null)
                            tagContent.append(reader.getText().trim());
                        break;
                    case XMLStreamConstants.END_ELEMENT:

                        switch (reader.getLocalName()) {
                            case "value":
                                if (StringUtils.isNotEmpty(propertyName)
                                        && Optional.ofNullable(tagContent).isPresent()) {
                                    properties.setProperty(propertyName, tagContent.toString());
                                    propertyName = null;
                                    tagContent = null;
                                }
                                break;
                        }
                        break;
                    case XMLStreamConstants.START_DOCUMENT:
                        break;

                }

            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return properties;
    }

}
