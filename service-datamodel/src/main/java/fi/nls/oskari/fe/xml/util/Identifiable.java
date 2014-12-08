package fi.nls.oskari.fe.xml.util;

import javax.xml.bind.annotation.XmlAttribute;

public class Identifiable {

    @XmlAttribute(required = false)
    public String id;

    @XmlAttribute(required = false)
    public boolean uuid;

}
