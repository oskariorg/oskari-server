package fi.nls.oskari.eu.inspire.gmlas.commontransportelements;

import java.util.Calendar;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import fi.nls.oskari.eu.inspire.gmlas.geographicalnames.GeographicalName;
import fi.nls.oskari.eu.inspire.gmlas.network.LinkType;

public class TransportLinkType extends LinkType {

    @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CommonTransportElements:3.0")
    public List<GeographicalName> geographicalName;

    @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CommonTransportElements:3.0")
    public String validFrom;

    @JacksonXmlProperty(namespace = "urn:x-inspire:specification:gmlas:CommonTransportElements:3.0")
    public String validTo;

}
