package fi.nls.oskari.fe.gml.util;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import fi.nls.oskari.fe.xml.util.Reference;

public class DirectPositionType extends Reference {

    public DirectPositionType() {
        
    }
    public DirectPositionType(String value) {
        this.value = value;
    }
    
    @JacksonXmlText
    public String value;
    
    @JacksonXmlProperty(isAttribute=true,localName="axisLabels")
    public String axisLabels;
    
    @JacksonXmlProperty(isAttribute=true,localName="srsDimension")
    public String srsDimension;
    
    @JacksonXmlProperty(isAttribute=true,localName="srsName")
    public String srsName;
    
    @JacksonXmlProperty(isAttribute=true,localName="uomLabels")
    public String uomLabels;

}
