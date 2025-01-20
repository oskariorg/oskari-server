package org.oskari.service.wfs.client;

import fi.nls.oskari.util.JSONHelper;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.filter.Filter;

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

    @Before
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
        Assert.assertEquals("name = 'helsinki'", CQL.toCQL(f));
        f = OskariWFSFilterFactory.getAttributeFilter(rangeFilter);
        Assert.assertEquals("population > 4000.0 AND population < 200000.0", CQL.toCQL(f));
        f = OskariWFSFilterFactory.getAttributeFilter(inclusiveFilter);
        Assert.assertEquals("population <= 200000.0 AND population >= 4000.0", CQL.toCQL(f));
        f = OskariWFSFilterFactory.getAttributeFilter(notInFilter);
        Assert.assertEquals("name <> 'Helsinki' AND name <> 'Tampere'", CQL.toCQL(f));
        f = OskariWFSFilterFactory.getAttributeFilter(inFilter);
        Assert.assertEquals("name = 'Helsinki' OR name = 'Tampere'", CQL.toCQL(f));
        f = OskariWFSFilterFactory.getAttributeFilter(likeFilter);
        Assert.assertEquals("name ILIKE 'Hels*'", CQL.toCQL(f));
        f = OskariWFSFilterFactory.getAttributeFilter(notLikeFilter);
        Assert.assertEquals("NOT (name ILIKE 'Hels*')", CQL.toCQL(f));

        f = OskariWFSFilterFactory.getAttributeFilter(andFilter);
        String cqlAnd = CQL.toCQL(f);
        Assert.assertEquals("type = 'city' AND (population > 20000.0)", cqlAnd);
        f = OskariWFSFilterFactory.getAttributeFilter(orFilter);
        String cqlOr = CQL.toCQL(f);
        Assert.assertEquals("(name = 'Tampere') OR type = 'village'", cqlOr);

        f = OskariWFSFilterFactory.getAttributeFilter(andOrFlter);
        Assert.assertEquals("(" + cqlAnd +") AND (" + cqlOr + ")", CQL.toCQL(f));
    }

    @Test
    public void equalFilters() throws Exception {
        JSONObject jsonFilter = new JSONObject(equalFilter);
        JSONObject propertyFilter = jsonFilter.getJSONObject("property");
        Filter f = OskariWFSFilterFactory.getAttributeFilter(jsonFilter);
        SimpleFeatureCollection sfc = fc.subCollection(f);
        Assert.assertEquals(0, sfc.size());

        propertyFilter.remove("caseSensitive"); // defaults to false
        f = OskariWFSFilterFactory.getAttributeFilter(jsonFilter);
        sfc = fc.subCollection(f);
        Assert.assertEquals(1, sfc.size());
        SimpleFeature feat =  sfc.features().next();
        Assert.assertEquals("Helsinki", feat.getAttribute("name"));

        propertyFilter.put("key", "capital");
        propertyFilter.put("value", false);
        f = OskariWFSFilterFactory.getAttributeFilter(jsonFilter);
        sfc = fc.subCollection(f);
        Assert.assertEquals(3, sfc.size());
        Assert.assertFalse(sfc.contains(feat));

        propertyFilter.put("key", "population");
        propertyFilter.put("value", 600000);
        f = OskariWFSFilterFactory.getAttributeFilter(jsonFilter);
        sfc = fc.subCollection(f);
        Assert.assertEquals(1, sfc.size());
        Assert.assertTrue(sfc.contains(feat));
    }

    @Test
    public void rangeFilters() throws Exception {
        Filter f = OskariWFSFilterFactory.getAttributeFilter(rangeFilter);
        SimpleFeatureCollection sfc = fc.subCollection(f);
        Assert.assertEquals(1, sfc.size());
        Assert.assertEquals("Sipoo", sfc.features().next().getAttribute("name"));
        f = OskariWFSFilterFactory.getAttributeFilter(inclusiveFilter);
        sfc = fc.subCollection(f);
        Assert.assertEquals(3, sfc.size());
        Assert.assertFalse(getFeatureNames(sfc).contains("Helsinki"));
    }

    @Test
    public void orFilter() throws Exception {
        Filter f = OskariWFSFilterFactory.getAttributeFilter(orFilter);
        SimpleFeatureCollection sfc = fc.subCollection(f);
        Assert.assertEquals(2, sfc.size());
        List<String> names = getFeatureNames(sfc);
        Assert.assertTrue(names.contains("Tampere"));
        Assert.assertTrue(names.contains("Puuppola"));
    }

    @Test
    public void andFilter() throws Exception {
        Filter f = OskariWFSFilterFactory.getAttributeFilter(andFilter);
        SimpleFeatureCollection sfc = fc.subCollection(f);
        Assert.assertEquals(2, sfc.size());
        List<String> names = getFeatureNames(sfc);
        Assert.assertTrue(names.contains("Tampere"));
        Assert.assertTrue(names.contains("Helsinki"));
    }

    @Test
    public void andOrFilter() throws Exception {
        Filter f = OskariWFSFilterFactory.getAttributeFilter(andOrFlter);
        SimpleFeatureCollection sfc = fc.subCollection(f);
        Assert.assertEquals(1, sfc.size());
        List<String> names = getFeatureNames(sfc);
        Assert.assertTrue(names.contains("Tampere"));
    }

    @Test
    public void inFilters() throws Exception {
        Filter f = OskariWFSFilterFactory.getAttributeFilter(notInFilter);
        SimpleFeatureCollection sfcNot = fc.subCollection(f);
        f = OskariWFSFilterFactory.getAttributeFilter(inFilter);
        SimpleFeatureCollection sfcIn = fc.subCollection(f);

        Assert.assertTrue("Subcollections should be disjoint sets", isDisjoint(sfcNot, sfcIn));
        Assert.assertEquals(fc.size(), sfcNot.size() + sfcIn.size());

        List<String> names = getFeatureNames(sfcNot);
        Assert.assertEquals(2, names.size());
        Assert.assertTrue(names.contains("Puuppola"));
        Assert.assertTrue(names.contains("Sipoo"));
    }

    @Test
    public void likeFilters() throws Exception {
        Filter fLike = OskariWFSFilterFactory.getAttributeFilter(likeFilter);
        SimpleFeatureCollection sfcLike = fc.subCollection(fLike);
        Filter fNot = OskariWFSFilterFactory.getAttributeFilter(notLikeFilter);
        SimpleFeatureCollection sfcNot = fc.subCollection(fNot);

        Assert.assertTrue("Subcollections should be disjoint sets", isDisjoint(sfcNot, sfcLike));
        Assert.assertEquals(fc.size(), sfcNot.size() + sfcLike.size());

        List<String> names = getFeatureNames(sfcLike);
        Assert.assertEquals(1, names.size());
        Assert.assertTrue(names.contains("Helsinki"));

        JSONObject jsonFilter = new JSONObject(likeFilter);
        JSONObject propertyFilter = jsonFilter.getJSONObject("property");
        propertyFilter.put("like", "*po*");
        fLike = OskariWFSFilterFactory.getAttributeFilter(jsonFilter);
        sfcLike = fc.subCollection(fLike);
        names = getFeatureNames(sfcLike);
        Assert.assertEquals(2, sfcLike.size());
        Assert.assertTrue(names.contains("Puuppola"));
        Assert.assertTrue(names.contains("Sipoo"));

        propertyFilter.put("like", "hels?nki");
        fLike = OskariWFSFilterFactory.getAttributeFilter(jsonFilter);
        sfcLike = fc.subCollection(fLike);
        Assert.assertEquals(1, sfcLike.size());
        names = getFeatureNames(sfcLike);
        Assert.assertTrue(names.contains("Helsinki"));
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
