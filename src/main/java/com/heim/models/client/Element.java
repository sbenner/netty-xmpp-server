package com.heim.models.client;

public interface Element {

    /**
     * Returns the XML representation of this Element.
     *
     * @return the stanza(/packet) extension as XML.
     */
    CharSequence toXML();
}