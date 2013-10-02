package fi.nls.oskari.pojo;

import static org.junit.Assert.*;

import org.junit.Test;

public class FilterTest {
    private String successJson = "{\"data\":{\"filter\":{\"geojson\":{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[373453.32214355,6676142],[374803.32214355,6681592],[369353.32214355,6682942],[368003.32214355,6677492],[373453.32214355,6676142]]]}}],\"crs\":{\"type\":\"EPSG\",\"properties\":{\"code\":3067}}}} } }";
    private String failJson = "{\"filter\":{\"geojson\":{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[373453.32214355,6676142],[374803.32214355,6681592],[369353.32214355,6682942],[368003.32214355,6677492],[373453.32214355,6676142]]]}}],\"crs\":{\"type\":\"EPSG\",\"properties\":{\"code\":3067}}}} }";
    private String epicFailJson = "{\"data\":{ \"geojson\":{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[373453.32214355,6676142],[374803.32214355,6681592],[369353.32214355,6682942],[368003.32214355,6677492],[373453.32214355,6676142]]]}}],\"crs\":{\"type\":\"EPSG\",\"properties\":{\"code\":3067}}} } }";
    
    @Test
	public void testSetParamsJSONSuccess() {
		Filter filterSuccess = Filter.setParamsJSON(successJson);
		assertTrue("There should be features", filterSuccess.getFeatures() != null && filterSuccess.getFeatures().length() > 0);
	}

    @Test
	public void testSetParamsJSONFail() {
		Filter filterFail = Filter.setParamsJSON(failJson);
		assertTrue("There should not be features", filterFail.getFeatures() == null);
	}

    @Test
	public void testSetParamsJSONEpicFail() {
		Filter filterEpicFail = Filter.setParamsJSON(epicFailJson);
		assertTrue("There should not be filter", filterEpicFail == null);
	}

}
