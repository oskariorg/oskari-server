package org.oskari.permissions;

import fi.nls.test.util.ResourceHelper;
import fi.nls.test.util.TestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oskari.permissions.model.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PermissionServiceMybatisImplTest {

    private static PermissionServiceMybatisImpl permissionService;
    private static int DUMMY_ID = -1;

    private Resource myResource;
    private Permission myPermission;
    private Permission myPermission2;

    @BeforeAll
    public static void init() throws SQLException, IOException, URISyntaxException {
        List<String> sqls = ResourceHelper.readSqlStatements(PermissionServiceMybatisImplTest.class, "/schema.sql");
        DataSource ds = TestHelper.createMemDBforUnitTest(sqls);
        permissionService = new PermissionServiceMybatisImpl(ds);
    }

    @BeforeEach
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
        Assertions.assertEquals(-1, myResource.getId());
        Assertions.assertEquals(-1, myPermission.getId());
        Assertions.assertEquals(-1, myPermission2.getId());
        permissionService.insertResource(myResource);
        Assertions.assertNotEquals(-1, myResource.getId(), "Insert should change the value of id field of the Resource");
        Assertions.assertNotEquals(-1, myPermission.getId(), "Insert should change the value of id field of the Permission");
        Assertions.assertNotEquals(-1, myPermission2.getId(), "Insert should change the value of id field of the Permission");
        Assertions.assertNotEquals(myPermission.getId(), myPermission2.getId(), "Different Permissions should get different ids");

        Optional<Resource> resource = permissionService.findResource(myResource.getId());
        Assertions.assertTrue(resource.isPresent());
        Resource actual = resource.get();
        Assertions.assertEquals(myResource.getId(), actual.getId());
        Assertions.assertEquals(myResource.getType(), actual.getType());
        Assertions.assertEquals(myResource.getMapping(), actual.getMapping());
        Assertions.assertEquals(2, actual.getPermissions().size());

        // Manually find the one that has the same id as myPermission (don't trust the list to be in same order)
        Permission actualPermission = findPermissionWithId(actual.getPermissions(), myPermission.getId());
        // ids match so no need to check those, findPermissionWithId would've thrown NoSuchElementException by now
        Assertions.assertEquals(myPermission.getType(), actualPermission.getType());
        Assertions.assertEquals(myPermission.getExternalType(), actualPermission.getExternalType());
        Assertions.assertEquals(myPermission.getExternalId(), actualPermission.getExternalId());

        permissionService.deleteResource(myResource);
        resource = permissionService.findResource(myResource.getId());
        Assertions.assertFalse(resource.isPresent());
        // TODO: Verify that the oskari_resource_permission rows are also deleted
    }

    private Permission findPermissionWithId(List<Permission> permissions, int id) {
        return permissions.stream()
                .filter(p -> p.getId() == id)
                .findAny()
                .get();
    }
}
