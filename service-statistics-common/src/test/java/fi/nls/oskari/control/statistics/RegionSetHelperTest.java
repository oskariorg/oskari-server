package fi.nls.oskari.control.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.oskari.geojson.GeoJSON;

import fi.nls.oskari.control.statistics.db.RegionSet;
import fi.nls.oskari.service.ServiceException;

public class RegionSetHelperTest {

    @Test
    @Ignore("Requires HTTP connection to a WFS server serving specific FeatureType")
    public void testGeoServerWFSWorks() throws MismatchedDimensionException, FactoryException, TransformException, ServiceException, IOException, JSONException {
        String endPoint = "http://localhost:8081/geoserver/wfs";
        RegionSet kunnatWFS = new RegionSet();
        kunnatWFS.setId(-1);
        kunnatWFS.setName("oskari:kunnat2013");
        kunnatWFS.setSrs_name("EPSG:3067");
        kunnatWFS.setAttributes(getAttributes("kuntakoodi", "kuntanimi", endPoint));
        List<Region> regions = RegionSetHelper.getRegions(kunnatWFS, "EPSG:3067");
        assertEquals(320, regions.size());
        for (Region region : regions) {
            if ("082".equals(region.getCode())) {
                assertEquals("Hattula", region.getName());
            } else if ("091".equals(region.getCode())) {
                assertEquals("Helsinki", region.getName());
            }
        }
    }

    @Test
    public void testGeoJSONResourceFileWorks() throws MismatchedDimensionException, FactoryException, TransformException, ServiceException, IOException, JSONException {
        String endPoint = "resources://kunnat2013.json";
        RegionSet kunnatJSON = new RegionSet();
        kunnatJSON.setId(-1);
        kunnatJSON.setName("oskari:kunnat2013");
        kunnatJSON.setSrs_name("EPSG:3067");
        kunnatJSON.setAttributes(getAttributes("kuntakoodi", "kuntanimi", endPoint));
        List<Region> regions = RegionSetHelper.getRegions(kunnatJSON, "EPSG:3067");
        assertEquals(320, regions.size());
        for (Region region : regions) {
            if ("082".equals(region.getCode())) {
                assertEquals("Hattula", region.getName());
                JSONObject geoJSON = region.getGeojson();
                assertEquals(GeoJSON.FEATURE, geoJSON.getString(GeoJSON.TYPE));
                JSONObject geometry = geoJSON.getJSONObject(GeoJSON.GEOMETRY);
                assertNotNull(geometry);
                JSONObject properties = region.getGeojson().getJSONObject(GeoJSON.PROPERTIES);
                assertEquals("082", properties.get(Region.KEY_CODE));
                assertEquals("Hattula", properties.get(Region.KEY_NAME));
            } else if ("091".equals(region.getCode())) {
                assertEquals("Helsinki", region.getName());
                JSONObject geoJSON = region.getGeojson();
                assertEquals(GeoJSON.FEATURE, geoJSON.getString(GeoJSON.TYPE));
                JSONObject geometry = geoJSON.getJSONObject(GeoJSON.GEOMETRY);
                assertNotNull(geometry);
                JSONObject properties = geoJSON.getJSONObject(GeoJSON.PROPERTIES);
                assertEquals("091", properties.get(Region.KEY_CODE));
                assertEquals("Helsinki", properties.get(Region.KEY_NAME));
            }
        }
    }

    @Test
    public void testDuplicatedRegions() throws MismatchedDimensionException, FactoryException, TransformException, ServiceException, IOException, JSONException {
        String endPoint = "resources://ely4500k.json";
        RegionSet elyJson = new RegionSet();
        elyJson.setId(-1);
        elyJson.setName("oskari:ely4500k");
        elyJson.setSrs_name("EPSG:3067");
        elyJson.setAttributes(getAttributes("ely", "nimi", endPoint));
        List<Region> regions = RegionSetHelper.getRegions(elyJson, "EPSG:3067");
        assertEquals(16, regions.size());
    }

    @Test
    public void testFeaturesUrl() throws MismatchedDimensionException, FactoryException, TransformException, ServiceException, IOException, JSONException {
        String endPoint = "https://my.domain";
        String overridingEndPoint = endPoint + "/feat";
        RegionSet kunnatJSON = new RegionSet();
        kunnatJSON.setId(-1);
        kunnatJSON.setUrl(endPoint);
        kunnatJSON.setName("oskari:kunnat2013");
        kunnatJSON.setSrs_name("EPSG:3067");
        assertEquals("Should return url when attributes NOT defined", endPoint, kunnatJSON.getFeaturesUrl());

        kunnatJSON.setAttributes(getAttributes("kuntakoodi", "kuntanimi", overridingEndPoint));
        assertEquals("Should return features url when attributes ARE defined", overridingEndPoint, kunnatJSON.getFeaturesUrl());

        overridingEndPoint = null;
        kunnatJSON.setAttributes(getAttributes("kuntakoodi", "kuntanimi", overridingEndPoint));
        assertEquals("Should return url when attributes ARE defined WITHOUT features url", endPoint, kunnatJSON.getFeaturesUrl());
    }

    private String getAttributes(String regionIdTag, String nameIdTag, String featuresUrl) throws JSONException {
        JSONObject attributes = new JSONObject();
        JSONObject statistics = new JSONObject();
        statistics.put("regionIdTag", regionIdTag);
        statistics.put("nameIdTag", nameIdTag);
        if (featuresUrl != null) {
            statistics.put("featuresUrl", featuresUrl);
        }
        attributes.put("statistics", statistics);
        return attributes.toString();
    }

}
