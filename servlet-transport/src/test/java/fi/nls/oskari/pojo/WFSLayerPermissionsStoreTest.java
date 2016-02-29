package fi.nls.oskari.pojo;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class WFSLayerPermissionsStoreTest {
	private static WFSLayerPermissionsStore store;
	private static List<String> layerIds;

	String jsonResult = "{\"layerIds\":[\"216\",\"218\"]}";
	String jsonFail = "{\"layerIds\"[216,218]}";
	
    @BeforeClass
    public static void setUp() {
    	store = new WFSLayerPermissionsStore();
		layerIds = new ArrayList<String>();
		layerIds.add("216");
		layerIds.add("218");
		store.setLayerIds(layerIds);
    }
   
	@Test
	public void testJSON() throws IOException {
		String json = store.getAsJSON();
		assertTrue("should get layerIds array [\"216\",\"218\"]", (json.equals(jsonResult)));
	}		
	
	@Test
	public void testJSONContains() throws IOException {
		store = WFSLayerPermissionsStore.setJSON(jsonResult);
		layerIds = store.getLayerIds();
		assertTrue("should contain 216", layerIds.contains("216"));
		assertTrue("should contain 218", layerIds.contains("218"));
	}	
	
	@Test(expected=IOException.class)
	public void testJSONIOException() throws IOException {
		WFSLayerPermissionsStore.setJSON(jsonFail);
	}	
	
}
