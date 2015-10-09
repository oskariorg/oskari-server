package fi.nls.oskari.db;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.service.db.BaseIbatisService;
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

/**
 * @author EVAARASMAKI
 * @author SMAKINEN
 */
public class DBHandler {

    private static Logger log = LogFactory.getLogger(DBHandler.class);
    private static DataSource datasource;

    public static void main(String[] args) throws Exception {
        // set alternate sqlMapLocation when running on commandline
        BaseIbatisService.setSqlMapLocation("META-INF/SqlMapConfig-content-resources.xml");
        OskariLayerServiceIbatisImpl.setSqlMapLocation("META-INF/SqlMapConfig-content-resources.xml");

        // populate standalone properties
        PropertyUtil.loadProperties("/db.properties");
        final String environment = System.getProperty("oskari.env");
        PropertyUtil.loadProperties("/db-" + environment + ".properties");
        PropertyUtil.loadProperties("/oskari.properties");
        PropertyUtil.loadProperties("/oskari-ext.properties");

        // allow specifying extra properties file as a command line argument
        if (args.length > 0 && args[0].endsWith(".properties")) {
            Properties props = new Properties();
            FileInputStream in = new FileInputStream(args[0]);
            props.load(in);
            in.close();
            PropertyUtil.addProperties(props, true);
        }

        // replace logger after properties are populated
        log = LogFactory.getLogger(DBHandler.class);

        final DatasourceHelper helper = DatasourceHelper.getInstance();
        try {
            datasource = helper.createDataSource();
            final String addView = System.getProperty("oskari.addview");
            final String addLayer = System.getProperty("oskari.addlayer");
            if(addView != null) {
                ViewHelper.insertView(getConnection(), addView);
            }
            else if(addLayer != null) {
                final int id = LayerHelper.setupLayer(addLayer);
                log.debug("Added layer with id:", id);
            }
            else {
                // initialize db with demo data if tables are not present
                createContentIfNotCreated();
            }

        } finally {
            helper.teardown();
        }
    }

    /**
     * Returns connection based on db.properties.
     * Prefers a datasource -> property 'db.jndi.name' (defaults to "jdbc/OskariPool").
     * Falls back to db url -> property 'db.url' (defaults to "jdbc:postgresql://localhost:5432/oskaridb")
     * Username and password ('db.username' and 'db.password' properties)
     * @return connection to the database
     * @throws SQLException if connection cannot be fetched
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static DataSource getDataSource() throws SQLException {
        return datasource;
    }

    public static void createContentIfNotCreated() {
        try {
            createContentIfNotCreated(getDataSource());
        }
        catch (Exception ex) {
            log.error(ex, "Couldn't get connection to db!");
        }
    }

    public static void createContentIfNotCreated(DataSource ds) {
        try {
            datasource = ds;
            Connection conn = ds.getConnection();
            DatabaseMetaData dbmeta = conn.getMetaData();

            final String dbName = dbmeta.getDatabaseProductName().replace(' ', '_');

            final ResultSet result = dbmeta.getTables(null, null, "portti_%", null);
            final ResultSet result2 = dbmeta.getTables(null, null, "PORTTI_%", null);
            final String propertyDropDB = System.getProperty("oskari.dropdb");

            final boolean tablesExist = result.next() || result2.next();
            // Portti tables available ?
            if ("true".equals(propertyDropDB) || !tablesExist) {
                log.info("Creating db for " + dbName);

                createContent(conn, dbName);
                try {
                    if(!conn.getAutoCommit()) {
                        conn.commit();
                    }
                } catch (SQLException e) {
                    log.error(e, "Couldn't commit changes!");
                }
            }
            else {
                log.info("Existing tables found in db. Use 'oskari.dropdb=true' system property to override.");
            }

            result.close();
        } catch (Exception e) {
            log.error(e, "Error creating db content!");
        }
    }
    private static void createContent(Connection conn, final String dbname) throws IOException{
        final String setup = ConversionHelper.getString(System.getProperty("oskari.setup"), "app-default");
        createContent(conn, dbname, setup);
    }

    private static void createContent(Connection conn, final String dbname, final String setupFile) throws IOException{
            String propertySetupFile = "/setup/" + setupFile;
            if(!setupFile.toLowerCase().endsWith(".json")) {
                // accept setup file without file extension
                propertySetupFile = propertySetupFile+ ".json";
            }

            String setupJSON = IOHelper.readString(getInputStreamFromResource(propertySetupFile));
            if(setupJSON == null || setupJSON.isEmpty()) {
                throw new RuntimeException("Error reading file " + propertySetupFile);
            }

            log.info("/ Initializing DB");
            final JSONObject setup = JSONHelper.createJSONObject(setupJSON);
            createContent(conn, dbname, setup);

    }

    @SuppressWarnings("resource")
    static InputStream getInputStreamFromResource(String propertySetupFile) {
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

    private static void createContent(Connection conn, final String dbname, final JSONObject setup) throws IOException{

        try {
            if(setup.has("create")) {
                log.info("/- running create scripts:");
                final JSONArray createScripts = setup.getJSONArray("create");
                for(int i = 0; i < createScripts.length(); ++i) {
                    final String sqlFileName = createScripts.getString(i);
                    System.out.println("/-  " + sqlFileName);
                    executeSqlFromFile(conn, dbname, sqlFileName);
                }
                log.info("/- Created tables");
            }

            if(setup.has("setup")) {
                log.info("/- running recursive setups:");
                final JSONArray setupFiles = setup.getJSONArray("setup");
                for(int i = 0; i < setupFiles.length(); ++i) {
                    final Object tmp = setupFiles.get(i);
                    if (tmp instanceof JSONObject) {
                        final JSONObject setupObj = (JSONObject) tmp;
                        log.info("/-  as inline JSON");
                        createContent(conn, dbname, setupObj);
                    }
                    else {
                        final String setupFileName = (String) tmp;
                        log.info("/-  " + setupFileName);
                        createContent(conn, dbname, setupFileName);
                    }
                }
                log.info("/- recursive setups complete");
            }

            if(setup.has("bundles")) {
                log.info("/- registering bundles:");
                final JSONObject bundlesSetup = setup.getJSONObject("bundles");
                final JSONArray namespaces = bundlesSetup.names();
                for(int namespaceIndex = 0; namespaceIndex < namespaces.length(); ++namespaceIndex) {
                    final String namespace = namespaces.getString(namespaceIndex);
                    final JSONArray namespaceBundles = bundlesSetup.getJSONArray(namespace);
                    for(int i = 0; i < namespaceBundles.length(); ++i) {
                        final String bundlesql = namespaceBundles.getString(i);
                        registerBundle(conn, namespace, bundlesql);
                    }
                }
            }

            // needed after db created so views/layers can be registered correctly using services
            if(setup.optBoolean("flyway", false)) {
                log.info("/- flyway migration for core db");
                try {
                    FlywaydbMigrator.migrate(getDataSource());
                    log.info("Oskari core DB migrated successfully");
                } catch (Exception e) {
                    log.error(e, "DB migration for Oskari core failed!");
                }
            }

            if(setup.has("views")) {
                log.info("/- adding views using ibatis");
                final JSONArray viewsListing = setup.getJSONArray("views");
                for(int i = 0; i < viewsListing.length(); ++i) {
                    final String viewConfFile = viewsListing.getString(i);
                    ViewHelper.insertView(conn, viewConfFile);
                }
            }
            if(setup.has("layers")) {
                log.info("/- adding layers using ibatis");
                final JSONArray layersListing = setup.getJSONArray("layers");
                for(int i = 0; i < layersListing.length(); ++i) {
                    final String layerConfFile = layersListing.getString(i);
                    LayerHelper.setupLayer(layerConfFile);
                }
            }


            if(setup.has("sql")) {
                log.info("/- running additional sql files");
                final JSONArray viewsListing = setup.getJSONArray("sql");
                for(int i = 0; i < viewsListing.length(); ++i) {
                    final String sqlFileName = viewsListing.getString(i);
                    System.out.println("/-  " + sqlFileName);
                    executeSqlFromFile(conn, dbname, sqlFileName);
                }
            }

        } catch (Exception e) {
            log.error(e, "Error creating content");
        }
    }



    private static void registerBundle(Connection conn, final String namespace, final String bundlefile) throws IOException, SQLException {
        log.info("/ - /sql/views/01-bundles/" + namespace + "/" + bundlefile);
        String sqlContents = IOHelper.readString(getInputStreamFromResource("/sql/views/01-bundles/" + namespace + "/" + bundlefile));
        executeMultilineSql(conn, sqlContents);
    }

    private static void executeSqlFromFile(Connection conn, final String dbName, final String fileName) throws IOException, SQLException {
        String sqlFile = readSQLFileAsString(dbName, fileName);
        executeMultilineSql(conn, sqlFile);
    }

    private static void executeMultilineSql(Connection conn, final String sqlContents) throws IOException, SQLException {

        String[] sqlStrings = sqlContents.split(";");
        Statement stmt = conn.createStatement();
        for (String sql : sqlStrings) {
            if (!sql.contains("--")) {
                stmt.execute(sql);
                if(!conn.getAutoCommit()) {
                    conn.commit();
                }
            }
        }
        stmt.close();
    }

    private static String readSQLFileAsString(final String dbName, final String fileName) throws java.io.IOException {

        try {
            InputStream is = getInputStreamFromResource("/sql/" + dbName + "/" + fileName);
            if (is == null) {
                is = getInputStreamFromResource("/sql/" + fileName);
                log.info("   file: /sql/" + fileName);
            }
            else {
                log.info("   file: /sql/" + dbName + "/" + fileName);
            }
            return IOHelper.readString(is);
        } catch (Exception ex) {
            log.error("Error reading sql file for dbName", dbName, "and file", fileName, "  ", ex.getMessage());
        }
        return "";
    }
}
