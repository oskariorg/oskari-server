package org.oskari.service.wfs.client;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.Filters;
import org.geotools.filter.text.cql2.CQL;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
            "        \"value\": 20000\n" +
            "    }, {\n" +
            "        \"key\": \"name\",\n" +
            "        \"notIn\": [\"Helsinki\"]\n" +
            "    }]\n" +
            "}";
    String orFilter = "{\n" +
            "    \"OR\": [{\n" +
            "        \"key\": \"name\",\n" +
            "        \"in\": [\"Helsinki\", \"Tampere\"]\n" +
            "    }, {\n" +
            "        \"key\": \"type\",\n" +
            "        \"value\": \"village\"\n" +
            "    }]\n" +
            "}";
    String rangeFilter = "{\n" +
            "    \"property\": {\n" +
            "        \"key\": \"population\",\n" +
            "        \"greaterThan\": 20000,\n" +
            "        \"atMost\": 200000\n" +
            "    }\n" +
            "}";
    String notInFilter = "{\n" +
            "    \"property\": {\n" +
            "        \"key\": \"name\",\n" +
            "        \"notIn\": [\"Helsinki\", \"Tampere\"]\n" +
            "    }\n" +
            "}";
    String likeFilter = "{\n" +
            "    \"property\": {\n" +
            "        \"key\": \"name\",\n" +
            "        \"like\": [\"Hels*\"]\n" +
            "    }\n" +
            "}";
    @Before
    public void init() {
        tb.setName("test");
        tb.add("name", String.class);
        tb.add("type", String.class);
        tb.add("population", Integer.class);
        fb = new SimpleFeatureBuilder(tb.buildFeatureType());

        fc = new DefaultFeatureCollection();
        fb.addAll(Arrays.asList("Helsinki", "city", 600000));
        fc.add(fb.buildFeature("fid.1"));
        fb.addAll(Arrays.asList("Sipoo", "town", 20000));
        fc.add(fb.buildFeature("fid.2"));
        fb.addAll(Arrays.asList("Tampere", "city", 200000));
        fc.add(fb.buildFeature("fid.3"));
        fb.addAll(Arrays.asList("Puuppola", "village", 4000));
        fc.add(fb.buildFeature("fid.4"));
    }
    @Test
    public void cqlTest() throws Exception {
        Filter f = OskariWFSFilter.getAttributeFilter(equalFilter);
        Assert.assertEquals("name = 'helsinki'", CQL.toCQL(f));
        f = OskariWFSFilter.getAttributeFilter(andFilter);
        Assert.assertEquals("type = 'city' AND population = '20000' AND (name <> 'Helsinki')", CQL.toCQL(f));
        f = OskariWFSFilter.getAttributeFilter(orFilter);
        Assert.assertEquals("(name = 'Helsinki' OR name = 'Tampere') OR type = 'village'", CQL.toCQL(f));
        f = OskariWFSFilter.getAttributeFilter(rangeFilter);
        Assert.assertEquals("population > 20000.0 AND population <= 200000.0", CQL.toCQL(f));
        f = OskariWFSFilter.getAttributeFilter(notInFilter);
        Assert.assertEquals("name <> 'Helsinki' AND name <> 'Tampere'", CQL.toCQL(f));
        f = OskariWFSFilter.getAttributeFilter(likeFilter);
        Assert.assertEquals("name ILIKE 'Hels*'", CQL.toCQL(f));
    }

    @Test
    public void equalFilter() throws Exception {
        JSONObject jsonFilter = new JSONObject(equalFilter);
        Filter f = OskariWFSFilter.getAttributeFilter(jsonFilter);
        SimpleFeatureCollection sfc = fc.subCollection(f);
        Assert.assertEquals(0, sfc.size());
        jsonFilter.getJSONObject("property").remove("caseSensitive"); // defaults to false
        f = OskariWFSFilter.getAttributeFilter(jsonFilter);
        sfc = fc.subCollection(f);
        Assert.assertEquals(1, sfc.size());
        Assert.assertEquals("Helsinki", sfc.features().next().getAttribute("name"));
    }
    @Test
    public void rangeFilter() throws Exception {
        Filter f = OskariWFSFilter.getAttributeFilter(rangeFilter);
        SimpleFeatureCollection sfc = fc.subCollection(f);
        Assert.assertEquals(1, sfc.size());
        Assert.assertEquals("Tampere", sfc.features().next().getAttribute("name"));
    }
    @Test
    public void orFilter() throws Exception {
        Filter f = OskariWFSFilter.getAttributeFilter(orFilter);
        SimpleFeatureCollection sfc = fc.subCollection(f);
        Assert.assertEquals(3, sfc.size());
        Optional<SimpleFeature> sipoo = Arrays.stream(sfc.toArray())
                .map(feat -> (SimpleFeature) feat)
                .filter(feat -> "Sipoo".equals(feat.getAttribute("name")))
                .findAny();
        List<String> names = Arrays.stream(sfc.toArray())
                .map(feat -> ((SimpleFeature) feat).getAttribute("name").toString())
                .collect(Collectors.toList());
        Assert.assertTrue(names.contains("Helsinki"));
        Assert.assertFalse(names.contains("Sipoo"));
        Assert.assertFalse(sipoo.isPresent());
    }
    @Test
    public void andFilter() throws Exception {
        Filter f = OskariWFSFilter.getAttributeFilter(orFilter);
        SimpleFeatureCollection sfc = fc.subCollection(f);
        Assert.assertEquals(3, sfc.size());
        List<String> names = getFeatureNames(sfc);
        Assert.assertTrue(names.contains("Helsinki"));
        Assert.assertFalse(names.contains("Sipoo"));
    }
    @Test
    public void notInFilter() throws Exception {
        Filter f = OskariWFSFilter.getAttributeFilter(notInFilter);
        SimpleFeatureCollection sfc = fc.subCollection(f);
        Assert.assertEquals(2, sfc.size());
        List<String> names = getFeatureNames(sfc);
        Assert.assertTrue(names.contains("Puuppola"));
        Assert.assertTrue(names.contains("Sipoo"));
    }
    @Test
    public void likeFilter() throws Exception {
        Filter f = OskariWFSFilter.getAttributeFilter(likeFilter);
        SimpleFeatureCollection sfc = fc.subCollection(f);
        Assert.assertEquals(1, sfc.size());
        List<String> names = getFeatureNames(sfc);
        Assert.assertTrue(names.contains("Helsinki"));
    }
    private List<String> getFeatureNames (SimpleFeatureCollection sfc) {
        return Arrays.stream(sfc.toArray())
                .map(feat -> ((SimpleFeature) feat).getAttribute("name").toString())
                .collect(Collectors.toList());
    }
}
