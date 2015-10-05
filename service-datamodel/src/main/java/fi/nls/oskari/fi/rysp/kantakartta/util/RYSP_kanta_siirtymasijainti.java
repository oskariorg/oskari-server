package fi.nls.oskari.fi.rysp.kantakartta.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import fi.nls.oskari.fe.gml.util.GeometryProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RYSP_kanta_siirtymasijainti extends GeometryProperty {
    @JacksonXmlText(value = true)
    public String value;

}
