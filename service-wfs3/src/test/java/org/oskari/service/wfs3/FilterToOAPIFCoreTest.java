package org.oskari.service.wfs3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.geotools.api.filter.And;
import org.geotools.api.filter.Filter;
import org.geotools.api.filter.FilterFactory;
import org.geotools.api.filter.PropertyIsEqualTo;
import org.geotools.api.filter.PropertyIsNotEqualTo;
import org.geotools.api.filter.expression.Literal;
import org.geotools.api.filter.expression.PropertyName;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import fi.nls.oskari.domain.map.OskariLayer;

public class FilterToOAPIFCoreTest {

    @Test
    public void testBboxAndPropertyFilter() throws NoSuchAuthorityCodeException, FactoryException, JSONException {
        OskariLayer layer = new OskariLayer();
        String tm35finURI = "http://www.opengis.net/def/crs/EPSG/0/3067";
        layer.setCapabilities(new JSONObject().put("crs-uri", new JSONArray(Arrays.asList(tm35finURI))));
        FilterToOAPIFCoreQuery f = new FilterToOAPIFCoreQuery(layer);
        FilterFactory ff = CommonFactoryFinder.getFilterFactory();

        double minEast = 500000.0;
        double maxEast = 501000.0;
        double minNorth = 6740263.0;
        double maxNorth = 6741263.0;
        CoordinateReferenceSystem tm35fin = CRS.decode("EPSG:3067");

        int a = 50;
        double b = -1337.0;
        String s = "qux";

        Filter foo = ff.equals(ff.property("foo"), ff.literal(a));
        Filter bar = ff.equals(ff.property("bar"), ff.literal(b));
        Filter baz = ff.equals(ff.property("baz"), ff.literal(s));
        Filter bbox = toBboxFilter(ff, minEast, minNorth, maxEast, maxNorth, tm35fin);
        Filter and = ff.and(Arrays.asList(bbox, foo, bar, baz));

        Map<String, String> actual = new HashMap<>();
        Filter postFilter = f.toQueryParameters(and, actual);

        Map<String, String> expected = new HashMap<>();
        expected.put("foo", Integer.toString(a));
        expected.put("bar", Double.toString(b));
        expected.put("baz", s);
        expected.put("bbox", String.format(Locale.US, "%f,%f,%f,%f", minEast, minNorth, maxEast, maxNorth));
        expected.put("bbox-crs", tm35finURI);
        assertEquals(expected, actual);

        assertEquals(Filter.INCLUDE, postFilter);
    }

    @Test
    public void testNotQueryablePropertyFilter() throws NoSuchAuthorityCodeException, FactoryException, JSONException {
        OskariLayer layer = new OskariLayer();
        String tm35finURI = "http://www.opengis.net/def/crs/EPSG/0/3067";
        layer.setCapabilities(new JSONObject().put("crs-uri", new JSONArray(Arrays.asList(tm35finURI))));
        layer.setAttributes(new JSONObject().put(FilterToOAPIFCoreQuery.ATTRIBUTE_QUERYABLES, new JSONArray(Arrays.asList("foo", "bar"))));

        FilterToOAPIFCoreQuery f = new FilterToOAPIFCoreQuery(layer);
        FilterFactory ff = CommonFactoryFinder.getFilterFactory();

        double minEast = 500000.0;
        double maxEast = 501000.0;
        double minNorth = 6740263.0;
        double maxNorth = 6741263.0;
        CoordinateReferenceSystem tm35fin = CRS.decode("EPSG:3067");

        int a = 50;
        double b = -1337.0;
        String c = "abc";
        String d = "zzz";

        Filter foo = ff.equals(ff.property("foo"), ff.literal(a));
        Filter bar = ff.equals(ff.property("bar"), ff.literal(b));
        Filter baz = ff.equals(ff.property("baz"), ff.literal(c));
        Filter qux = ff.equals(ff.property("qux"), ff.literal(d));
        Filter bbox = toBboxFilter(ff, minEast, minNorth, maxEast, maxNorth, tm35fin);
        Filter and = ff.and(Arrays.asList(bbox, foo, bar, baz, qux));

        Map<String, String> actual = new HashMap<>();
        Filter postFilter = f.toQueryParameters(and, actual);

        // baz and qux not queryable
        Map<String, String> expected = new HashMap<>();
        expected.put("foo", Integer.toString(a));
        expected.put("bar", Double.toString(b));
        expected.put("bbox", String.format(Locale.US, "%f,%f,%f,%f", minEast, minNorth, maxEast, maxNorth));
        expected.put("bbox-crs", tm35finURI);
        assertEquals(expected, actual);

        if (!(postFilter instanceof And)) {
            fail();
        }
        assertPropertyIsEqualTo(((And) postFilter).getChildren().get(1), "baz", c);
        assertPropertyIsEqualTo(((And) postFilter).getChildren().get(0), "qux", d);
    }

    private void assertPropertyIsEqualTo(Filter filter, String expectedPropertyName, Object expectedLiteral) {
        if (!(filter instanceof PropertyIsEqualTo)) {
            fail();
        }
        PropertyIsEqualTo eq = (PropertyIsEqualTo) filter;

        if (!(eq.getExpression1() instanceof PropertyName)) {
            fail();
        }
        assertEquals(expectedPropertyName, ((PropertyName) eq.getExpression1()).getPropertyName());

        if (!(eq.getExpression2() instanceof Literal)) {
            fail();
        }
        assertEquals(expectedLiteral, ((Literal) eq.getExpression2()).getValue());
    }

    @Test
    public void testNotEqualTo() throws NoSuchAuthorityCodeException, FactoryException, JSONException {
        OskariLayer layer = new OskariLayer();
        String tm35finURI = "http://www.opengis.net/def/crs/EPSG/0/3067";
        layer.setCapabilities(new JSONObject().put("crs-uri", new JSONArray(Arrays.asList(tm35finURI))));
        FilterToOAPIFCoreQuery f = new FilterToOAPIFCoreQuery(layer);
        FilterFactory ff = CommonFactoryFinder.getFilterFactory();

        double minEast = 500000.0;
        double maxEast = 501000.0;
        double minNorth = 6740263.0;
        double maxNorth = 6741263.0;
        CoordinateReferenceSystem tm35fin = CRS.decode("EPSG:3067");

        int a = 50;
        int b = 30;

        Filter bbox = toBboxFilter(ff, minEast, minNorth, maxEast, maxNorth, tm35fin);
        Filter eq = ff.equals(ff.property("myprop"), ff.literal(a));
        Filter notEq = ff.notEqual(ff.property("myprop2"), ff.literal(b));
        Filter and = ff.and(Arrays.asList(bbox, eq, notEq));

        Map<String, String> actual = new HashMap<>();
        Filter postFilter = f.toQueryParameters(and, actual);

        Map<String, String> expected = new HashMap<>();
        expected.put("myprop", Integer.toString(a));
        expected.put("bbox", String.format(Locale.US, "%f,%f,%f,%f", minEast, minNorth, maxEast, maxNorth));
        expected.put("bbox-crs", tm35finURI);
        assertEquals(expected, actual);

        if (!(postFilter instanceof PropertyIsNotEqualTo)) {
            fail("Expected PostFilter to be PropertyIsNotEqualTo");
        }
        PropertyIsNotEqualTo neq = (PropertyIsNotEqualTo) postFilter;
        assertEquals(b, ((Literal) neq.getExpression2()).getValue());
    }

    @Test
    public void testSimpleOr() throws NoSuchAuthorityCodeException, FactoryException, JSONException {
        OskariLayer layer = new OskariLayer();
        String tm35finURI = "http://www.opengis.net/def/crs/EPSG/0/3067";
        layer.setCapabilities(new JSONObject().put("crs-uri", new JSONArray(Arrays.asList(tm35finURI))));
        FilterToOAPIFCoreQuery f = new FilterToOAPIFCoreQuery(layer);
        FilterFactory ff = CommonFactoryFinder.getFilterFactory();

        Filter eq1 = ff.equals(ff.property("myprop"), ff.literal(1));
        Filter eq2 = ff.equals(ff.property("myprop"), ff.literal(2));
        Filter eq3 = ff.equals(ff.property("myprop"), ff.literal(3));
        Filter or = ff.or(Arrays.asList(eq1, eq2, eq3));

        Map<String, String> actual = new HashMap<>();
        Filter postFilter = f.toQueryParameters(or, actual);

        Map<String, String> expected = new HashMap<>();
        expected.put("myprop", "1,2,3");
        assertEquals(expected, actual);

        assertEquals(Filter.INCLUDE, postFilter);
    }

    @Test
    public void testNonSenseAnd() throws NoSuchAuthorityCodeException, FactoryException, JSONException {
        OskariLayer layer = new OskariLayer();
        String tm35finURI = "http://www.opengis.net/def/crs/EPSG/0/3067";
        layer.setCapabilities(new JSONObject().put("crs-uri", new JSONArray(Arrays.asList(tm35finURI))));
        FilterToOAPIFCoreQuery f = new FilterToOAPIFCoreQuery(layer);
        FilterFactory ff = CommonFactoryFinder.getFilterFactory();

        Filter eq1 = ff.equals(ff.property("myprop"), ff.literal(1));
        Filter eq2 = ff.equals(ff.property("myprop"), ff.literal(2));
        Filter eq3 = ff.equals(ff.property("myprop"), ff.literal(3));
        Filter and = ff.and(Arrays.asList(eq1, eq2, eq3));

        try {
            f.toQueryParameters(and, new HashMap<>());
            fail();
        } catch (UnsupportedOperationException expected) {
            // success
        }
    }

    private Filter toBboxFilter(FilterFactory ff,
            double x1, double y1, double x2, double y2,
            CoordinateReferenceSystem crs) {
        return ff.bbox("geom", x1, y1, x2, y2, CRS.toSRS(crs));
    }

}
