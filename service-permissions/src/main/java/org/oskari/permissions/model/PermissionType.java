package org.oskari.permissions.model;

public enum PermissionType {
    PUBLISH("publish"),
    VIEW_LAYER,
    EDIT_LAYER("edit"),
    EDIT_LAYER_CONTENT,
    VIEW_PUBLISHED,
    DOWNLOAD("download"),
    EXECUTE,
    ADD_MAPLAYER;

    private String jsonKey;

    PermissionType() {
        jsonKey = this.name();
    }
    PermissionType(String jsonName) {
        jsonKey = jsonName;
    }
    public String getJsonKey() {
        return jsonKey;
    }
}
