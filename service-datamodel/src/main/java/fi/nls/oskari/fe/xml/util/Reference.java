package fi.nls.oskari.fe.xml.util;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class Reference extends Associable {

    @JacksonXmlProperty(isAttribute = true, namespace = "http://www.w3.org/1999/xlink")
    public String owns;
    
    @JacksonXmlText
    public String value;
    
    public Reference(){}
    public Reference(final String value ) {
        this.value = value;
    }
    

}
