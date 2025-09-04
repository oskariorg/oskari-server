package org.oskari.map.myfeatures.service;

import java.util.List;
import java.util.UUID;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import fi.nls.oskari.service.OskariComponent;

/**
 * All input geometries are expected to be in native coordinate reference system
 * All returned geometries will be in native coordinate reference system
 */
public abstract class MyFeaturesService extends OskariComponent {

    public abstract CoordinateReferenceSystem getNativeCRS();

    public abstract MyFeaturesLayer getLayer(UUID layerId);
    public abstract void createLayer(MyFeaturesLayer layer);
    public abstract void updateLayer(MyFeaturesLayer layer);
    public abstract void deleteLayer(UUID layerId);

    public abstract MyFeaturesFeature getFeature(UUID layerId, long featureId);
    public abstract void createFeature(UUID layerId, MyFeaturesFeature f);
    public abstract void updateFeature(UUID layerId, MyFeaturesFeature feature);
    public abstract void deleteFeature(UUID layerId, long featureId);

    public abstract List<MyFeaturesFeature> getFeatures(UUID layerId);
    public abstract List<MyFeaturesFeature> getFeaturesByBbox(UUID layerId, double minX, double minY, double maxX, double maxY);

    public abstract void createFeatures(UUID layerId, List<MyFeaturesFeature> features);

    public abstract List<MyFeaturesLayer> getLayersByOwnerUuid(String ownerUuid);
    public abstract void deleteLayersByOwnerUuid(String ownerUuid);

    public abstract void swapAxisOrder(UUID layerId);

}
