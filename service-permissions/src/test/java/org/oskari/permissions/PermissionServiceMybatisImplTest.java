package org.oskari.permissions;

import fi.nls.test.util.ResourceHelper;
import fi.nls.test.util.TestHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oskari.permissions.model.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class PermissionServiceMybatisImplTest {

    private static PermissionServiceMybatisImpl permissionService;
    private static int DUMMY_ID = -1;

    private Resource myResource;
    private Permission myPermission;
    private Permission myPermission2;

    @BeforeClass
    public static void init() throws SQLException, IOException, URISyntaxException {
        List<String> sqls = ResourceHelper.readSqlStatements(PermissionServiceMybatisImplTest.class, "/schema.sql");
        DataSource ds = TestHelper.createMemDBforUnitTest(sqls);
        permissionService = new PermissionServiceMybatisImpl(ds);
    }

    @Before
    public void setup() {
        myResource = new OskariLayerResource(DUMMY_ID);

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
}
