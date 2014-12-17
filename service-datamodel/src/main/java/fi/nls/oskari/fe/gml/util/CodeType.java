package fi.nls.oskari.fe.gml.util;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class CodeType {

    @JacksonXmlProperty(isAttribute = true)
    public String codeSpace;
    
    @JacksonXmlProperty(isAttribute = true)
    public String codeListValue;

    @JacksonXmlProperty(isAttribute = true)
    public String codeList;

    @JacksonXmlText(value = true)
    public String value;
}
