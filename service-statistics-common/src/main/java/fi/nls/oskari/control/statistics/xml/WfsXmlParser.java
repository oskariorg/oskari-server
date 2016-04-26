package fi.nls.oskari.control.statistics.xml;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.geotools.GML;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


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

    private static final Logger LOG = LogFactory.getLogger(WfsXmlParser.class);

    public static List<RegionCodeNamePair> parse(InputStream inputStream, String idProperty, String nameProperty) throws IOException {

        List<RegionCodeNamePair> nameCodes = new ArrayList<>();
        GML gml = new GML(GML.Version.GML3);
        try {
            SimpleFeatureCollection fc = gml.decodeFeatureCollection(inputStream);
            SimpleFeatureIterator it = fc.features();

            while (it.hasNext()) {
                final SimpleFeature feature = it.next();
                Property id = feature.getProperty(idProperty);
                Property name = feature.getProperty(nameProperty);
                if(id == null || name == null) {
                    LOG.debug("Couldn't find id (", idProperty, ") and/or name(", nameProperty,
                            ") property for region. Properties are:", feature.getProperties());
                    continue;
                }
                nameCodes.add(new RegionCodeNamePair(
                        (String)id.getValue(),
                        (String)name.getValue()));
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        }
        if(nameCodes.isEmpty()) {
            throw new IOException("Empty result, check configuration for region id-property=" +
                    idProperty + " and name-property =" + nameProperty);
        }
        return nameCodes;
    }
}
