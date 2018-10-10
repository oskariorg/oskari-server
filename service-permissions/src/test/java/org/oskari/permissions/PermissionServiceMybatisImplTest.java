package org.oskari.permissions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oskari.permissions.model.Permission;
import org.oskari.permissions.model.PermissionExternalType;
import org.oskari.permissions.model.PermissionType;
import org.oskari.permissions.model.Resource;
import org.oskari.permissions.model.ResourceType;

public class PermissionServiceMybatisImplTest {

    private static PermissionServiceMybatisImpl permissionService;

    private Resource myResource;
    private Permission myPermission;
    private Permission myPermission2;

    @BeforeClass
    public static void init() throws SQLException, IOException, URISyntaxException {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        List<String> schema = readAllLines("schema.sql");
        List<String> sqls = splitIntoStatements(schema);
        try (Connection c = ds.getConnection();
                Statement s = c.createStatement()) {
            for (String sql : sqls) {
                s.execute(sql);
            }
        }
        permissionService = new PermissionServiceMybatisImpl(ds);
    }

    @Before
    public void setup() {
        myResource = new Resource();
        myResource.setType(ResourceType.maplayer);
        myResource.setMapping(1);

        myPermission = new Permission();
        myPermission.setExternalId(100);
        myPermission.setExternalType(PermissionExternalType.ROLE);
        myPermission.setType(PermissionType.DOWNLOAD);

        myPermission2 = new Permission();
        myPermission.setExternalId(100);
        myPermission.setExternalType(PermissionExternalType.ROLE);
        myPermission.setType(PermissionType.EDIT_LAYER);

        myResource.addPermission(myPermission);
        myResource.addPermission(myPermission2);
    }

    @Test
    public void testCRUD() {
        assertEquals(-1, myResource.getId());
        assertEquals(-1, myPermission.getId());
        assertEquals(-1, myPermission2.getId());
        permissionService.insertResource(myResource);
        assertNotEquals("Insert should change the value of id field of the Resource", -1, myResource.getId());
        assertNotEquals("Insert should change the value of id field of the Permission", -1, myPermission.getId());
        assertNotEquals("Insert should change the value of id field of the Permission", -1, myPermission2.getId());
        assertNotEquals("Different Permissions should get different ids", myPermission.getId(), myPermission2.getId());

        Optional<Resource> resource = permissionService.findResource(myResource.getId());
        assertTrue(resource.isPresent());
        Resource actual = resource.get();
        assertEquals(myResource.getId(), actual.getId());
        assertEquals(myResource.getType(), actual.getType());
        assertEquals(myResource.getMapping(), actual.getMapping());
        assertEquals(2, actual.getPermissions().size());

        // Manually find the one that has the same id as myPermission (don't trust the list to be in same order)
        Permission actualPermission = findPermissionWithId(actual.getPermissions(), myPermission.getId());
        // ids match so no need to check those, findPermissionWithId would've thrown NoSuchElementException by now
        assertEquals(myPermission.getType(), actualPermission.getType());
        assertEquals(myPermission.getExternalType(), actualPermission.getExternalType());
        assertEquals(myPermission.getExternalId(), actualPermission.getExternalId());

        permissionService.deleteResource(myResource);
        resource = permissionService.findResource(myResource.getId());
        assertFalse(resource.isPresent());
        // TODO: Verify that the oskari_permission rows are also deleted
    }

    private Permission findPermissionWithId(List<Permission> permissions, int id) {
        return permissions.stream()
                .filter(p -> p.getId() == id)
                .findAny()
                .get();
    }

    private static List<String> readAllLines(String resource) throws IOException, URISyntaxException {
        Path path = Paths.get(PermissionServiceMybatisImplTest.class.getClassLoader().getResource(resource).toURI());
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    private static List<String> splitIntoStatements(List<String> lines) {
        List<String> statements = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            int j = line.indexOf(';');
            if (j < 0) {
                sb.append(line).append(' ');
                continue;
            }
            if (j > 0) {
                sb.append(line.substring(0, j));
            }
            statements.add(sb.toString());
            sb.setLength(0);
        }
        return statements;
    }

}
