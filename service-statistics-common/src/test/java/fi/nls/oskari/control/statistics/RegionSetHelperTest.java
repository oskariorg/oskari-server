package fi.nls.oskari.control.statistics;

import fi.nls.oskari.control.statistics.db.RegionSet;
import fi.nls.oskari.service.ServiceException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.geotools.api.geometry.MismatchedDimensionException;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.operation.TransformException;
import org.oskari.geojson.GeoJSON;

import java.io.IOException;
import java.util.List;

public class RegionSetHelperTest {

    @Test
    @Disabled("Requires HTTP connection to a WFS server serving specific FeatureType")
    public void testGeoServerWFSWorks() throws MismatchedDimensionException, FactoryException, TransformException, ServiceException, IOException, JSONException {
        String endPoint = "http://localhost:8081/geoserver/wfs";
        RegionSet kunnatWFS = new RegionSet();
        kunnatWFS.setId(-1);
        kunnatWFS.setName("oskari:kunnat2013");
        kunnatWFS.setSrs_name("EPSG:3067");
        kunnatWFS.setAttributes(getAttributes("kuntakoodi", "kuntanimi", endPoint));
        List<Region> regions = RegionSetHelper.getRegions(kunnatWFS, "EPSG:3067");
        Assertions.assertEquals(320, regions.size());
        for (Region region : regions) {
            if ("082".equals(region.getCode())) {
                Assertions.assertEquals("Hattula", region.getName());
            } else if ("091".equals(region.getCode())) {
                Assertions.assertEquals("Helsinki", region.getName());
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
        Assertions.assertEquals(320, regions.size());
        for (Region region : regions) {
            if ("082".equals(region.getCode())) {
                Assertions.assertEquals("Hattula", region.getName());
                JSONObject geoJSON = region.getGeojson();
                Assertions.assertEquals(GeoJSON.FEATURE, geoJSON.getString(GeoJSON.TYPE));
                JSONObject geometry = geoJSON.getJSONObject(GeoJSON.GEOMETRY);
                Assertions.assertNotNull(geometry);
                JSONObject properties = region.getGeojson().getJSONObject(GeoJSON.PROPERTIES);
                Assertions.assertEquals("082", properties.get(Region.KEY_CODE));
                Assertions.assertEquals("Hattula", properties.get(Region.KEY_NAME));
            } else if ("091".equals(region.getCode())) {
                Assertions.assertEquals("Helsinki", region.getName());
                JSONObject geoJSON = region.getGeojson();
                Assertions.assertEquals(GeoJSON.FEATURE, geoJSON.getString(GeoJSON.TYPE));
                JSONObject geometry = geoJSON.getJSONObject(GeoJSON.GEOMETRY);
                Assertions.assertNotNull(geometry);
                JSONObject properties = geoJSON.getJSONObject(GeoJSON.PROPERTIES);
                Assertions.assertEquals("091", properties.get(Region.KEY_CODE));
                Assertions.assertEquals("Helsinki", properties.get(Region.KEY_NAME));
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
        Assertions.assertEquals(16, regions.size());
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
        Assertions.assertEquals(endPoint, kunnatJSON.getFeaturesUrl(), "Should return url when attributes NOT defined");

        kunnatJSON.setAttributes(getAttributes("kuntakoodi", "kuntanimi", overridingEndPoint));
        Assertions.assertEquals(overridingEndPoint, kunnatJSON.getFeaturesUrl(), "Should return features url when attributes ARE defined");

        overridingEndPoint = null;
        kunnatJSON.setAttributes(getAttributes("kuntakoodi", "kuntanimi", overridingEndPoint));
        Assertions.assertEquals(endPoint, kunnatJSON.getFeaturesUrl(), "Should return url when attributes ARE defined WITHOUT features url");
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
