package fi.nls.oskari.eu.inspire.gmlas.geographicalnames;

import java.util.List;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0", localName = "GeographicalName")
public class GeographicalName extends GeographicalNameType {
    public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
    public static final QName QN = new QName(NS, "NamedPlace");

    @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0")
    public List<GeographicalNameType> name;

}
