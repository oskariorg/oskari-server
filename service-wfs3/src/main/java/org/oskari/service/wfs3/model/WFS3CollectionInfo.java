package org.oskari.service.wfs3.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WFS3CollectionInfo {

    private String id;
    private String title;
    private String description;
    private List<WFS3Link> links;
    private WFS3Extent extent;
    private List<String> crs = Arrays.asList("http://www.opengis.net/def/crs/OGC/1.3/CRS84");

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // 'id' used to be 'name' prior to 14.2.2019
    public void setName(String name) {
        this.id = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<WFS3Link> getLinks() {
        return links == null ? Collections.emptyList() : links;
    }

    public void setLinks(List<WFS3Link> links) {
        this.links = links;
    }

    public WFS3Extent getExtent() {
        return extent;
    }

    public void setExtent(WFS3Extent extent) {
        this.extent = extent;
    }

    public List<String> getCrs() {
        return crs == null ? Collections.emptyList() : crs;
    }

    public void setCrs(List<String> crs) {
        this.crs = crs;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WFS3CollectionInfo other = (WFS3CollectionInfo) obj;
        if (crs == null) {
            if (other.crs != null)
                return false;
        } else if (!crs.equals(other.crs))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (extent == null) {
            if (other.extent != null)
                return false;
        } else if (!extent.equals(other.extent))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (links == null) {
            if (other.links != null)
                return false;
        } else if (!links.equals(other.links))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        return true;
    }

}
