package fi.nls.oskari.pojo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.hamcrest.Matchers;
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
        System.out.println(transformed.getMinX());
        System.out.println(transformed.getMinY());
        assertThat("MinX greater bound", 64.71499289327947 - 64.71499289327947 * 0.00001, Matchers.lessThanOrEqualTo(transformed.getMinX()));
        assertThat("MinX lower bound", 64.71499289327947 * 1.00001, Matchers.greaterThanOrEqualTo(transformed.getMinX()));
        assertThat("MinY greater bound", 25.3399808304302 - 25.3399808304302 * 0.00001, Matchers.lessThanOrEqualTo(transformed.getMinY()));
        assertThat("MinY lower bound", 25.3399808304302 * 1.00001, Matchers.greaterThanOrEqualTo(transformed.getMinY()));
/*
		assertTrue("should get transformed coordinates",
                (transformed.getMinX() >= 64.71499289327947 - 64.71499289327947 * 0.00001 &&
                        transformed.getMinX() <= 64.71499289327947 * 1.00001 &&
                        transformed.getMinY() >= 25.3399808304302 - 25.3399808304302 * 0.00001 &&
                        transformed.getMinY() <= 25.3399808304302 * 1.00001));
                        */
    }

    @Test
    public void testScaledEnvelope() {
        ReferencedEnvelope scaled = location.getScaledEnvelope(1.0);
        assertTrue("should get the same envelope",
                (scaled.getWidth() == location.getEnvelope().getWidth() && scaled.getHeight() == location.getEnvelope().getHeight()));
        scaled = location.getScaledEnvelope(2.0);
        assertTrue("should get double sized envelope",
                (scaled.getWidth() == location.getEnvelope().getWidth()*2 && scaled.getHeight() == location.getEnvelope().getHeight()*2));
    }

    @Test
    public void testEnlargedEnvelope() {
        location.setEnlargedEnvelope(bbox);
        ReferencedEnvelope scaled = location.getEnlargedEnvelope();
        System.out.println(scaled.getWidth());
        System.out.println(scaled.getHeight());

        assertTrue("should get double sized envelope",
                (scaled.getWidth() == 9 && scaled.getHeight() == 6));
    }
}
