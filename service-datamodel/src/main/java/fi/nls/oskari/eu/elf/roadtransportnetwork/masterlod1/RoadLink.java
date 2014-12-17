package fi.nls.oskari.eu.elf.roadtransportnetwork.masterlod1;

import javax.xml.namespace.QName;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import fi.nls.oskari.eu.inspire.gmlas.roadtransportnetwork.RoadLinkType;

@JacksonXmlRootElement(namespace = "http://www.locationframework.eu/schemas/RoadTransportNetwork/MasterLoD1/1.0", localName = "RoadLink")
public class RoadLink extends RoadLinkType {
    public static final String NS = "http://www.locationframework.eu/schemas/RoadTransportNetwork/MasterLoD1/1.0";
    public static final QName QN = new QName(NS, "RoadLink");
}
