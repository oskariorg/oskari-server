package org.oskari.control.myfeatures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.junit.jupiter.api.Test;
import org.oskari.map.userlayer.input.SHPParser;

import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFieldInfo;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFieldType;

public class ImportMyfeaturesHandlerTest {

    @Test
    public void testMappingFeaturesFromSHP() throws Exception {
        File file = new File(getClass().getClassLoader().getResource("kosa_1401_etrs-gk26_a_region.shp").toURI());
        SimpleFeatureCollection sfc = new SHPParser().parse(file, null, CRS.decode("EPSG:3067", true));

        List<MyFeaturesFieldInfo> fields = ImportMyFeaturesHandler.getFields(sfc.getSchema());
        assertEquals(8, fields.size());
        assertEquals(MyFeaturesFieldType.Integer, getFieldType(fields, "ID"));
        assertEquals(MyFeaturesFieldType.Integer, getFieldType(fields, "LAJI"));
        assertEquals(MyFeaturesFieldType.String, getFieldType(fields, "LAJIN_SELI"));
        assertEquals(MyFeaturesFieldType.Double, getFieldType(fields, "Z1"));
        assertEquals(MyFeaturesFieldType.Double, getFieldType(fields, "Z2"));
        assertEquals(MyFeaturesFieldType.String, getFieldType(fields, "TEKSTI"));
        assertEquals(MyFeaturesFieldType.String, getFieldType(fields, "AJ_KAUPUNG"));
        assertEquals(MyFeaturesFieldType.String, getFieldType(fields, "AJ_KAUPU00"));

        List<MyFeaturesFeature> myFeatures = ImportMyFeaturesHandler.toFeatures(sfc, fields, -1);
        assertEquals(sfc.size(), myFeatures.size());

        List<String> fieldNames = fields.stream().map(MyFeaturesFieldInfo::getName).toList();

        for (MyFeaturesFeature myFeature : myFeatures) {
            assertNotNull(myFeature.getFid());
            assertNotNull(myFeature.getGeometry());
            assertNotNull(myFeature.getProperties());
            assertEquals(8, myFeature.getProperties().length());
        }

        Map<String, MyFeaturesFeature> myFeaturesById = myFeatures.stream()
                .collect(Collectors.toMap(MyFeaturesFeature::getFid, x -> x));

        // Check that we find matching feature by fid and all of their properties match
        for (SimpleFeatureIterator it = sfc.features(); it.hasNext();) {
            SimpleFeature f = it.next();
            MyFeaturesFeature mf = myFeaturesById.get(f.getID());
            for (String fieldName : fieldNames) {
                Object expected = f.getAttribute(fieldName);
                Object actual = mf.getProperties().get(fieldName);
                assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void testMaxFeatures() throws Exception {
        File file = new File(getClass().getClassLoader().getResource("kosa_1401_etrs-gk26_a_region.shp").toURI());
        SimpleFeatureCollection sfc = new SHPParser().parse(file, null, CRS.decode("EPSG:3067", true));

        int maxFeatures = 5;
        List<MyFeaturesFieldInfo> fields = ImportMyFeaturesHandler.getFields(sfc.getSchema());
        List<MyFeaturesFeature> myFeatures = ImportMyFeaturesHandler.toFeatures(sfc, fields, maxFeatures);
        assertEquals(maxFeatures, myFeatures.size());
    }

    private static MyFeaturesFieldType getFieldType(List<MyFeaturesFieldInfo> fields, String name) {
        return fields.stream().filter(x -> name.equals(x.getName())).findAny().get().getType();
    }

}
