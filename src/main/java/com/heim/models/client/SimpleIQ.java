/**
 * Copyright © 2014 Florian Schmaus
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.heim.models.client;

import org.jivesoftware.smack.packet.IQ;


/**
 * A simple IQ.
 * <p>
 * Simple IQs child elements do not contain further attributes besides 'xmlns'. They may contain additional packet
 * extensions.
 * </p>
 */
public abstract class SimpleIQ extends IQ {

    protected SimpleIQ(String childElementName, String childElementNamespace) {
        super(childElementName, childElementNamespace);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {


        xml.setEmptyElement();
        return xml;
    }

}
