package fi.nls.oskari.eu.inspire.gmlas.roadtransportnetwork;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(namespace = "urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0")
public class RoadLink extends RoadLinkType {
    public static final String NS = "urn:x-inspire:specification:gmlas:RoadTransportNetwork:3.0";
    public static final QName QN = new QName(NS, "RoadLink");
}
