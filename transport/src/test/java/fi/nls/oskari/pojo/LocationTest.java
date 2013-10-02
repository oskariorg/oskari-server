package fi.nls.oskari.pojo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.BeforeClass;
import org.junit.Test;

public class LocationTest {
	private static Location location;
	
    @BeforeClass
    public static void setUp() {
		location = new Location("EPSG:3067");
		List<Double> bbox = new ArrayList<Double>();
		bbox.add(420893.0);
		bbox.add(7177728.0);
		bbox.add(420893.0);
		bbox.add(7177728.0);
		location.setBbox(bbox);
    }
    
	@Test
	public void testEnvelope() {
		// normal envelope creation
		ReferencedEnvelope envelope = location.getEnvelope();
		assertTrue("should get valid envelope", (envelope != null));
	}
	
	@Test
	public void testTransformEnvelope() {
		// transformed envelope
		ReferencedEnvelope transformed = location.getTransformEnvelope("EPSG:4326", true);
		assertTrue("should get transformed coordinates", 
				(transformed.getMinY() == 25.3399808304302 && 
				transformed.getMinX() == 64.71499289327947));
	}

}
