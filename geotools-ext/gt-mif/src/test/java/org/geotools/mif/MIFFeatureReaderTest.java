package org.geotools.mif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;

import org.geotools.api.data.DataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.api.data.SimpleFeatureSource;
import org.junit.Test;
import org.geotools.api.feature.simple.SimpleFeature;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

public class MIFFeatureReaderTest {

    @Test
    public void testReadingEmptyMIDFields() throws URISyntaxException, IOException {
        try {
            File mif = new File(getClass().getResource("empty_fields.MIF").toURI());
            File mid = new File(getClass().getResource("empty_fields.MID").toURI());
            DataStore store = new MIFDataStore(mif, mid);
            SimpleFeatureSource source = store.getFeatureSource("empty_fields");
            SimpleFeatureCollection fc = source.getFeatures();
            assertEquals(1, fc.size());
            try (SimpleFeatureIterator it = fc.features()) {
                if (!it.hasNext()) {
                    fail();
                }
                SimpleFeature f = it.next();

                /*
                Region 1
                4
                357517.2 6860602.8
                357539.1 6860613.8
                357556.1 6860578.1
                357533.8 6860567.8
                 */
                Polygon region = (Polygon) f.getDefaultGeometry();
                assertEquals(5, region.getNumPoints()); // +1 because ring is automatically closed
                assertEquals(0, region.getNumInteriorRing());

                CoordinateSequence csq = region.getExteriorRing().getCoordinateSequence();
                assertEquals( 357517.2, csq.getOrdinate(0, 0), 1e-9);
                assertEquals(6860602.8, csq.getOrdinate(0, 1), 1e-9);
                assertEquals( 357539.1, csq.getOrdinate(1, 0), 1e-9);
                assertEquals(6860613.8, csq.getOrdinate(1, 1), 1e-9);
                assertEquals( 357556.1, csq.getOrdinate(2, 0), 1e-9);
                assertEquals(6860578.1, csq.getOrdinate(2, 1), 1e-9);
                assertEquals( 357533.8, csq.getOrdinate(3, 0), 1e-9);
                assertEquals(6860567.8, csq.getOrdinate(3, 1), 1e-9);
                assertEquals( 357517.2, csq.getOrdinate(4, 0), 1e-9);
                assertEquals(6860602.8, csq.getOrdinate(4, 1), 1e-9);

                assertNull(f.getAttribute("id"));
                assertEquals(Integer.class, f.getProperty("id").getType().getBinding());

                assertNull(f.getAttribute("foo"));
                assertEquals(Long.class, f.getProperty("foo").getType().getBinding());

                assertNull(f.getAttribute("bar"));
                assertEquals(Float.class, f.getProperty("bar").getType().getBinding());

                assertNull(f.getAttribute("baz"));
                assertEquals(Double.class, f.getProperty("baz").getType().getBinding());

                assertNull(f.getAttribute("qux"));
                assertEquals(Boolean.class, f.getProperty("qux").getType().getBinding());

                if (it.hasNext()) {
                    fail();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFeatureReader() throws URISyntaxException, IOException {
        File mif = new File(getClass().getResource("kenro_alue_maarajat.MIF").toURI());
        File mid = new File(getClass().getResource("kenro_alue_maarajat.MID").toURI());
        DataStore store = new MIFDataStore(mif, mid);
        SimpleFeatureSource source = store.getFeatureSource("kenro_alue_maarajat");
        SimpleFeatureCollection fc = source.getFeatures();
        assertEquals(25, fc.size());
        try (SimpleFeatureIterator it = fc.features()) {
            if (!it.hasNext()) {
                fail();
            }
            SimpleFeature f = it.next();
            // Region  1
            //   5615
            // 3644472.125 7289630.034
            // 3644097.725 7293078.173
            // 0,0,"516","Lappi","Lappland","http://www.kela.fi","",
            Polygon region = (Polygon) f.getDefaultGeometry();
            assertEquals(5615, region.getNumPoints());
            assertEquals(0, region.getNumInteriorRing());

            CoordinateSequence csq = region.getExteriorRing().getCoordinateSequence();
            assertEquals(3644472.125, csq.getOrdinate(0, 0), 1e-9);
            assertEquals(7289630.034, csq.getOrdinate(0, 1), 1e-9);
            assertEquals(3644097.725, csq.getOrdinate(1, 0), 1e-9);
            assertEquals(7293078.173, csq.getOrdinate(1, 1), 1e-9);

            assertEquals(0, (long) f.getAttribute("id"));
            assertEquals(0, (long) f.getAttribute("aineisto_id"));
            assertEquals("516", f.getAttribute("aluekoodi"));
            assertEquals("Lappi", f.getAttribute("nimi"));
            assertEquals("Lappland", f.getAttribute("nimi_se"));
            assertEquals("http://www.kela.fi", f.getAttribute("www_osoite"));
            assertEquals("", f.getAttribute("modify_user"));
            assertEquals(null, f.getAttribute("modify_time"));
            assertEquals(LocalDate.class, f.getProperty("modify_time").getType().getBinding());

            // Move to last feature
            while (it.hasNext()) {
                f = it.next();
            }

            // Region  2
            //   49
            // 3380768.212 6679512.723
            // 3381892.004 6677581.001
            // ...
            //   5
            // 3383004.736 6672746.505
            // 3382883.26 6675036.999
            // 0,0,"980","Helsinki","Helsingfors","http://www.kela.fi","",
            MultiPolygon helsinkiRegion = (MultiPolygon) f.getDefaultGeometry();
            assertEquals(54, helsinkiRegion.getNumPoints());
            assertEquals(2, helsinkiRegion.getNumGeometries());

            region = (Polygon) helsinkiRegion.getGeometryN(0);

            csq = region.getExteriorRing().getCoordinateSequence();
            assertEquals(49, csq.size());
            assertEquals(3380768.212, csq.getOrdinate(0, 0), 1e-9);
            assertEquals(6679512.723, csq.getOrdinate(0, 1), 1e-9);
            assertEquals(3381892.004, csq.getOrdinate(1, 0), 1e-9);
            assertEquals(6677581.001, csq.getOrdinate(1, 1), 1e-9);

            region = (Polygon) helsinkiRegion.getGeometryN(1);

            csq = region.getExteriorRing().getCoordinateSequence();
            assertEquals(5, csq.size());
            assertEquals(3383004.736, csq.getOrdinate(0, 0), 1e-9);
            assertEquals(6672746.505, csq.getOrdinate(0, 1), 1e-9);
            assertEquals(3382883.260, csq.getOrdinate(1, 0), 1e-9);
            assertEquals(6675036.999, csq.getOrdinate(1, 1), 1e-9);

            assertEquals(0, (long) f.getAttribute("id"));
            assertEquals(0, (long) f.getAttribute("aineisto_id"));
            assertEquals("980", f.getAttribute("aluekoodi"));
            assertEquals("Helsinki", f.getAttribute("nimi"));
            assertEquals("Helsingfors", f.getAttribute("nimi_se"));
            assertEquals("http://www.kela.fi", f.getAttribute("www_osoite"));
            assertEquals("", f.getAttribute("modify_user"));
            assertEquals(null, f.getAttribute("modify_time"));
            assertEquals(LocalDate.class, f.getProperty("modify_time").getType().getBinding());
        }
    }

}
