package fi.mml.map.mapwindow.util;

import java.util.Map;
import java.util.Set;

public class PermissionCollection {

    private final Set<String> permissionsList;
    private final Set<String> downloadPermissionsList;
    private final Set<String> editAccessList;
    private final Map<String, Set<String>> dynamicPermissions;

    public PermissionCollection(Set<String> permissionsList, Set<String> downloadPermissionsList,
            Set<String> editAccessList, Map<String, Set<String>> dynamicPermissions) {
        this.permissionsList = permissionsList;
        this.downloadPermissionsList = downloadPermissionsList;
        this.editAccessList = editAccessList;
        this.dynamicPermissions = dynamicPermissions;
    }

    public Set<String> getPermissionsList() {
        return permissionsList;
    }

    public Set<String> getDownloadPermissionsList() {
        return downloadPermissionsList;
    }

    public Set<String> getEditAccessList() {
        return editAccessList;
    }

    public Map<String, Set<String>> getDynamicPermissions() {
        return dynamicPermissions;
    }

}
