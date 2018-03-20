package fi.nls.oskari.wfs.extension;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.wfs.LayerProcessor;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserLayerProcessor implements LayerProcessor {

    private static final Logger LOG = LogFactory.getLogger(UserLayerProcessor.class);
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
        excludedProperties.add("id");
    }

    private ObjectMapper mapper = new ObjectMapper();

    public boolean isProcessable(WFSLayerStore layer) {
        return layer.getLayerId().startsWith(USERLAYER_PREFIX);
        //return layer.getId() == PropertyUtil.getOptional("userlayer.baselayer.id", -1); //layer.getId() == -1, should be userlayer.baselayer.id (e.g. 3 or 10)
    }

    /**
     * Parse features' property_json attribute and add parsed attributes to features
     */
    public FeatureCollection<SimpleFeatureType, SimpleFeature> process(FeatureCollection<SimpleFeatureType, SimpleFeature> features, WFSLayerStore layer) {

        DefaultFeatureCollection result = null;
        SimpleFeatureBuilder builder = null;

        FeatureIterator<SimpleFeature> iterator = features.features();
        try {
            while (iterator.hasNext()) {
                SimpleFeature simpleFeature = iterator.next();
                Map<String, Object> jsonMap = getUserlayerFields(simpleFeature);
                // only for first feature to build new processed/parsed feature type
                if (result == null) {
                    FeatureDef def = getFeatureDef(simpleFeature.getFeatureType(), layer, jsonMap, features.getID());
                    result = def.collection;
                    builder = def.builder;
                }
                //copy attribute values
                //do not add excludedProperties
                for (Property property : simpleFeature.getProperties()) {
                    if (!excludedProperties.contains(property.getName().getLocalPart())) {
                        builder.set(property.getName(), property.getValue());
                    }
                }

                // add new attribute values (from property_json)
                for (Map.Entry<String, Object> attribute : jsonMap.entrySet()) {
                    builder.set(attribute.getKey(), attribute.getValue());
                }
                // buildFeature calls reset() internally so we are good to go for next round
                result.add(builder.buildFeature(simpleFeature.getID()));
            }
        } catch (Exception ex) {
            throw new ServiceRuntimeException("Userlayer processing failed", ex);
        } finally {
            iterator.close();
        }
        return result;
    }

    protected Map<String, Object> getUserlayerFields(SimpleFeature simpleFeature) throws IOException {
        Property propertyJson = simpleFeature.getProperty("property_json");
        return mapper.readValue(propertyJson.getValue().toString(),
                new TypeReference<HashMap<String, Object>>() {});
    }

    protected FeatureDef getFeatureDef(SimpleFeatureType type, WFSLayerStore layer, Map<String, Object> jsonMap, String featuresetId) {
        FeatureDef p = new FeatureDef();
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName(type.getName());
        typeBuilder.setNamespaceURI(layer.getFeatureNamespaceURI());
        typeBuilder.setSRS(layer.getSRSName());

        //copy feature's attributes to new feature type builder
        //do not add excludedProperties
        for (AttributeDescriptor desc : type.getAttributeDescriptors()) {
            if (!excludedProperties.contains(desc.getLocalName())) {
                typeBuilder.add(desc);
            }
        }
        typeBuilder.setName(type.getName());
        // add new parsed attributes from property_json to new type builder
        for (Map.Entry<String, Object> attribute : jsonMap.entrySet()) {
            typeBuilder.add(attribute.getKey(), attribute.getValue().getClass());
        }

        SimpleFeatureType parsedFeatureType = typeBuilder.buildFeatureType();
        p.collection = new DefaultFeatureCollection(featuresetId, parsedFeatureType);
        p.builder = new SimpleFeatureBuilder(parsedFeatureType);
        return p;
    }

    class FeatureDef {
        DefaultFeatureCollection collection;
        SimpleFeatureBuilder builder;
    }
}