package org.oskari.service.wfs.client;

import fi.nls.oskari.util.JSONHelper;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OskariWFSFilterTest {
    SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
    SimpleFeatureBuilder fb;
    DefaultFeatureCollection fc;
    String equalFilter = "{\n" +
            "    \"property\": {\n" +
            "        \"key\": \"name\",\n" +
            "        \"caseSensitive\": true,\n" +
            "        \"value\": \"helsinki\"\n" +
            "    }\n" +
            "}";
    String andFilter = "{\n" +
            "    \"AND\": [{\n" +
            "        \"key\": \"type\",\n" +
            "        \"value\": \"city\"\n" +
            "    }, {\n" +
            "        \"key\": \"population\",\n" +
            "        \"greaterThan\": 20000\n" +
            "    }]\n" +
            "}";
    String orFilter = "{\n" +
            "    \"OR\": [{\n" +
            "        \"key\": \"name\",\n" +
            "        \"in\": [\"Tampere\"]\n" +
            "    }, {\n" +
            "        \"key\": \"type\",\n" +
            "        \"value\": \"village\"\n" +
            "    }]\n" +
            "}";
    JSONObject andOrFlter = JSONHelper.merge(
            JSONHelper.createJSONObject(andFilter),
            JSONHelper.createJSONObject(orFilter));
    String rangeFilter = "{\n" +
            "    \"property\": {\n" +
            "        \"key\": \"population\",\n" +
            "        \"greaterThan\": 4000,\n" +
            "        \"lessThan\": 200000\n" +
            "    }\n" +
            "}";
    String inclusiveFilter = rangeFilter
            .replace("greaterThan","atLeast")
            .replace("lessThan","atMost");
    String notInFilter = "{\n" +
            "    \"property\": {\n" +
            "        \"key\": \"name\",\n" +
            "        \"notIn\": [\"Helsinki\", \"Tampere\"]\n" +
            "    }\n" +
            "}";
    String  inFilter = notInFilter.replace("notIn", "in");
    String likeFilter = "{\n" +
            "    \"property\": {\n" +
            "        \"key\": \"name\",\n" +
            "        \"like\": \"Hels*\"\n" +
            "    }\n" +
            "}";
    String notLikeFilter = likeFilter.replace("like", "notLike");

    @BeforeEach
    public void init() {
        tb.setName("test");
        tb.add("name", String.class);
        tb.add("type", String.class);
        tb.add("population", Integer.class);
        tb.add("capital", Boolean.class);
        fb = new SimpleFeatureBuilder(tb.buildFeatureType());

        fc = new DefaultFeatureCollection();
        fb.addAll(Arrays.asList("Helsinki", "city", 600000, true));
        fc.add(fb.buildFeature("fid.1"));
        fb.addAll(Arrays.asList("Sipoo", "town", 25000, false));
        fc.add(fb.buildFeature("fid.2"));
        fb.addAll(Arrays.asList("Tampere", "city", 200000, false));
        fc.add(fb.buildFeature("fid.3"));
        fb.addAll(Arrays.asList("Puuppola", "village", 4000, false));
        fc.add(fb.buildFeature("fid.4"));
    }

    @Test
    public void cqlTest() throws Exception {
        Filter f = OskariWFSFilterFactory.getAttributeFilter(equalFilter);
        Assertions.assertEquals("name = 'helsinki'", CQL.toCQL(f));
        f = OskariWFSFilterFactory.getAttributeFilter(rangeFilter);
        Assertions.assertEquals("population > 4000.0 AND population < 200000.0", CQL.toCQL(f));
        f = OskariWFSFilterFactory.getAttributeFilter(inclusiveFilter);
        Assertions.assertEquals("population <= 200000.0 AND population >= 4000.0", CQL.toCQL(f));
        f = OskariWFSFilterFactory.getAttributeFilter(notInFilter);
        Assertions.assertEquals("name <> 'Helsinki' AND name <> 'Tampere'", CQL.toCQL(f));
        f = OskariWFSFilterFactory.getAttributeFilter(inFilter);
        Assertions.assertEquals("name = 'Helsinki' OR name = 'Tampere'", CQL.toCQL(f));
        f = OskariWFSFilterFactory.getAttributeFilter(likeFilter);
        Assertions.assertEquals("name ILIKE 'Hels*'", CQL.toCQL(f));
        f = OskariWFSFilterFactory.getAttributeFilter(notLikeFilter);
        Assertions.assertEquals("NOT (name ILIKE 'Hels*')", CQL.toCQL(f));

        f = OskariWFSFilterFactory.getAttributeFilter(andFilter);
        String cqlAnd = CQL.toCQL(f);
        Assertions.assertEquals("type = 'city' AND (population > 20000.0)", cqlAnd);
        f = OskariWFSFilterFactory.getAttributeFilter(orFilter);
        String cqlOr = CQL.toCQL(f);
        Assertions.assertEquals("(name = 'Tampere') OR type = 'village'", cqlOr);

        f = OskariWFSFilterFactory.getAttributeFilter(andOrFlter);
        Assertions.assertEquals("(" + cqlAnd +") AND (" + cqlOr + ")", CQL.toCQL(f));
    }

    @Test
    public void equalFilters() throws Exception {
        JSONObject jsonFilter = new JSONObject(equalFilter);
        JSONObject propertyFilter = jsonFilter.getJSONObject("property");
        Filter f = OskariWFSFilterFactory.getAttributeFilter(jsonFilter);
        SimpleFeatureCollection sfc = fc.subCollection(f);
        Assertions.assertEquals(0, sfc.size());

        propertyFilter.remove("caseSensitive"); // defaults to false
        f = OskariWFSFilterFactory.getAttributeFilter(jsonFilter);
        sfc = fc.subCollection(f);
        Assertions.assertEquals(1, sfc.size());
        SimpleFeature feat =  sfc.features().next();
        Assertions.assertEquals("Helsinki", feat.getAttribute("name"));

        propertyFilter.put("key", "capital");
        propertyFilter.put("value", false);
        f = OskariWFSFilterFactory.getAttributeFilter(jsonFilter);
        sfc = fc.subCollection(f);
        Assertions.assertEquals(3, sfc.size());
        Assertions.assertFalse(sfc.contains(feat));

        propertyFilter.put("key", "population");
        propertyFilter.put("value", 600000);
        f = OskariWFSFilterFactory.getAttributeFilter(jsonFilter);
        sfc = fc.subCollection(f);
        Assertions.assertEquals(1, sfc.size());
        Assertions.assertTrue(sfc.contains(feat));
    }

    @Test
    public void rangeFilters() throws Exception {
        Filter f = OskariWFSFilterFactory.getAttributeFilter(rangeFilter);
        SimpleFeatureCollection sfc = fc.subCollection(f);
        Assertions.assertEquals(1, sfc.size());
        Assertions.assertEquals("Sipoo", sfc.features().next().getAttribute("name"));
        f = OskariWFSFilterFactory.getAttributeFilter(inclusiveFilter);
        sfc = fc.subCollection(f);
        Assertions.assertEquals(3, sfc.size());
        Assertions.assertFalse(getFeatureNames(sfc).contains("Helsinki"));
    }

    @Test
    public void orFilter() throws Exception {
        Filter f = OskariWFSFilterFactory.getAttributeFilter(orFilter);
        SimpleFeatureCollection sfc = fc.subCollection(f);
        Assertions.assertEquals(2, sfc.size());
        List<String> names = getFeatureNames(sfc);
        Assertions.assertTrue(names.contains("Tampere"));
        Assertions.assertTrue(names.contains("Puuppola"));
    }

    @Test
    public void andFilter() throws Exception {
        Filter f = OskariWFSFilterFactory.getAttributeFilter(andFilter);
        SimpleFeatureCollection sfc = fc.subCollection(f);
        Assertions.assertEquals(2, sfc.size());
        List<String> names = getFeatureNames(sfc);
        Assertions.assertTrue(names.contains("Tampere"));
        Assertions.assertTrue(names.contains("Helsinki"));
    }

    @Test
    public void andOrFilter() throws Exception {
        Filter f = OskariWFSFilterFactory.getAttributeFilter(andOrFlter);
        SimpleFeatureCollection sfc = fc.subCollection(f);
        Assertions.assertEquals(1, sfc.size());
        List<String> names = getFeatureNames(sfc);
        Assertions.assertTrue(names.contains("Tampere"));
    }

    @Test
    public void inFilters() throws Exception {
        Filter f = OskariWFSFilterFactory.getAttributeFilter(notInFilter);
        SimpleFeatureCollection sfcNot = fc.subCollection(f);
        f = OskariWFSFilterFactory.getAttributeFilter(inFilter);
        SimpleFeatureCollection sfcIn = fc.subCollection(f);

        Assertions.assertTrue(isDisjoint(sfcNot, sfcIn), "Subcollections should be disjoint sets");
        Assertions.assertEquals(fc.size(), sfcNot.size() + sfcIn.size());

        List<String> names = getFeatureNames(sfcNot);
        Assertions.assertEquals(2, names.size());
        Assertions.assertTrue(names.contains("Puuppola"));
        Assertions.assertTrue(names.contains("Sipoo"));
    }

    @Test
    public void likeFilters() throws Exception {
        Filter fLike = OskariWFSFilterFactory.getAttributeFilter(likeFilter);
        SimpleFeatureCollection sfcLike = fc.subCollection(fLike);
        Filter fNot = OskariWFSFilterFactory.getAttributeFilter(notLikeFilter);
        SimpleFeatureCollection sfcNot = fc.subCollection(fNot);

        Assertions.assertTrue(isDisjoint(sfcNot, sfcLike), "Subcollections should be disjoint sets");
        Assertions.assertEquals(fc.size(), sfcNot.size() + sfcLike.size());

        List<String> names = getFeatureNames(sfcLike);
        Assertions.assertEquals(1, names.size());
        Assertions.assertTrue(names.contains("Helsinki"));

        JSONObject jsonFilter = new JSONObject(likeFilter);
        JSONObject propertyFilter = jsonFilter.getJSONObject("property");
        propertyFilter.put("like", "*po*");
        fLike = OskariWFSFilterFactory.getAttributeFilter(jsonFilter);
        sfcLike = fc.subCollection(fLike);
        names = getFeatureNames(sfcLike);
        Assertions.assertEquals(2, sfcLike.size());
        Assertions.assertTrue(names.contains("Puuppola"));
        Assertions.assertTrue(names.contains("Sipoo"));

        propertyFilter.put("like", "hels?nki");
        fLike = OskariWFSFilterFactory.getAttributeFilter(jsonFilter);
        sfcLike = fc.subCollection(fLike);
        Assertions.assertEquals(1, sfcLike.size());
        names = getFeatureNames(sfcLike);
        Assertions.assertTrue(names.contains("Helsinki"));
    }

    private List<String> getFeatureNames (SimpleFeatureCollection sfc) {
        return Arrays.stream(sfc.toArray())
                .map(feat -> ((SimpleFeature) feat).getAttribute("name").toString())
                .collect(Collectors.toList());
    }

    private boolean isDisjoint (SimpleFeatureCollection sfc1, SimpleFeatureCollection sfc2) {
        SimpleFeatureIterator iter = sfc1.features();
        while(iter.hasNext()) {
            if (sfc2.contains(iter.next())) {
                return false;
            }
        }
        return true;
    }
}
