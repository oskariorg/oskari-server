package org.oskari.permissions.model;

import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.PropertyUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import org.oskari.user.Role;
import org.oskari.user.User;
import org.junit.jupiter.api.Test;

public class ResourceTest {

    private Role guest;
    private Role admin;



    @BeforeEach
    public void setup() throws DuplicateException {
        guest = new Role();
        guest.setId(1);
        guest.setName("Foo");

        admin = new Role();
        admin.setId(2);
        admin.setName(Role.DEFAULT_ADMIN_ROLE_NAME);

        PropertyUtil.addProperty("oskari.user.service", "fi.nls.oskari.service.DummyUserService", true);
    }

    @AfterEach
    public void teardown() {
        PropertyUtil.clearProperties();
    }

    @Test
    public void testHasPermission() {
        Resource resource = new Resource();
        Assertions.assertFalse(resource.hasPermission(guest, PermissionType.DOWNLOAD));
        Assertions.assertFalse(resource.hasPermission(guest, PermissionType.EDIT_LAYER));

        resource.addPermission(getPermision(PermissionType.DOWNLOAD, guest));
        resource.addPermission(getPermision(PermissionType.DOWNLOAD, admin));
        Assertions.assertTrue(resource.hasPermission(guest, PermissionType.DOWNLOAD));
        Assertions.assertFalse(resource.hasPermission(guest, PermissionType.EDIT_LAYER));

        resource.removePermissionsOfType(PermissionType.DOWNLOAD, PermissionExternalType.ROLE, (int) guest.getId());
        Assertions.assertFalse(resource.hasPermission(guest, PermissionType.DOWNLOAD));
        Assertions.assertFalse(resource.hasPermission(guest, PermissionType.EDIT_LAYER));

        resource.addPermission(getPermision(PermissionType.EDIT_LAYER, guest));
        Assertions.assertFalse(resource.hasPermission(guest, PermissionType.DOWNLOAD));
        Assertions.assertTrue(resource.hasPermission(guest, PermissionType.EDIT_LAYER));

        User user = new User();
        user.setId(1);

        Assertions.assertFalse(resource.hasPermission(user, PermissionType.EDIT_LAYER));
        Assertions.assertFalse(resource.hasPermission(user, PermissionType.DOWNLOAD));

        user.addRole(guest);
        Assertions.assertFalse(resource.hasPermission(user, PermissionType.DOWNLOAD));
        Assertions.assertTrue(resource.hasPermission(user, PermissionType.EDIT_LAYER));

        user.clearRoles();
        Assertions.assertFalse(resource.hasPermission(user, PermissionType.DOWNLOAD));
        Assertions.assertFalse(resource.hasPermission(user, PermissionType.EDIT_LAYER));

        user.addRole(admin);
        Assertions.assertTrue(resource.hasPermission(user, PermissionType.DOWNLOAD));
        Assertions.assertTrue(resource.hasPermission(user, PermissionType.EDIT_LAYER));
    }

    public Permission getPermision(PermissionType type, Role role) {
        Permission permission = new Permission();
        permission.setType(type);
        permission.setExternalType(PermissionExternalType.ROLE);
        permission.setExternalId((int) role.getId());
        return permission;
    }

}
