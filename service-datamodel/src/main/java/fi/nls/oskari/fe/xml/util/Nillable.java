package fi.nls.oskari.fe.xml.util;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import javax.xml.bind.annotation.XmlAttribute;

public class Nillable extends Reference {

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

    public String toString() {
        if( this.value != null ) {
            return this.value.toString(); 
        } else { 
            return null;
        }
    }
}