package fi.nls.oskari.fe.xml.util;

import javax.xml.bind.annotation.XmlAttribute;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class Reference extends Associable {

    @JacksonXmlProperty(isAttribute = true, namespace = "http://www.w3.org/1999/xlink")
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
