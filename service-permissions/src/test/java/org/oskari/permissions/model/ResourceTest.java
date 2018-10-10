package org.oskari.permissions.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.oskari.permissions.model.Permission.ExternalType;
import org.oskari.permissions.model.Permission.Type;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;

public class ResourceTest {

    private Role guest;
    private Role admin;

    @Before
    public void setup() {
        guest = new Role();
        guest.setId(1);
        guest.setName("Foo");

        admin = new Role();
        admin.setId(2);
        admin.setName(Role.DEFAULT_ADMIN_ROLE_NAME);
    }

    @Test
    public void testHasPermission() {
        Resource resource = new Resource();
        assertFalse(resource.hasPermission(guest, Type.DOWNLOAD));
        assertFalse(resource.hasPermission(guest, Type.EDIT_LAYER));

        resource.addPermission(getPermision(Type.DOWNLOAD, guest));
        assertTrue(resource.hasPermission(guest, Type.DOWNLOAD));
        assertFalse(resource.hasPermission(guest, Type.EDIT_LAYER));

        resource.removePermissionsOfType(Type.DOWNLOAD);
        assertFalse(resource.hasPermission(guest, Type.DOWNLOAD));
        assertFalse(resource.hasPermission(guest, Type.EDIT_LAYER));

        resource.addPermission(getPermision(Type.EDIT_LAYER, guest));
        assertFalse(resource.hasPermission(guest, Type.DOWNLOAD));
        assertTrue(resource.hasPermission(guest, Type.EDIT_LAYER));

        User user = new User();
        user.setId(1);

        assertFalse(resource.hasPermission(user, Type.EDIT_LAYER));
        assertFalse(resource.hasPermission(user, Type.DOWNLOAD));

        user.addRole(guest);
        assertFalse(resource.hasPermission(user, Type.DOWNLOAD));
        assertTrue(resource.hasPermission(user, Type.EDIT_LAYER));

        user.clearRoles();
        assertFalse(resource.hasPermission(user, Type.DOWNLOAD));
        assertFalse(resource.hasPermission(user, Type.EDIT_LAYER));

        user.addRole(admin);
        assertFalse(resource.hasPermission(user, Type.DOWNLOAD));
        assertTrue(resource.hasPermission(user, Type.EDIT_LAYER));
    }

    public Permission getPermision(Type type, Role role) {
        Permission permission = new Permission();
        permission.setType(type);
        permission.setExternalType(ExternalType.ROLE);
        permission.setExternalId((int) role.getId());
        return permission;
    }

}
