package org.oskari.csw.request;

import fi.nls.oskari.search.channel.MetadataCatalogueQueryHelper;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.expression.Expression;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.oskari.geojson.GeoJSONReader;
import org.xml.sax.SAXException;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static fi.nls.test.util.XmlTestHelper.compareXML;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetRecordsTest {

    private FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();

    @Test()
    public void testWithNoFilter() {
        assertThrows(ServiceRuntimeException.class , () -> {
            org.oskari.csw.request.GetRecords.createRequest(null);
        });
    }

    @Test
    public void testRequestType() {
        // build filter
        Filter filter = createEqualsFilter("csw:Any", "testing");
        String request = GetRecords.createRequest(filter);

        assertTrue(request.contains("<csw:ElementSetName>summary</csw:ElementSetName>"), "Should get 'summary' as request type");

        request = GetRecords.createRequest(filter, "brief");
        assertTrue(request.contains("<csw:ElementSetName>brief</csw:ElementSetName>"), "Should get 'brief' as request type");

        request = GetRecords.createRequest(filter, "full");
        assertTrue(request.contains("<csw:ElementSetName>full</csw:ElementSetName>"), "Should get 'full' as request type");
    }

    @Test()
    public void testRequestTypeInvalid() {
        // build filter
        assertThrows(IllegalArgumentException.class, () -> {
            Filter filter = createEqualsFilter("csw:Any", "testing");
            org.oskari.csw.request.GetRecords.createRequest(filter, "dummy");
        });
    }

    @Test
    public void testSimpleFilter() throws IOException, SAXException {
        // build filter
        Filter filter = createEqualsFilter("my value", "myprop");
        String request = GetRecords.createRequest(filter);

        // read expected result and compare
        String expected = IOHelper.readString(getClass().getResourceAsStream("GetRecords-simple.xml"));
        Diff xmlDiff = compareXML(request, expected);
        assertFalse(xmlDiff.hasDifferences(), "Should get expected simple request" + xmlDiff);
    }

    @Test
    public void testMultiFilter() throws IOException, SAXException {
        // build filter
        Filter equalfilter = createEqualsFilter("my value", "myprop");
        Filter likefilter = createLikeFilter("input*", "query");

        String request = GetRecords.createRequest(filterFactory.and(equalfilter, likefilter));

        // read expected result and compare
        String expected = IOHelper.readString(getClass().getResourceAsStream("GetRecords-multi.xml"));
        Diff xmlDiff = compareXML(request, expected);
        assertFalse(xmlDiff.hasDifferences(), "Should get expected and-filter request" + xmlDiff);
    }

    @Test
    public void testCoverageFilter() throws IOException, SAXException {

        // build filter
        String input = "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[292864,6845440],[292864,6781952],[393216,6781952],[393216,6845440],[292864,6845440]]]},\"properties\":{\"area\":\"Alue ei saa muodostaa silmukkaa. Piirrä risteämätön alue nähdäksesi mittaustuloksen.\"},\"id\":\"drawFeature3\"}],\"crs\":\"EPSG:3067\"}";
        Filter filter = createGeometryFilter(input);
        String request = GetRecords.createRequest(filter);

        // read expected result and compare
        String expected = IOHelper.readString(getClass().getResourceAsStream("GetRecords-coverage.xml"));
        Diff xmlDiff = compareXML(request, expected);

        //for getting detailed differences between two xml files
        List<Difference> differences = new ArrayList();
        xmlDiff.getDifferences().forEach(differences::add);

        boolean compareXpathOnly = differences.size() == 1;
        if (compareXpathOnly) {
            // Java 11/17 produces different results for transforms than Java 8
            // The expected result is produced with Java 17
            // if the only thing that's different is the content of coordinates everything is fine
            final String coordinatesPath =
                    "/GetRecords[1]/Query[1]/Constraint[1]/Filter[1]/Intersects[1]/Polygon[1]/outerBoundaryIs[1]/LinearRing[1]/coordinates[1]/text()[1]";
            // if the difference is NOT the coordinates -> we have a problem
            assertEquals("Something else than coordinates transform differ in expected and result",
                    coordinatesPath, differences.get(0).getComparison().getTestDetails().getXPath());
        }
        assertFalse(xmlDiff.hasDifferences(), "Should get expected coverage request" + xmlDiff);
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