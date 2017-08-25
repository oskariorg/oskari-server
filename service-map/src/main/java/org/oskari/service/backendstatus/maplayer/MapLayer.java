package org.oskari.service.backendstatus.maplayer;

/**
 * Simplified version of OskariLayer
 */
public class MapLayer {

    private final int id;
    private final String name;
    private final String url;

    public MapLayer(Integer id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

}
