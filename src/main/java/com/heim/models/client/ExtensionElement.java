package com.heim.models.client;

public interface ExtensionElement extends NamedElement {

    /**
     * Returns the root element XML namespace.
     *
     * @return the namespace.
     */
    String getNamespace();

}