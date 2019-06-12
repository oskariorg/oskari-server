package org.oskari.permissions.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;

public class ResourceTest {

    private Role guest;
    private Role admin;



    @Before
    public void setup() throws DuplicateException {
        guest = new Role();
        guest.setId(1);
        guest.setName("Foo");

        admin = new Role();
        admin.setId(2);
        admin.setName(Role.DEFAULT_ADMIN_ROLE_NAME);

        PropertyUtil.addProperty("oskari.user.service", "fi.nls.oskari.service.DummyUserService", true);
    }

    @After
    public void teardown() {
        PropertyUtil.clearProperties();
    }

    @Test
    public void testHasPermission() {
        Resource resource = new Resource();
        assertFalse(resource.hasPermission(guest, PermissionType.DOWNLOAD));
        assertFalse(resource.hasPermission(guest, PermissionType.EDIT_LAYER));

        resource.addPermission(getPermision(PermissionType.DOWNLOAD, guest));
        assertTrue(resource.hasPermission(guest, PermissionType.DOWNLOAD));
        assertFalse(resource.hasPermission(guest, PermissionType.EDIT_LAYER));

        resource.removePermissionsOfType(PermissionType.DOWNLOAD);
        assertFalse(resource.hasPermission(guest, PermissionType.DOWNLOAD));
        assertFalse(resource.hasPermission(guest, PermissionType.EDIT_LAYER));

        resource.addPermission(getPermision(PermissionType.EDIT_LAYER, guest));
        assertFalse(resource.hasPermission(guest, PermissionType.DOWNLOAD));
        assertTrue(resource.hasPermission(guest, PermissionType.EDIT_LAYER));

        User user = new User();
        user.setId(1);

        assertFalse(resource.hasPermission(user, PermissionType.EDIT_LAYER));
        assertFalse(resource.hasPermission(user, PermissionType.DOWNLOAD));

        user.addRole(guest);
        assertFalse(resource.hasPermission(user, PermissionType.DOWNLOAD));
        assertTrue(resource.hasPermission(user, PermissionType.EDIT_LAYER));

        user.clearRoles();
        assertFalse(resource.hasPermission(user, PermissionType.DOWNLOAD));
        assertFalse(resource.hasPermission(user, PermissionType.EDIT_LAYER));

        user.addRole(admin);
        assertFalse(resource.hasPermission(user, PermissionType.DOWNLOAD));
        assertTrue(resource.hasPermission(user, PermissionType.EDIT_LAYER));
    }

    public Permission getPermision(PermissionType type, Role role) {
        Permission permission = new Permission();
        permission.setType(type);
        permission.setExternalType(PermissionExternalType.ROLE);
        permission.setExternalId((int) role.getId());
        return permission;
    }

}
