package fi.nls.oskari.cache;

import fi.nls.oskari.cache.JedisSubscriber;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.*;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wfs.CachingSchemaLocator;
import fi.nls.oskari.work.WFSMapLayerJob;
import org.geotools.feature.FeatureCollection;
import org.geotools.resources.Classes;
import org.json.JSONObject;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.PropertyDescriptor;

import java.io.BufferedReader;
import java.util.*;

/**
 * To use this:
 *
 * // subscribe to schema channel
 * JedisManager.subscribe(new LayerUpdateSubscriber(), LayerUpdateSubscriber.CHANNEL);
 */
public class LayerUpdateSubscriber extends JedisSubscriber {
    private final static Logger log = LogFactory.getLogger(LayerUpdateSubscriber.class);

    public static final String CHANNEL = "layerConfiguration";
    public static final String SCHEMA_CHANNEL = "schemaInfo";

    public static final String MSG_UPDATED = "updated";

    /**
     * Handles the received message if the channel is "schemaInfo"
     *
     * @param channel
     * @param message
     */
    @Override
    public void onMessage(String channel, String message) {
        if(channel.equals(CHANNEL) && message.equals(MSG_UPDATED)) {
            log.debug("2. PARSING LAYER SCHEMAS WITH LAYER CONF @REDIS");

            // flush schema static hashmap and redis schemas - new stuff is saved when making the requests
            CachingSchemaLocator.flushAll();

            // processing configuration
            WFSMapLayerJob.Type processType = WFSMapLayerJob.Type.NORMAL;
            SessionStore store = new SessionStore();
            store.setLanguage(null);

            boolean customParser = false;

            // go through layers that are stored in redis and make a test request and parsing
            Set<String> layerKeys = JedisManager.keys("WFSLayer_*");
            for (String layerKey : layerKeys) {
                String json = JedisManager.get(layerKey);

                WFSLayerStore layer;
                try {
                    layer = WFSLayerStore.setJSON(json);
                } catch (Exception e) {
                    log.error(e, "JSON parsing failed for WFSLayerStore \n" + json);
                    continue;
                }
                customParser = false;
                if(layer.isCustomParser()) {
                    customParser = true;
                    layer.setCustomParser(""); // don't use custom parser
                }

                JSONObject root = new JSONObject();
                JSONHelper.putValue(root, "id", layer.getLayerId());

                // check that valid location information
                if(layer.getTestLocation().size() != 4) {
                    log.warn("Location bounds configuration failed", layer.getLayerId());
                    JSONHelper.putValue(root, "status", "fail - location problem");
                    JedisManager.publish(SCHEMA_CHANNEL, root.toString());
                    continue;
                }

                // set testing location
                /*Location location = new Location();
                location.setSrs(layer.getSRSName());
                location.setBbox(layer.getTestLocation());
                location.setZoom(layer.getTestZoom());
                store.setLocation(location);

                BufferedReader res = WFSMapLayerJob.request(processType, layer, store, layer.getTestLocation(), null);
                if(res == null) {
                    log.warn("Request failed for layer", layer.getLayerId());
                    JSONHelper.putValue(root, "status", "fail - no response");
                    JedisManager.publish(SCHEMA_CHANNEL, root.toString());
                    continue;
                }

                // parse response
                FeatureCollection<SimpleFeatureType, SimpleFeature> features = WFSMapLayerJob.response(layer, res);
                if(features == null) {
                    log.warn("Parsing failed", layer.getLayerId());
                    JSONHelper.putValue(root, "status", "fail - parsing problem");
                    JedisManager.publish(SCHEMA_CHANNEL, root.toString());
                    continue;
                }

                // valid if we have features
                if(features.size() == 0) {
                    log.warn("No features defined - can't validate", layer.getLayerId());
                    JSONHelper.putValue(root, "status", "fail - no features found");
                    JedisManager.publish(SCHEMA_CHANNEL, root.toString());
                    continue;
                }

                // NOTE: needs 1 valid feature to get a result
                // Supports two (2) level information (inner features)
                Map<String, String> types = new HashMap<String, String>();
                String defaultTypes = "";
                SimpleFeature feature = features.features().next();
                for(Property prop : feature.getProperties()) {
                    String field = prop.getName().toString();
                    String type = Classes.getShortName(prop.getType().getBinding());
                    if(type.equals("Geometry") && field.equals(layer.getGMLGeometryProperty().replaceAll("^[^_]*:", ""))) {
                        field = "*" + field;
                    }
                    if(defaultTypes.length() == 0) {
                        defaultTypes += field + ":" + type;
                    } else {
                        defaultTypes += "," + field + ":" + type;
                    }

                    if(type.equals("Feature")) {
                        // inner feature
                        String innerTypes = "";
                        SimpleFeature innerFeature = (SimpleFeature) feature.getAttribute(field);
                        for(Property prop2 : innerFeature.getProperties()) {
                            String field2 = prop2.getName().toString();
                            String type2 = Classes.getShortName(prop2.getType().getBinding());
                            if(innerTypes.length() == 0) {
                                innerTypes += field2 + ":" + type2;
                            } else {
                                innerTypes += "," + field2 + ":" + type2;
                            }
                        }
                        types.put(field, innerTypes);
                    }
                }
                types.put("default", defaultTypes);

                if(customParser) layer.setCustomParser("true");
                layer.setFeatureType(types);
                layer.save();

                // send publish to get new schema in db
                JSONObject schema = new JSONObject(types);
                JSONHelper.putValue(root, "schema", schema);
                JSONHelper.putValue(root, "status", "ok");
                JedisManager.publish(SCHEMA_CHANNEL, root.toString());
                */
            }
        }
    }

}
