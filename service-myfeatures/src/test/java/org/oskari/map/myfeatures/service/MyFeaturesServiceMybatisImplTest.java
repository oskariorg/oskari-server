package org.oskari.map.myfeatures.service;

import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.test.util.JSONTestHelper;
import fi.nls.test.util.ResourceHelper;
import fi.nls.test.util.TestHelper;

import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import javax.sql.DataSource;

import java.util.List;
import java.util.UUID;

public class MyFeaturesServiceMybatisImplTest {

    private static MyFeaturesServiceMybatisImpl service;

    @BeforeAll
    public static void init() throws Exception {
        List<String> sqls = ResourceHelper.readSqlStatements(MyFeaturesServiceMybatisImplTest.class, "/ddl.sql");
        DataSource ds = TestHelper.createMemDBforUnitTest(sqls);
        service = new MyFeaturesServiceMybatisImpl(ds);
    }

    @Test
    public void layerCRUD() throws Exception {
        String uuid = UUID.randomUUID().toString();

        JSONObject options = new JSONObject();
        options.put("foo", "bar");
        options.put("qux", 112);

        MyFeaturesLayer expected = new MyFeaturesLayer();
        expected.setOwnerUuid(uuid);
        expected.setName("en", "foobar");
        expected.setLayerOptions(new WFSLayerOptions(options));

        Assertions.assertEquals(null, expected.getId());

        service.createLayer(expected);

        Assertions.assertNotEquals(0L, expected.getId().getLeastSignificantBits(), "createLayer should modify id of the layer");
        Assertions.assertNotEquals(0L, expected.getId().getMostSignificantBits(), "createLayer should modify id of the layer");

        Assertions.assertEquals("foobar", expected.getName("en"));
        
        // Test getLayer
        MyFeaturesLayer actual = service.getLayer(expected.getId());
        assertEq(expected, actual);
        
        // Test updating the layer
        expected.setName("sv", "fååbar");
        Assertions.assertEquals("foobar", expected.getName("en"));       
        Assertions.assertEquals("fååbar", expected.getName("sv"));
        
        service.updateLayer(expected);

        Assertions.assertNotEquals(expected.getCreated(), expected.getUpdated());

        actual = service.getLayer(expected.getId());
        assertEq(expected, actual);

        // Test deleting the layer
        service.deleteLayer(expected.getId());
        Assertions.assertNull(service.getLayer(expected.getId()));
    }

    private static void assertEq(MyFeaturesLayer expected, MyFeaturesLayer actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getOwnerUuid(), actual.getOwnerUuid());
        Assertions.assertEquals(expected.getNames(), actual.getNames());
        JSONTestHelper.shouldEqual(actual.getAttributes(), expected.getAttributes());
        JSONTestHelper.shouldEqual(actual.getOptions(), expected.getOptions());
    }

    @Test
    public void featuresCRUD() throws Exception {
        MyFeaturesLayer layer = new MyFeaturesLayer();
        layer.setName("en", "foobar");
        service.createLayer(layer);

        Assertions.assertNull(layer.getExtent());
        Assertions.assertEquals(0, layer.getFeatureCount());

        GeometryFactory gf = new GeometryFactory();
        
        MyFeaturesFeature f1 = new MyFeaturesFeature();
        f1.setFid("1000");
        f1.setGeometry(gf.createPoint(new Coordinate(23, 68)));
        f1.setProperties(new JSONObject()
            .put("my_prop", 5)
            .put("my_other_prop", "foo"));

        MyFeaturesFeature f2 = new MyFeaturesFeature();
        f2.setFid("1001");
        f2.setGeometry(gf.createPoint(new Coordinate(24, 69)));
        f2.setProperties(new JSONObject()
            .put("my_prop", 3)
            .put("my_other_prop", "bar"));

        MyFeaturesFeature f3 = new MyFeaturesFeature();
        f3.setFid("1002");
        f3.setGeometry(gf.createPoint(new Coordinate(25, 70)));
        f3.setProperties(new JSONObject()
            .put("my_prop", 1)
            .put("my_other_prop", "baz"));

        service.createFeature(layer.getId(), f1);
        service.createFeature(layer.getId(), f2);
        service.createFeature(layer.getId(), f3);

        List<MyFeaturesFeature> features = service.getFeatures(layer.getId());
        Assertions.assertEquals(3, features.size());
        assertEq(f1, features.stream().filter(x -> x.getFid().equals(f1.getFid())).findAny().get());
        assertEq(f2, features.stream().filter(x -> x.getFid().equals(f2.getFid())).findAny().get());
        assertEq(f3, features.stream().filter(x -> x.getFid().equals(f3.getFid())).findAny().get());
        layer = service.getLayer(layer.getId());
        Assertions.assertEquals(3, layer.getFeatureCount());
        Assertions.assertEquals(23, layer.getExtent().getMinX());
        Assertions.assertEquals(68, layer.getExtent().getMinY());
        Assertions.assertEquals(25, layer.getExtent().getMaxX());
        Assertions.assertEquals(70, layer.getExtent().getMaxY());

        f3.setGeometry(gf.createPoint(new Coordinate(26, 71)));
        f3.setProperties(new JSONObject()
            .put("my_prop", -1)
            .put("my_other_prop", "qux"));
        service.updateFeature(layer.getId(), f3);

        assertEq(f3, service.getFeature(layer.getId(), f3.getFid()));

        layer = service.getLayer(layer.getId());
        Assertions.assertEquals(3, layer.getFeatureCount());
        Assertions.assertEquals(23, layer.getExtent().getMinX());
        Assertions.assertEquals(68, layer.getExtent().getMinY());
        Assertions.assertEquals(26, layer.getExtent().getMaxX());
        Assertions.assertEquals(71, layer.getExtent().getMaxY());

        service.deleteFeature(layer.getId(), f3.getFid());

        Assertions.assertNull(service.getFeature(layer.getId(), f3.getFid()));

        features = service.getFeatures(layer.getId());
        Assertions.assertEquals(2, features.size());
        assertEq(f1, features.stream().filter(x -> x.getFid().equals(f1.getFid())).findAny().get());
        assertEq(f2, features.stream().filter(x -> x.getFid().equals(f2.getFid())).findAny().get());
        layer = service.getLayer(layer.getId());
        Assertions.assertEquals(2, layer.getFeatureCount());
        Assertions.assertEquals(23, layer.getExtent().getMinX());
        Assertions.assertEquals(68, layer.getExtent().getMinY());
        Assertions.assertEquals(24, layer.getExtent().getMaxX());
        Assertions.assertEquals(69, layer.getExtent().getMaxY());

        Assertions.assertEquals(layer.getCreated(), layer.getUpdated());

        service.swapAxisOrder(layer.getId());
        layer = service.getLayer(layer.getId());
        Assertions.assertEquals(2, layer.getFeatureCount());
        Assertions.assertEquals(68, layer.getExtent().getMinX());
        Assertions.assertEquals(23, layer.getExtent().getMinY());
        Assertions.assertEquals(69, layer.getExtent().getMaxX());
        Assertions.assertEquals(24, layer.getExtent().getMaxY());

        Assertions.assertEquals(layer.getCreated(), layer.getUpdated(), "swapAxisOrder should not update `updated`");

        service.deleteLayer(layer.getId());
        Assertions.assertTrue(service.getFeatures(layer.getId()).isEmpty(), "deleteLayer should delete features also");
    }

    private static void assertEq(MyFeaturesFeature expected, MyFeaturesFeature actual) {
        Assertions.assertEquals(expected.getFid(), actual.getFid());
        Assertions.assertEquals(expected.getGeometry(), actual.getGeometry());
        JSONTestHelper.shouldEqual(actual.getProperties(), expected.getProperties());
        Assertions.assertEquals(expected.getCreated(), actual.getCreated());
    }
}
