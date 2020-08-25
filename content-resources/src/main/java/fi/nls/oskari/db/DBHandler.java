package fi.nls.oskari.db;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.Set;

/**
 * @author EVAARASMAKI
 * @author SMAKINEN
 */
public class DBHandler {

    private static Logger log;

    private static Logger getLog() {
        if (log == null) {
            log = LogFactory.getLogger(DBHandler.class);
        }
        return log;
    }

    public static void setupAppContent(Connection conn, final String setupFile) throws IOException {
        createContent(conn, "PostgreSQL", setupFile);
    }

    private static void createContent(Connection conn, final String dbname, final String setupFile) throws IOException {
        if (setupFile == null) {
            getLog().info("No setup file configured. Skipping content creation.");
            return;
        }
        String propertySetupFile = "/setup/" + setupFile;
        if (!setupFile.toLowerCase().endsWith(".json")) {
            // accept setup file without file extension
            propertySetupFile = propertySetupFile + ".json";
        }

        String setupJSON = IOHelper.readString(getInputStreamFromResource(propertySetupFile));
        if (setupJSON == null || setupJSON.isEmpty()) {
            throw new RuntimeException("Error reading file " + propertySetupFile);
        }

        getLog().info("/ Initializing DB");
        final JSONObject setup = JSONHelper.createJSONObject(setupJSON);
        createContent(conn, dbname, setup);
    }

    private static void createContent(Connection conn, final String dbname, final JSONObject setup) throws IOException{

        try {
            if(setup.has("views")) {
                getLog().info("/- adding views using ibatis");
                final JSONArray viewsListing = setup.getJSONArray("views");
                for(int i = 0; i < viewsListing.length(); ++i) {
                    final String viewConfFile = viewsListing.getString(i);
                    ViewHelper.insertView(conn, viewConfFile);
                }
            }
            if(setup.has("layers")) {
                getLog().info("/- adding layers using ibatis");
                final JSONArray layersListing = setup.getJSONArray("layers");
                for(int i = 0; i < layersListing.length(); ++i) {
                    final String layerConfFile = layersListing.getString(i);
                    LayerHelper.setupLayer(layerConfFile);
                }
            }
        } catch (Exception e) {
            getLog().error(e, "Error creating content");
            throw new ServiceRuntimeException("Unable to process setup file", e);
        }
    }

    @SuppressWarnings("resource")
    public static InputStream getInputStreamFromResource(String propertySetupFile) {
        InputStream is = null;
        try {
            // If resource overlay directory has been specified prefer the files in there
            // over those shipped with Oskari.
            String resourceOverlayDirectory = System.getProperty("oskari.resourceOverlayDir");
            if (resourceOverlayDirectory != null) {
                is = new FileInputStream(resourceOverlayDirectory + propertySetupFile);
            }
        } catch (FileNotFoundException e) {
            // do nothing
        }
        if (is == null) {
            is = DBHandler.class.getResourceAsStream(propertySetupFile);
        }
        return is;
    }
}
