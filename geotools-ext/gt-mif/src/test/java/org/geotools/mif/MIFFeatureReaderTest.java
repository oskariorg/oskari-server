package org.geotools.mif;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

public class MIFFeatureReaderTest {

    @Test
    public void testReadingEmptyMIDFields() throws URISyntaxException, IOException {
        File mif = new File(getClass().getResource("empty_fields.MIF").toURI());
        File mid = new File(getClass().getResource("empty_fields.MID").toURI());
        DataStore store = new MIFDataStore(mif, mid);
        SimpleFeatureSource source = store.getFeatureSource("empty_fields");
        SimpleFeatureCollection fc = source.getFeatures();
        Assertions.assertEquals(1, fc.size());
        try (SimpleFeatureIterator it = fc.features()) {
            if (!it.hasNext()) {
                Assertions.fail();
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
            Assertions.assertEquals(5, region.getNumPoints()); // +1 because ring is automatically closed
            Assertions.assertEquals(0, region.getNumInteriorRing());

            CoordinateSequence csq = region.getExteriorRing().getCoordinateSequence();
            Assertions.assertEquals(357517.2, csq.getOrdinate(0, 0), 1e-9);
            Assertions.assertEquals(6860602.8, csq.getOrdinate(0, 1), 1e-9);
            Assertions.assertEquals(357539.1, csq.getOrdinate(1, 0), 1e-9);
            Assertions.assertEquals(6860613.8, csq.getOrdinate(1, 1), 1e-9);
            Assertions.assertEquals(357556.1, csq.getOrdinate(2, 0), 1e-9);
            Assertions.assertEquals(6860578.1, csq.getOrdinate(2, 1), 1e-9);
            Assertions.assertEquals(357533.8, csq.getOrdinate(3, 0), 1e-9);
            Assertions.assertEquals(6860567.8, csq.getOrdinate(3, 1), 1e-9);
            Assertions.assertEquals(357517.2, csq.getOrdinate(4, 0), 1e-9);
            Assertions.assertEquals(6860602.8, csq.getOrdinate(4, 1), 1e-9);

            Assertions.assertNull(f.getAttribute("id"));
            Assertions.assertEquals(Integer.class, f.getProperty("id").getType().getBinding());

            Assertions.assertNull(f.getAttribute("foo"));
            Assertions.assertEquals(Long.class, f.getProperty("foo").getType().getBinding());

            Assertions.assertNull(f.getAttribute("bar"));
            Assertions.assertEquals(Float.class, f.getProperty("bar").getType().getBinding());

            Assertions.assertNull(f.getAttribute("baz"));
            Assertions.assertEquals(Double.class, f.getProperty("baz").getType().getBinding());

            Assertions.assertNull(f.getAttribute("qux"));
            Assertions.assertEquals(Boolean.class, f.getProperty("qux").getType().getBinding());

            if (it.hasNext()) {
                Assertions.fail();
            }
        }
    }

    @Test
    public void testFeatureReader() throws URISyntaxException, IOException {
        File mif = new File(getClass().getResource("kenro_alue_maarajat.MIF").toURI());
        File mid = new File(getClass().getResource("kenro_alue_maarajat.MID").toURI());
        DataStore store = new MIFDataStore(mif, mid);
        SimpleFeatureSource source = store.getFeatureSource("kenro_alue_maarajat");
        SimpleFeatureCollection fc = source.getFeatures();
        Assertions.assertEquals(25, fc.size());
        try (SimpleFeatureIterator it = fc.features()) {
            if (!it.hasNext()) {
                Assertions.fail();
            }
            SimpleFeature f = it.next();
            // Region  1
            //   5615
            // 3644472.125 7289630.034
            // 3644097.725 7293078.173
            // 0,0,"516","Lappi","Lappland","http://www.kela.fi","",
            Polygon region = (Polygon) f.getDefaultGeometry();
            Assertions.assertEquals(5615, region.getNumPoints());
            Assertions.assertEquals(0, region.getNumInteriorRing());

            CoordinateSequence csq = region.getExteriorRing().getCoordinateSequence();
            Assertions.assertEquals(3644472.125, csq.getOrdinate(0, 0), 1e-9);
            Assertions.assertEquals(7289630.034, csq.getOrdinate(0, 1), 1e-9);
            Assertions.assertEquals(3644097.725, csq.getOrdinate(1, 0), 1e-9);
            Assertions.assertEquals(7293078.173, csq.getOrdinate(1, 1), 1e-9);

            Assertions.assertEquals(0, (long) f.getAttribute("id"));
            Assertions.assertEquals(0, (long) f.getAttribute("aineisto_id"));
            Assertions.assertEquals("516", f.getAttribute("aluekoodi"));
            Assertions.assertEquals("Lappi", f.getAttribute("nimi"));
            Assertions.assertEquals("Lappland", f.getAttribute("nimi_se"));
            Assertions.assertEquals("http://www.kela.fi", f.getAttribute("www_osoite"));
            Assertions.assertEquals("", f.getAttribute("modify_user"));
            Assertions.assertEquals(null, f.getAttribute("modify_time"));
            Assertions.assertEquals(LocalDate.class, f.getProperty("modify_time").getType().getBinding());

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
            Assertions.assertEquals(54, helsinkiRegion.getNumPoints());
            Assertions.assertEquals(2, helsinkiRegion.getNumGeometries());

            region = (Polygon) helsinkiRegion.getGeometryN(0);

            csq = region.getExteriorRing().getCoordinateSequence();
            Assertions.assertEquals(49, csq.size());
            Assertions.assertEquals(3380768.212, csq.getOrdinate(0, 0), 1e-9);
            Assertions.assertEquals(6679512.723, csq.getOrdinate(0, 1), 1e-9);
            Assertions.assertEquals(3381892.004, csq.getOrdinate(1, 0), 1e-9);
            Assertions.assertEquals(6677581.001, csq.getOrdinate(1, 1), 1e-9);

            region = (Polygon) helsinkiRegion.getGeometryN(1);

            csq = region.getExteriorRing().getCoordinateSequence();
            Assertions.assertEquals(5, csq.size());
            Assertions.assertEquals(3383004.736, csq.getOrdinate(0, 0), 1e-9);
            Assertions.assertEquals(6672746.505, csq.getOrdinate(0, 1), 1e-9);
            Assertions.assertEquals(3382883.260, csq.getOrdinate(1, 0), 1e-9);
            Assertions.assertEquals(6675036.999, csq.getOrdinate(1, 1), 1e-9);

            Assertions.assertEquals(0, (long) f.getAttribute("id"));
            Assertions.assertEquals(0, (long) f.getAttribute("aineisto_id"));
            Assertions.assertEquals("980", f.getAttribute("aluekoodi"));
            Assertions.assertEquals("Helsinki", f.getAttribute("nimi"));
            Assertions.assertEquals("Helsingfors", f.getAttribute("nimi_se"));
            Assertions.assertEquals("http://www.kela.fi", f.getAttribute("www_osoite"));
            Assertions.assertEquals("", f.getAttribute("modify_user"));
            Assertions.assertEquals(null, f.getAttribute("modify_time"));
            Assertions.assertEquals(LocalDate.class, f.getProperty("modify_time").getType().getBinding());
        }
    }

    @Test
    public void testReadingTextstringOnSeparateLine() throws URISyntaxException, IOException {
        File mif = new File(getClass().getResource("textstring_separate_line.mif").toURI());
        File fakeMid = new File("anything_that_doesnt_exists");

        DataStore store = new MIFDataStore(mif, fakeMid);
        SimpleFeatureSource source = store.getFeatureSource("textstring_separate_line");
        SimpleFeatureCollection fc = source.getFeatures();
        Assertions.assertEquals(2, fc.size());
        try (SimpleFeatureIterator it = fc.features()) {
            if (!it.hasNext()) {
                Assertions.fail();
            }
            SimpleFeature f = it.next();

            /*
            REGION 1
            8
            321129.582 7003475.008
            319770.969 7003405.009
            319492.514 7003390.491
            319479.797 7003389.828
            318699.691 7003352.012
            318696.539 7003426.558
            321120.18 7003556.465
            321129.582 7003475.008
            BRUSH(1,0)
            */
            Polygon region = (Polygon) f.getDefaultGeometry();
            Assertions.assertEquals(8, region.getNumPoints());
            Assertions.assertEquals(0, region.getNumInteriorRing());

            /*
            TEXT
                "233-404-5-70"
                319350.402 7003400.612 319415.202 7003405.612
                */
            if (!it.hasNext()) {
                Assertions.fail();
            }
            f = it.next();
            Assertions.assertEquals(Point.class, f.getDefaultGeometry().getClass());
        }
    }

}
