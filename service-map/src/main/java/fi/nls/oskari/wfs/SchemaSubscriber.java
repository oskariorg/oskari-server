package fi.nls.oskari.wfs;

import fi.nls.oskari.cache.JedisSubscriber;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

public class SchemaSubscriber extends JedisSubscriber {
    private final static Logger log = LogFactory.getLogger(SchemaSubscriber.class);

    public static final String CHANNEL = "schemaInfo";

    //private static WFSLayerConfigurationService wfsService = new WFSLayerConfigurationServiceIbatisImpl();

    /**
     * Handles the received message if the channel is "schemaInfo"
     *
     * @param channel
     * @param message
     */
    @Override
    public void onMessage(String channel, String message) {
        if(channel.equals(CHANNEL)) {
            log.debug("3. SAVING SCHEMA INFORMATION");
            JSONObject root = JSONHelper.createJSONObject(message);

            String tmpId = JSONHelper.getStringFromJSON(root, "id", null);
            if(tmpId == null) {
                log.error("Id was not set");
                return;
            }

            String status = JSONHelper.getStringFromJSON(root, "status", null);
            if(status == null) {
                log.error("Status is not set");
                status = "fail - status was not set";
            }


            String schema = null;
            try {
                schema = root.getJSONObject("schema").toString();
            } catch(Exception e) {
                log.error(e, "JSON fail");
            }

            if(schema == null && status.equals("ok")) {
                log.error("Schema is not set");
                status = "fail - schema was not set";
            }

            //long id = Long.parseLong(tmpId);
            //wfsService.updateSchemaInfo(id, schema, status);
        }
    }

}
