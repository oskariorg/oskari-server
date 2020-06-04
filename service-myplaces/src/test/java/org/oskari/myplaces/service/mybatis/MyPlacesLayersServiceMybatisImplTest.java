package org.oskari.myplaces.service.mybatis;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.h2.jdbcx.JdbcDataSource;
import org.json.JSONObject;
import org.junit.Test;

import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.test.util.JSONTestHelper;

public class MyPlacesLayersServiceMybatisImplTest {

    @Test
    public void happyCase() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;INIT=create domain if not exists json as text;MODE=PostgreSQL");

        Path path = Paths.get(getClass().getClassLoader().getResource("categories_ddl.sql").toURI());
        byte[] utf8 = Files.readAllBytes(path);
        String ddl = new String(utf8, StandardCharsets.UTF_8);

        try (Connection c = ds.getConnection();
                Statement stmt = c.createStatement()) {
            stmt.execute(ddl);
        }

        MyPlacesLayersServiceMybatisImpl service = new MyPlacesLayersServiceMybatisImpl(ds);

        String uuid = UUID.randomUUID().toString();

        JSONObject options = new JSONObject();
        options.put("foo", "bar");
        options.put("qux", 112);

        MyPlaceCategory expected = new MyPlaceCategory();
        expected.setUuid(uuid);
        expected.setName("foobar");
        expected.setPublisher_name("bazqux");
        expected.setDefault(true);
        expected.setOptions(options);

        assertEquals(0, expected.getId());
        assertEquals(1, service.insert(Arrays.asList(expected)));
        assertEquals("Insert should modify id of the object", 1, expected.getId());

        // Test getById
        MyPlaceCategory actual = service.getById(1).get();
        assertEquals(1, actual.getId());
        assertEq(expected, actual);

        // Test getByIds
        List<MyPlaceCategory> actuals = service.getByIds(new long[] { 1, 2, 3 });
        assertEquals("Only id 1 should exist", 1, actuals.size());
        actual = actuals.get(0);
        assertEquals(1, actual.getId());
        assertEq(expected, actual);

        // Test getByUserId
        actuals = service.getByUserId(uuid);
        assertEquals("User should have 1 category", 1, actuals.size());
        actual = actuals.get(0);
        assertEq(expected, actual);

        // Test update
        expected.setName("laalaa");
        expected.setPublisher_name("faxnax");
        expected.setDefault(false);
        assertEquals(1, service.update(Arrays.asList(expected)));
        actual = service.getById(1).get();
        assertEq(expected, actual);
        // Sanity test to check assertEq(MyPlaceCategory, MyPlaceCategory) works
        assertEquals(1, actual.getId());
        assertEquals("laalaa", actual.getName());
        assertEquals("faxnax", actual.getPublisher_name());
        assertEquals(false, actual.isDefault());
        JSONTestHelper.shouldEqual(actual.getOptions(), new JSONObject("{'foo': 'bar', 'qux': 112}".replace('\'', '"')));

        // Test delete
        service.delete(new long[] { 1 });
        assertEquals("After delete id 1 should not be available", false, service.getById(1).isPresent());
        assertEquals("After delete user should have no categories", 0, service.getByUserId(uuid).size());
    }

    private void assertEq(MyPlaceCategory expected, MyPlaceCategory actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getUuid(), actual.getUuid());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getPublisher_name(), actual.getPublisher_name());
        JSONTestHelper.shouldEqual(actual.getOptions(), expected.getOptions());
    }

}
