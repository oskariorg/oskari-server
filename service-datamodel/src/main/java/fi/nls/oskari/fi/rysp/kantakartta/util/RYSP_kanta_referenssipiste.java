package fi.nls.oskari.fi.rysp.kantakartta.util;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import fi.nls.oskari.fe.gml.util.GeometryProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RYSP_kanta_referenssipiste extends GeometryProperty {
    public static final String NS = "http://www.paikkatietopalvelu.fi/gml/kantakartta";
    public static final QName QN = new QName(NS, "referenssipiste");

    @JacksonXmlText(value = true)
    public String value;

}
