package fi.nls.oskari.wfs;

import fi.nls.oskari.transport.TransportJobException;
import org.geotools.feature.FeatureCollection;
import org.geotools.xml.Parser;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.fail;

public class WFSExportTest {

    String srespo = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<wfs:FeatureCollection xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:oskari=\"http://www.oskari.org\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" numberOfFeatures=\"1\" timeStamp=\"2017-02-09T15:41:26.930Z\" xsi:schemaLocation=\"http://www.oskari.org http://localhost:8080/geoserver/oskari/wfs?service=WFS&amp;version=1.1.0&amp;request=DescribeFeatureType&amp;typeName=oskari%3Amy_places http://www.opengis.net/wfs http://localhost:8080/geoserver/schemas/wfs/1.1.0/wfs.xsd\">\n" +
            "    <gml:featureMember>\n" +
            "        <oskari:my_places gml:id=\"my_places.6\">\n" +
            "            <gml:name>Espoo</gml:name>\n" +
            "            <oskari:uuid>asdf-asdf-asdf-asdf-asdf</oskari:uuid>\n" +
            "            <oskari:category_id>1</oskari:category_id>\n" +
            "            <oskari:attention_text />\n" +
            "            <oskari:created>2017-02-09T11:06:43.419Z</oskari:created>\n" +
            "            <oskari:geometry>\n" +
            "                <gml:MultiPoint srsDimension=\"2\" srsName=\"http://www.opengis.net/gml/srs/epsg.xml#3067\">\n" +
            "                    <gml:pointMember>\n" +
            "                        <gml:Point srsDimension=\"2\">\n" +
            "                            <gml:pos>371384.5 6678808.5</gml:pos>\n" +
            "                        </gml:Point>\n" +
            "                    </gml:pointMember>\n" +
            "                </gml:MultiPoint>\n" +
            "            </oskari:geometry>\n" +
            "            <oskari:place_desc>test1</oskari:place_desc>\n" +
            "            <oskari:link />\n" +
            "            <oskari:image_url />\n" +
            "        </oskari:my_places>\n" +
            "    </gml:featureMember>\n" +
            "</wfs:FeatureCollection>";


    @Test
    public void testExportSimpleFeatures() throws ParserConfigurationException, SAXException, IOException {

        Parser parser = GMLParser3.getParserWithoutSchemaLocator();

        BufferedReader response = new BufferedReader(new StringReader(srespo));

        Object obj;
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = null;
        try {
            obj = parser.parse(response);

            if (obj instanceof FeatureCollection) {
                featureCollection = (FeatureCollection<SimpleFeatureType, SimpleFeature>) obj;
            } else {
                fail("GML3 parser should result featureCollection");
            }
        } catch (Exception e) {
            fail("GML3 parser should not throw exception");
        }

        //Export  featureCollection
        try {
            WFSExport export = new WFSExport("GPX", "EPSG:3067");
            String filename = export.export(featureCollection);
        }
        catch (TransportJobException e) {
            if(!e.getMessageKey().equals(WFSExceptionHelper.WARNING_GDAL_NOT_INSTALLED))
            {
                fail("WFSExport should not throw exception");
            }
        }
        catch (Exception e) {

            fail("WFSExport should not throw exception");
        }
    }
}
