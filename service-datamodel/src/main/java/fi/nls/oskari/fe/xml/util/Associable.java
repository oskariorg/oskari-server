package fi.nls.oskari.fe.xml.util;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Associable extends Identifiable {

    // [/~{http://www.w3.org/1999/xlink}actuate] [0..1]
    @JacksonXmlProperty(isAttribute = true, namespace = "http://www.w3.org/1999/xlink")
    public String actuate;

    // [/~{http://www.w3.org/1999/xlink}arcrole] [0..1]
    @JacksonXmlProperty(isAttribute = true, namespace = "http://www.w3.org/1999/xlink")
    public String arcrole;

    // [/~{http://www.w3.org/1999/xlink}href] [0..1]
    @JacksonXmlProperty(isAttribute = true, namespace = "http://www.w3.org/1999/xlink")
    public String href;

    @JacksonXmlProperty(isAttribute = true, namespace = "http://www.w3.org/1999/xlink")
    public String nilReason;

 
    @JacksonXmlProperty(isAttribute = true, namespace = "http://www.w3.org/1999/xlink")
    public String role;

    @JacksonXmlProperty(isAttribute = true, namespace = "http://www.w3.org/1999/xlink")
    public String show;

    @JacksonXmlProperty(isAttribute = true, namespace = "http://www.w3.org/1999/xlink")
    public String title;
    @JacksonXmlProperty(isAttribute = true, namespace = "http://www.w3.org/1999/xlink")
    public String type;
    
}
