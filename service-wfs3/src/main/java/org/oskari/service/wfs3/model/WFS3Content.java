package org.oskari.service.wfs3.model;

import java.util.List;

public class WFS3Content {

    private List<WFS3Link> links;
    private List<WFS3CollectionInfo> collections;

    public List<WFS3Link> getLinks() {
        return links;
    }

    public void setLinks(List<WFS3Link> links) {
        this.links = links;
    }

    public List<WFS3CollectionInfo> getCollections() {
        return collections;
    }

    public void setCollections(List<WFS3CollectionInfo> collections) {
        this.collections = collections;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WFS3Content other = (WFS3Content) obj;
        if (collections == null) {
            if (other.collections != null)
                return false;
        } else if (!collections.equals(other.collections))
            return false;
        if (links == null) {
            if (other.links != null)
                return false;
        } else if (!links.equals(other.links))
            return false;
        return true;
    }

}
