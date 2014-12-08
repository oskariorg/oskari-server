package fi.nls.oskari.eu.inspire.gmlas.roadtransportnetwork;

import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import fi.nls.oskari.eu.inspire.gmlas.commontransportelements.TransportLinkType;

public class RoadLinkType extends TransportLinkType {
    @JacksonXmlProperty(isAttribute = true, localName = "id", namespace = "http://www.opengis.net/gml/3.2")
    public String id;

    /* NLS  */
    @XmlElement(required = false, namespace = "http://www.opengis.net/gml/3.2")
    public String identifier;
}
