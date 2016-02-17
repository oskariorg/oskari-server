package fi.nls.oskari.control.statistics.xml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * We just need the name and id from here.
 * Example:
 * 
 * <wfs:FeatureCollection numberMatched="320" numberReturned="320" timeStamp="2015-11-05T15:58:30.588Z"
 *   xsi:schemaLocation="http://www.opengis.net/gml/3.2 http://localhost:8080/geoserver/schemas/gml/3.2.1/gml.xsd
 *     http://www.opengis.net/wfs/2.0 http://localhost:8080/geoserver/schemas/wfs/2.0/wfs.xsd http://www.oskari.org
 *     http://localhost:8080/geoserver/wfs?service=WFS&version=2.0.0&request=DescribeFeatureType&typeName=oskari%3Akunnat2013">
 *   <wfs:boundedBy><gml:Envelope><gml:lowerCorner>83741.81960000005 6636325.0362</gml:lowerCorner><gml:upperCorner>732907.822 7776430.911</gml:upperCorner></gml:Envelope></wfs:boundedBy>
 *   <wfs:member>
 *     <oskari:kunnat2013 gml:id="kunnat2013.fid-2f003dff_150d823048f_-60b9">
 *       <gml:boundedBy><gml:Envelope srsDimension="2" srsName="urn:ogc:def:crs:EPSG::3067"><gml:lowerCorner>321987.072 6959704.551</gml:lowerCorner><gml:upperCorner>366787.924 7005219.8</gml:upperCorner></gml:Envelope></gml:boundedBy>
 *       <oskari:kuntanimi>Alajärvi</oskari:kuntanimi>
 *       <oskari:kuntakoodi>005</oskari:kuntakoodi>
 *     </oskari:kunnat2013>
 *   </wfs:member> ...
 */
public class WfsXmlParser {
    public static List<RegionCodeNamePair> parse(String xml, String codeTag, String nameTag) throws XMLStreamException {
        XMLInputFactory f = XMLInputFactory.newInstance();
        XMLStreamReader r = f.createXMLStreamReader( new StringReader(xml) );
        List<RegionCodeNamePair> nameCodes = new ArrayList<>();
        String code = "";
        String name = "";
        while(r.hasNext()) {
            if (r.isStartElement()) {
                if (r.getLocalName().endsWith(codeTag)) {
                    code = r.getElementText();
                } else if (r.getLocalName().endsWith(nameTag)) {
                    name = r.getElementText();
                }
            } else if (r.isEndElement()) {
                if (r.getLocalName().endsWith("member")) {
                    nameCodes.add(new RegionCodeNamePair(code, name));
                    code = "";
                    name = "";
                }
            }
            r.next();
        }
        return nameCodes;
    }
}
