package fi.nls.oskari.isotc211.gmd;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import fi.nls.oskari.fe.xml.util.Nillable;

@JacksonXmlRootElement(namespace = "http://www.isotc211.org/2005/gmd")
public class MD_RepresentativeFraction {
    
    static class NillableInteger extends Nillable {
        
        @JacksonXmlProperty(localName="Integer")
        public String _Integer;
        
        public Integer toInteger() {
            return Integer.parseInt(_Integer);
        }
    }

    // shortcut to gco:Integer_PropertyType
    @JacksonXmlProperty
    public NillableInteger denominator;

}
