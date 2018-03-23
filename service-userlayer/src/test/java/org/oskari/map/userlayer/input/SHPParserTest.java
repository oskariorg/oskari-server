package org.oskari.map.userlayer.input;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.map.userlayer.input.SHPParser;

import fi.nls.oskari.service.ServiceException;

public class SHPParserTest {

    @Test
    public void testParse() throws ServiceException, URISyntaxException, NoSuchAuthorityCodeException, FactoryException {
        SimpleFeatureCollection fc = parse("SHP2017.shp");
        List<SimpleFeature> features = collectToList(fc);
        assertEquals("There are 21 Features in the file", 21, features.size());
        CoordinateReferenceSystem crs = fc.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem();
        assertEquals("Projection is correctly determined from .prj file", "EPSG:3067", CRS.toSRS(crs));
    }

    private SimpleFeatureCollection parse(String resourcePath) throws URISyntaxException, ServiceException {
        SHPParser parser = new SHPParser();
        File file = new File(getClass().getResource(resourcePath).toURI());
        return parser.parse(file, null, null);
    }

    private List<SimpleFeature> collectToList(SimpleFeatureCollection fc) {
        List<SimpleFeature> features = new ArrayList<>();
        try (SimpleFeatureIterator it = fc.features()) {
            while (it.hasNext()) {
                features.add(it.next());
            }
        }
        return features;
    }

}
