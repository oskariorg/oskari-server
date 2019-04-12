package org.oskari.csw.request;

import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.search.channel.MetadataCatalogueQueryHelper;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.oskari.geojson.GeoJSONReader;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class GetRecordsTest {

    private FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2();

    @BeforeClass
    public static void setUp() {
        // use relaxed comparison settings
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setIgnoreAttributeOrder(true);
    }

    @Test(expected = ServiceRuntimeException.class)
    public void testWithNoFilter() {
        org.oskari.csw.request.GetRecords.createRequest(null);
        fail("Should have thrown exception");
    }

    @Test
    public void testSimpleFilter() throws IOException, SAXException {
        // build filter
        Filter filter = createEqualsFilter("my value", "myprop");
        String request = org.oskari.csw.request.GetRecords.createRequest(filter);

        // read expected result and compare
        String expected = IOHelper.readString(getClass().getResourceAsStream("GetRecords-simple.xml"));
        Diff xmlDiff = new Diff(request, expected);
        assertTrue("Should get expected simple request" + xmlDiff, xmlDiff.similar());
    }

    @Test
    public void testMultiFilter() throws IOException, SAXException {
        // build filter
        Filter equalfilter = createEqualsFilter("my value", "myprop");
        Filter likefilter = createLikeFilter("input*", "query");

        String request = org.oskari.csw.request.GetRecords.createRequest(filterFactory.and(equalfilter, likefilter));
//        System.out.println(request);

        // read expected result and compare
        String expected = IOHelper.readString(getClass().getResourceAsStream("GetRecords-multi.xml"));
        Diff xmlDiff = new Diff(request, expected);
        assertTrue("Should get expected and-filter request" + xmlDiff, xmlDiff.similar());
    }

    @Test
    public void testCoverageFilter() throws IOException, SAXException {

        // build filter
        String input = "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[292864,6845440],[292864,6781952],[393216,6781952],[393216,6845440],[292864,6845440]]]},\"properties\":{\"area\":\"Alue ei saa muodostaa silmukkaa. Piirrä risteämätön alue nähdäksesi mittaustuloksen.\"},\"id\":\"drawFeature3\"}],\"crs\":\"EPSG:3067\"}";
        Filter filter = createGeometryFilter(input);
        String request = org.oskari.csw.request.GetRecords.createRequest(filter);

        // read expected result and compare
        String expected = IOHelper.readString(getClass().getResourceAsStream("GetRecords-coverage.xml"));
        Diff xmlDiff = new Diff(request, expected);
        //for getting detailed differences between two xml files
        DetailedDiff detailXmlDiff = new DetailedDiff(xmlDiff);

        List<Difference> differences = detailXmlDiff.getAllDifferences();
        boolean isJava11 = System.getProperty("java.version").startsWith("11.");
        if (isJava11 && differences.size() == 1) {
            // Java 11 produces different results for transforms than Java 8
            // The expected result is produced with Java 8
            // if the only thing that's different is the content of coordinates everything is fine
            final String coordinatesPath =
                    "/GetRecords[1]/Query[1]/Constraint[1]/Filter[1]/Intersects[1]/Polygon[1]/outerBoundaryIs[1]/LinearRing[1]/coordinates[1]/text()[1]";
            // if the difference is NOT the coordinates -> we have a problem
            assertEquals("Something else than coordinates transform differ in expected and result",
                    coordinatesPath, differences.get(0).getTestNodeDetail().getXpathLocation());
        } else {
            assertTrue("Should get expected coverage request" + xmlDiff, xmlDiff.similar());
        }
    }

    private Filter createLikeFilter(final String searchCriterion,
                                    final String searchElementName) {
        if (searchCriterion == null || searchCriterion.isEmpty()) {
            return null;
        }
        Expression _property = filterFactory.property(searchElementName);
        return filterFactory.like(_property, searchCriterion,
                MetadataCatalogueQueryHelper.WILDCARD_CHARACTER,
                MetadataCatalogueQueryHelper.SINGLE_WILDCARD_CHARACTER,
                MetadataCatalogueQueryHelper.ESCAPE_CHARACTER,
                false);
    }

    private Filter createEqualsFilter(final String searchCriterion,
                                      final String searchElementName) {
        if (searchCriterion == null || searchCriterion.isEmpty()) {
            return null;
        }
        Expression _property = filterFactory.property(searchElementName);
        return filterFactory.equals(_property, filterFactory.literal(searchCriterion));
    }

    private Filter createGeometryFilter(final String searchCriterion) {
        try {
            JSONObject geojson = JSONHelper.createJSONObject(searchCriterion);

            JSONArray features = geojson.optJSONArray("features");
            if (features == null || features.length() != 1) {
                return null;
            }
            Geometry geom = GeoJSONReader.toGeometry(features.optJSONObject(0).optJSONObject("geometry"));
            CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:3067");
            CoordinateReferenceSystem targetCRS = CRS.decode(MetadataCatalogueQueryHelper.TARGET_SRS, true);

            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
            Geometry transformed = JTS.transform(geom, transform);

            return filterFactory.intersects(
                    filterFactory.property("ows:BoundingBox"),
                    filterFactory.literal(transformed));
        } catch (Exception e) {
            throw new ServiceRuntimeException("Can't create GetRecords request with coverage filter", e);
        }
    }

}