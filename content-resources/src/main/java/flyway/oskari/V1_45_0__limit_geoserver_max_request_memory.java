package flyway.oskari;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONException;
import org.json.JSONObject;

public class V1_45_0__limit_geoserver_max_request_memory implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_45_0__limit_geoserver_max_request_memory.class);

    private static final String PROP_GS_URL = "geoserver.url";
    private static final String PROP_GS_USER = "geoserver.user";
    private static final String PROP_GS_PASS = "geoserver.password";

    private static final String PROP_MAX_REQ_MEM = "flyway.1_45_0.geoserver.wms.max.request.memory";
    private static final int DEFAULT_MAX_REQ_MEM_KB = 128 * 1024;

    private static final String GS_REST_WMS_SETTINGS_ENDPOINT = "/rest/services/wms/settings.json";

    public void migrate(Connection connection) throws IOException, JSONException {
        if(PropertyUtil.getOptional("flyway.1_45_0.skip", false)) {
            LOG.info("Migration to set 'Max rendering memory' WMS setting in GeoServer skipped.");
            return;
        }
        String endPoint = PropertyUtil.getNecessary(PROP_GS_URL) + GS_REST_WMS_SETTINGS_ENDPOINT;
        String user = PropertyUtil.getNecessary(PROP_GS_USER);
        String pass = PropertyUtil.getNecessary(PROP_GS_PASS);
        int maxReqMem = PropertyUtil.getOptional(PROP_MAX_REQ_MEM, DEFAULT_MAX_REQ_MEM_KB);
        try {
            setGeoserverMaxRequestMemory(endPoint, user, pass, maxReqMem);
        } catch (IOException ex) {
            LOG.warn("Couldn't connect to GeoServer for updating the Max rendering memory WMS setting. Tried connecting to",
                    endPoint, ". If you don't have a GeoServer or don't want to update the settings you can skip this",
                    "migration by adding 'flyway.1_45_0.skip=true' to oskari-ext.properties");
            throw ex;
        }
    }

    protected static void setGeoserverMaxRequestMemory(String endPoint,
            String user, String pass, int maxReqMem) throws IOException, JSONException {
        LOG.debug("Trying to set maxRequestMemory to", maxReqMem,
                "settings:", endPoint, user, pass);
        HttpURLConnection conn = IOHelper.getConnection(endPoint, user, pass);
        JSONObject settingsJson = toJSON(IOHelper.readBytes(conn));

        int maxReqMemCurrent = settingsJson.getJSONObject("wms").getInt("maxRequestMemory");
        if (maxReqMem == maxReqMemCurrent) {
            LOG.info("maxRequestMemory is already set to", maxReqMem, "- consider job done");
            return;
        }
        settingsJson.getJSONObject("wms").put("maxRequestMemory", maxReqMem);

        conn = IOHelper.getConnection(endPoint, user, pass);
        IOHelper.put(conn, IOHelper.CONTENT_TYPE_JSON, toUTF8(settingsJson));

        int sc = conn.getResponseCode();
        if (sc == HttpURLConnection.HTTP_OK) {
            LOG.info("Updated maxRequestMemory succesfully from",
                    maxReqMemCurrent, "to", maxReqMem);
        } else {
            LOG.info("Failed to PUT settings.json");
        }
    }

    protected static JSONObject toJSON(byte[] utf8) throws JSONException {
        return new JSONObject(new String(utf8, StandardCharsets.UTF_8));
    }

    protected static byte[] toUTF8(JSONObject json) {
        return json.toString().getBytes(StandardCharsets.UTF_8);
    }

}
