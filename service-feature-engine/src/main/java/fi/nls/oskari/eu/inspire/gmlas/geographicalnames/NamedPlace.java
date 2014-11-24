package fi.nls.oskari.eu.inspire.gmlas.geographicalnames;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(namespace = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0")
public class NamedPlace extends NamedPlaceType {

    public static final String NS = "urn:x-inspire:specification:gmlas:GeographicalNames:3.0";
    public static final QName QN = new QName(NS, "NamedPlace");
}
