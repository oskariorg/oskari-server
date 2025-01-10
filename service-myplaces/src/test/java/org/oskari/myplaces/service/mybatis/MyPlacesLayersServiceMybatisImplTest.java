package org.oskari.myplaces.service.mybatis;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import fi.nls.test.util.ResourceHelper;
import fi.nls.test.util.TestHelper;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.test.util.JSONTestHelper;

import javax.sql.DataSource;

public class MyPlacesLayersServiceMybatisImplTest {

    @Test
    public void happyCase() throws Exception {
        List<String> sqls = ResourceHelper.readSqlStatements(MyPlacesLayersServiceMybatisImplTest.class, "/categories_ddl.sql");
        DataSource ds = TestHelper.createMemDBforUnitTest(sqls);
        MyPlacesLayersServiceMybatisImpl service = new MyPlacesLayersServiceMybatisImpl(ds);

        String uuid = UUID.randomUUID().toString();

        JSONObject options = new JSONObject();
        options.put("foo", "bar");
        options.put("qux", 112);

        MyPlaceCategory expected = new MyPlaceCategory();
        expected.setUuid(uuid);
        expected.setCategory_name("foobar");
        expected.setPublisher_name("bazqux");
        expected.setDefault(true);
        expected.setOptions(options);
        expected.setLocale(new JSONObject());

        Assertions.assertEquals(0, expected.getId());
        Assertions.assertEquals(1, service.insert(Arrays.asList(expected)));
        Assertions.assertEquals(1, expected.getId(), "Insert should modify id of the object");

        // Test getById
        MyPlaceCategory actual = service.getById(1).get();
        Assertions.assertEquals(1, actual.getId());
        assertEq(expected, actual);

        // Test getByIds
        List<MyPlaceCategory> actuals = service.getByIds(new long[] { 1, 2, 3 });
        Assertions.assertEquals(1, actuals.size(), "Only id 1 should exist");
        actual = actuals.get(0);
        Assertions.assertEquals(1, actual.getId());
        assertEq(expected, actual);

        // Test getByUserId
        actuals = service.getByUserId(uuid);
        Assertions.assertEquals(1, actuals.size(), "User should have 1 category");
        actual = actuals.get(0);
        assertEq(expected, actual);

        // Test update
        expected.setCategory_name("laalaa");
        expected.setPublisher_name("faxnax");
        expected.setDefault(false);
        Assertions.assertEquals(1, service.update(Arrays.asList(expected)));
        actual = service.getById(1).get();
        assertEq(expected, actual);
        // Sanity test to check assertEq(MyPlaceCategory, MyPlaceCategory) works
        Assertions.assertEquals(1, actual.getId());
        Assertions.assertEquals("laalaa", actual.getCategory_name());
        Assertions.assertEquals("faxnax", actual.getPublisher_name());
        Assertions.assertEquals(false, actual.isDefault());
        JSONTestHelper.shouldEqual(actual.getOptions(), new JSONObject("{'foo': 'bar', 'qux': 112}".replace('\'', '"')));

        // Test delete
        service.delete(new long[] { 1 });
        Assertions.assertEquals(false, service.getById(1).isPresent(), "After delete id 1 should not be available");
        Assertions.assertEquals(0, service.getByUserId(uuid).size(), "After delete user should have no categories");
    }

    private void assertEq(MyPlaceCategory expected, MyPlaceCategory actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getUuid(), actual.getUuid());
        Assertions.assertEquals(expected.getCategory_name(), actual.getCategory_name());
        Assertions.assertEquals(expected.getPublisher_name(), actual.getPublisher_name());
        JSONTestHelper.shouldEqual(actual.getOptions(), expected.getOptions());
    }

}
