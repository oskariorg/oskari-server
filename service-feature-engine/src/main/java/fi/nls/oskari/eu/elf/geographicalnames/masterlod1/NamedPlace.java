package fi.nls.oskari.eu.elf.geographicalnames.masterlod1;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;


@JacksonXmlRootElement(namespace = "http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0")
public class NamedPlace extends NamedPlaceType {
    public static final String NS = "http://www.locationframework.eu/schemas/GeographicalNames/MasterLoD1/1.0";
    public static final QName QN = new QName(NS, "NamedPlace");
}
