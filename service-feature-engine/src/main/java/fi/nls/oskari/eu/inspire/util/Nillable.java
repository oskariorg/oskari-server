package fi.nls.oskari.eu.inspire.util;

import javax.xml.bind.annotation.XmlAttribute;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class Nillable {

    public Nillable() {
        
    }
    public Nillable(final String value) {
        this.value = value;
    }
    
    @JacksonXmlText(value = true)
    public String value;

    @XmlAttribute(name = "nilReason", required = false)
    public String nilReason;

    @XmlAttribute(required = false, name = "nil", namespace = "http://www.w3.org/2001/XMLSchema-instance")
    public boolean nil;
}