package fi.nls.oskari.isotc211.gmd;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import fi.nls.oskari.fe.xml.util.Reference;

@JacksonXmlRootElement(namespace = "http://www.isotc211.org/2005/gmd")
public class LocalisedCharacterString extends Reference { // shortcut

    @JacksonXmlProperty(isAttribute = true)
    public String id;
    @JacksonXmlProperty(isAttribute = true)
    public String locale;
    
    @JacksonXmlText(value = true)
    public String value;
    
    public LocalisedCharacterString() {}
    public LocalisedCharacterString(final String val ) {
        value = val;
    }
}
