package org.oskari.ogcapi.features;

import org.oskari.ogcapi.OpenAPILink;

import java.util.List;

public class FeaturesContent {

    private List<OpenAPILink> links;
    private List<FeaturesCollectionInfo> collections;

    public List<OpenAPILink> getLinks() {
        return links;
    }

    public void setLinks(List<OpenAPILink> links) {
        this.links = links;
    }

    public List<FeaturesCollectionInfo> getCollections() {
        return collections;
    }

    public void setCollections(List<FeaturesCollectionInfo> collections) {
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
        FeaturesContent other = (FeaturesContent) obj;
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
