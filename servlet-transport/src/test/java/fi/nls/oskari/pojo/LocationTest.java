package fi.nls.oskari.pojo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.BeforeClass;
import org.junit.Test;


public class LocationTest {
	private static Location location;
    private static List<Double> bbox;
	
    @BeforeClass
    public static void setUp() {
		location = new Location("EPSG:3067");
		bbox = new ArrayList<Double>();
		bbox.add(420893.0);
		bbox.add(7177728.0);
		bbox.add(420896.0);
		bbox.add(7177730.0);
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
        assertEquals("MinY is within acceptable range ", 64.71499289327947, transformed.getMinY(), 0.00001);
        assertEquals("MinX is within acceptable range ", 25.3399808304302, transformed.getMinX(), 0.00001);
    }

    @Test
    public void testScaledEnvelope() {
        ReferencedEnvelope scaled = location.getScaledEnvelope(1.0);
        assertEquals("should get the same envelope width ", location.getEnvelope().getWidth(), scaled.getWidth(), 0);
        assertEquals("should get the same envelope height ", location.getEnvelope().getHeight(), scaled.getHeight(), 0);

        scaled = location.getScaledEnvelope(2.0);
        assertEquals("should get double sized envelope width ", location.getEnvelope().getWidth()*2, scaled.getWidth(), 0);
        assertEquals("should get double sized envelope height ", location.getEnvelope().getHeight()*2, scaled.getHeight(), 0);
    }

    @Test
    public void testEnlargedEnvelope() {
        location.setEnlargedEnvelope(bbox);
        ReferencedEnvelope scaled = location.getEnlargedEnvelope();

        assertEquals("should get double sized envelope width ", 9, scaled.getWidth(), 0);
        assertEquals("should get double sized envelope height ", 6, scaled.getHeight(), 0);
    }
}
