package org.geotools.mif;

import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.NoSuchAuthorityCodeException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class MIFHeaderTest {

    @Test
    public void testHeaderParsing() throws URISyntaxException, IOException, NoSuchAuthorityCodeException, FactoryException, TransformException {
        File mif = new File(getClass().getResource("kenro_alue_maarajat.MIF").toURI());
        MIFDataStore store = new MIFDataStore(mif, null);
        MIFHeader header = store.readHeader();
        Assertions.assertEquals(750, header.getVersion());
        Assertions.assertEquals(StandardCharsets.ISO_8859_1, header.getCharset());
        Assertions.assertEquals(",", header.getDelimiter());

        Assertions.assertEquals(8, header.getColumns().length);
        int i = 0;
        Assertions.assertEquals("id", header.getColumns()[i++].getName());
        Assertions.assertEquals("aineisto_id", header.getColumns()[i++].getName());
        Assertions.assertEquals("aluekoodi", header.getColumns()[i++].getName());
        Assertions.assertEquals("nimi", header.getColumns()[i++].getName());
        Assertions.assertEquals("nimi_se", header.getColumns()[i++].getName());
        Assertions.assertEquals("www_osoite", header.getColumns()[i++].getName());
        Assertions.assertEquals("modify_user", header.getColumns()[i++].getName());
        Assertions.assertEquals("modify_time", header.getColumns()[i++].getName());
    }

    @Test
    public void testCoordSysWithExplicitBoundsTypeCode() throws URISyntaxException, IOException, NoSuchAuthorityCodeException, FactoryException, TransformException {
        File mif = new File(getClass().getResource("has_explicit_bounds.mif").toURI());
        MIFDataStore store = new MIFDataStore(mif, null);
        MIFHeader header = store.readHeader();
        Assertions.assertEquals(750, header.getVersion());
        Assertions.assertEquals(StandardCharsets.ISO_8859_1, header.getCharset());
        CoordinateReferenceSystem actual = header.getCoordSys();
        MathTransform t = CRS.findMathTransform(actual, CRS.decode("EPSG:3067", true));
        double[] src = new double[2];
        double[] dst = new double[2];
        src[0] = 357517.2;
        src[1] = 6860602.8;
        t.transform(src, 0, dst, 0, 1);
        Assertions.assertArrayEquals(src, dst, 0.0);
    }

    @Test
    public void tm35fin_kiinteistoraja() throws URISyntaxException, IOException, NoSuchAuthorityCodeException, FactoryException, TransformException {
        File mif = new File(getClass().getResource("tm35fin_kiinteistoraja.mif").toURI());
        MIFDataStore store = new MIFDataStore(mif, null);
        MIFHeader header = store.readHeader();
        Assertions.assertEquals(300, header.getVersion());
        Assertions.assertEquals(StandardCharsets.ISO_8859_1, header.getCharset());
        CoordinateReferenceSystem actual = header.getCoordSys();
        MathTransform t = CRS.findMathTransform(actual, CRS.decode("EPSG:3067", true));
        double[] src = new double[2];
        double[] dst = new double[2];
        src[0] = 357517.2;
        src[1] = 6860602.8;
        t.transform(src, 0, dst, 0, 1);
        Assertions.assertArrayEquals(src, dst, 0.0);
    }

    @Test
    public void gk23_kiinteistoraja() throws URISyntaxException, IOException, NoSuchAuthorityCodeException, FactoryException, TransformException {
        File mif = new File(getClass().getResource("gk23_kiinteistoraja.mif").toURI());
        MIFDataStore store = new MIFDataStore(mif, null);
        MIFHeader header = store.readHeader();
        Assertions.assertEquals(300, header.getVersion());
        Assertions.assertEquals(StandardCharsets.ISO_8859_1, header.getCharset());
        CoordinateReferenceSystem actual = header.getCoordSys();
        MathTransform t = CRS.findMathTransform(actual, CRS.decode("EPSG:3877", true));
        double[] src = new double[2];
        double[] dst = new double[2];
        src[0] = 23521271.015;
        src[1] = 7001167.539;
        t.transform(src, 0, dst, 0, 1);
        Assertions.assertArrayEquals(src, dst, 0.0);
    }

}
