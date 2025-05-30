package org.oskari.map.myfeatures.service;

import java.util.List;

import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.locationtech.jts.geom.Envelope;

import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;

public interface MyFeaturesService {

    public MyFeaturesLayer getLayerById(long id);

    public void createLayer(MyFeaturesLayer layer);
    public void updateLayer(MyFeaturesLayer layer);

    public int addFeatures(MyFeaturesLayer layer, List<MyFeaturesFeature> features);
    
    public void addFeature(MyFeaturesLayer layer, MyFeaturesFeature f);
    public void updateFeature(MyFeaturesLayer layer, MyFeaturesFeature f);
    public int deleteFeature(MyFeaturesLayer layer, String featureId);

    public Envelope getLayerExtent(final long id);
    public Envelope getLayerExtentFromData(final long id);
    public Envelope updateLayerExtentFromData(final long id);

    public Envelope swapAxisOrder(MyFeaturesLayer layer);

    public void deleteLayerAndFeaturesById(long id);

}
