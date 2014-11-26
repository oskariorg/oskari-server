package fi.nls.oskari.fe.gml.util;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class CodeType {

    @JacksonXmlProperty(isAttribute = true)
    public String codeSpace;

    @JacksonXmlText(value = true)
    public String value;
}
