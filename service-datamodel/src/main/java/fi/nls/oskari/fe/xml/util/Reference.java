package fi.nls.oskari.fe.xml.util;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import javax.xml.bind.annotation.XmlAttribute;

public class Reference extends Associable {

    
    //@JacksonXmlProperty(localName="owns", isAttribute = true)
    @XmlAttribute(name = "owns", required = false)
    public String owns;

    @JacksonXmlText
    public String value;

    @XmlAttribute(required = false, name = "nil", namespace = "http://www.w3.org/2001/XMLSchema-instance")
    public boolean nil;

    @XmlAttribute(name = "nilReason", required = false)
    public String nilReason;

    public Reference() {
    }

    public Reference(final String value) {
        this.value = value;
    }

}
