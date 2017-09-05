package fi.nls.oskari.wfs;

import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.feature.FeatureCollection;

public interface LayerProcessor {
	boolean isProcessable(WFSLayerStore layer);
	FeatureCollection<SimpleFeatureType, SimpleFeature> process (FeatureCollection<SimpleFeatureType, SimpleFeature> features, WFSLayerStore layer);
		
	
	
}