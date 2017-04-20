package fi.nls.oskari.wfs.extension;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wfs.LayerProcessor;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;

public class UserLayerProcessor implements LayerProcessor {
		
	private static Logger log = LogFactory.getLogger(UserLayerProcessor.class);
	private static final String USERLAYER_PREFIX = "userlayer_";
	
	protected static Set<String> excludedProperties = new HashSet<String>();
	static {		
		excludedProperties.add("property_json");
		excludedProperties.add("uuid");
		excludedProperties.add("user_layer_id");
		excludedProperties.add("feature_id");
		excludedProperties.add("created");
		excludedProperties.add("updated");
		excludedProperties.add("attention_text");
	}

	public boolean isProcessable(WFSLayerStore layer) {
		return layer.getLayerId().startsWith(USERLAYER_PREFIX);
		//return layer.getId() == PropertyUtil.getOptional("userlayer.baselayer.id", -1); //layer.getId() == -1, should be userlayer.baselayer.id (e.g. 3 or 10)
	}
	
	/**
     * Parse features' property_json attribute and add parsed attributes to features
     *
     */
    public FeatureCollection<SimpleFeatureType, SimpleFeature> process (FeatureCollection<SimpleFeatureType, SimpleFeature> features, WFSLayerStore layer){
		
		DefaultFeatureCollection parsedFeatures = null;
		SimpleFeature simpleFeature;		
		SimpleFeatureType simpleFeatureType = features.getSchema();
		SimpleFeatureType parsedFeatureType = null;
		SimpleFeatureBuilder featureBuilder = null;
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();		
		typeBuilder.setName(simpleFeatureType.getName());
		typeBuilder.setNamespaceURI(layer.getFeatureNamespaceURI());
		typeBuilder.setSRS(layer.getSRSName());
		//typeBuilder.setCRS(layer.getCrs());
		ObjectMapper mapper = new ObjectMapper();

		//copy feature's attributes to new feature type builder
		//do not add excludedProperties
		for (AttributeDescriptor desc : simpleFeatureType.getAttributeDescriptors()){
			if (!excludedProperties.contains(desc.getLocalName())){
				typeBuilder.add(desc);
			}
		}
		
		FeatureIterator<SimpleFeature> iterator = features.features();
		try {
			while(iterator.hasNext()){
				simpleFeature = iterator.next();
				Property propertyJson = simpleFeature.getProperty("property_json");
				Map <String,Object> jsonMap = mapper.readValue(propertyJson.getValue().toString(), new TypeReference<HashMap<String,Object>>() {});
				
				// only for first feature to build new processed/parsed feature type
				if (parsedFeatureType == null){
					// add new parsed attributes from property_json to new type builder
					for (String attributeName : jsonMap.keySet()){
						typeBuilder.add(attributeName, jsonMap.get(attributeName).getClass());
						
					}					
					parsedFeatureType = typeBuilder.buildFeatureType();
					parsedFeatures= new DefaultFeatureCollection(features.getID(), parsedFeatureType);
					featureBuilder = new SimpleFeatureBuilder(parsedFeatureType);
				}
				//copy attribute values
				//do not add excludedProperties
				for (Property property : simpleFeature.getProperties()){
					if (!excludedProperties.contains(property.getName().getLocalPart())){
						featureBuilder.set(property.getName(), property.getValue());
					}
				}
				
				// add new attribute values (from property_json)
				for (String attributeName : jsonMap.keySet()){
					featureBuilder.set (attributeName, jsonMap.get(attributeName));
				}
				parsedFeatures.add(featureBuilder.buildFeature(simpleFeature.getID()));
			}
		}catch (Exception ex){
			throw new ServiceRuntimeException("Userlayer processing failed", ex);
		}
		finally{
			iterator.close();
		}
		return parsedFeatures;
    }
}