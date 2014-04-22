package fi.nls.oskari.db;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceIbatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author EVAARASMAKI
 * @author SMAKINEN
 */
public class DBHandler {

    private static Logger log = LogFactory.getLogger(DBHandler.class);

    public static void main(String[] args) throws Exception {
        // populate standalone properties
        InputStream in = null;
        try {
            final Properties prop = new Properties();
            in = DBHandler.class.getResourceAsStream("/db.properties");
            prop.load(in);
            PropertyUtil.addProperties(prop);
        } catch (Exception e) {
            System.out.println("Error when populating properties!");
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (Exception ignored) { }
        }
        final String environment = System.getProperty("oskari.env");
        if(environment != null && !environment.isEmpty()) {
            try {
                final Properties prop = new Properties();
                in = DBHandler.class.getResourceAsStream("/db-" + environment + ".properties");
                prop.load(in);
                PropertyUtil.addProperties(prop, true);
            } catch (Exception ignored) {
                System.out.println("Error when populating env-properties for '" + environment + "'!!");
            } finally {
                try {
                    in.close();
                } catch (Exception ignored) { }
            }
        }

        // replace logger after properties are populated
        log = LogFactory.getLogger(DBHandler.class);

        final String addView = System.getProperty("oskari.addview");
        if(addView != null) {
            insertView(getConnection(), addView);
        }
        else {
            // initialize db with demo data if tables are not present
            createContentIfNotCreated();
        }
    }

    public static void debugPrintDBContents() {
        // Enable to show db contents for view related tables if an error occurs
        printQuery("SELECT * FROM portti_bundle");
        printQuery("SELECT * FROM portti_view_supplement");
        printQuery("SELECT * FROM portti_view");
        printQuery("SELECT * FROM portti_view_bundle_seq");

        // Enable to show db contents for layer permissions related tables if an error occurs
        printQuery("SELECT * FROM portti_resource_user");
        printQuery("SELECT * FROM portti_permissions");
    }

    /**
     * Returns connection based on db.properties.
     * Prefers a datasource -> property 'datasource' (defaults to "jdbc/OskariPool").
     * Falls back to db url -> property 'url' (defaults to "jdbc:postgresql://localhost:5432/oskaridb")
     * Username and password ('user' and 'pass' properties)
     * @return connection to the database
     * @throws SQLException if connection cannot be fetched
     */
    public static Connection getConnection() throws SQLException {

        final String datasource = PropertyUtil.get("datasource", "jdbc/OskariPool");
        try {
            final InitialContext ctx = new InitialContext();
            final DataSource ds = (DataSource) ctx.lookup("java:/comp/env/" + datasource);
            return ds.getConnection();
        } catch (Exception e) {
        }

        final String url = PropertyUtil.get("url", "jdbc:postgresql://localhost:5432/oskaridb");
        try {
            final Properties connectionProps = new Properties();
            final String user = PropertyUtil.getOptional("user");
            if(user != null) connectionProps.put("user", user);

            final String pass = PropertyUtil.getOptional("pass");
            if(pass != null) connectionProps.put("password", pass);

            overrideConnectionPropertiesFromSystemProperties(connectionProps);

            final Connection conn = DriverManager.getConnection(url, connectionProps);
            if(conn != null) {
                log.info("Using connection:", url);
                return conn;
            }
        } catch (Exception e) {
        }
        throw new SQLException("Couldn't get db connection! Tried with datasource: "
                +datasource + " and url: " + url + ". Aborting...");
    }

    private static void overrideConnectionPropertiesFromSystemProperties(Properties connectionProps) {
        overridePropertyIfNotNull(connectionProps, "user", System.getProperty("db.username"));
        overridePropertyIfNotNull(connectionProps, "password", System.getProperty("db.password"));
    }

    private static void overridePropertyIfNotNull(Properties properties, String propertyName, String propertyValue) {
        if(propertyValue != null) properties.put(propertyName, propertyValue);
    }

    public static void createContentIfNotCreated() {
        try {
            Connection conn = getConnection();
            DatabaseMetaData dbmeta = conn.getMetaData();

            final String dbName = dbmeta.getDatabaseProductName().replace(' ', '_');
            String[] types = null;

            final ResultSet result = dbmeta.getTables(null, null, "portti_%", types);
            final ResultSet result2 = dbmeta.getTables(null, null, "PORTTI_%", types);
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
            else if(tablesExist) {
                log.info("Existing tables found in db. Use 'oskari.dropdb=true' system property to override.");
            }

            result.close();
        } catch (Exception e) {
            log.error(e, "Error creating db content!");
        }
    }
    private static void createContent(Connection conn, final String dbname) {
        final String setup = ConversionHelper.getString(System.getProperty("oskari.setup"), "app-default");
        createContent(conn, dbname, setup);
    }

    private static void createContent(Connection conn, final String dbname, final String setupFile) {

        try {

            String propertySetupFile = "/setup/" + setupFile;
            if(!setupFile.toLowerCase().endsWith(".json")) {
                // accept setup file without file extension
                propertySetupFile = propertySetupFile+ ".json";
            }

            String setupJSON = IOHelper.readString(DBHandler.class.getResourceAsStream(propertySetupFile));
            if(setupJSON == null || setupJSON.isEmpty()) {
                throw new RuntimeException("Error reading file " + propertySetupFile);
            }

            log.info("/ Initializing DB");
            final JSONObject setup = JSONHelper.createJSONObject(setupJSON);
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
                    final String setupFileName = setupFiles.getString(i);
                    System.out.println("/-  " + setupFileName);
                    createContent(conn, dbname, setupFileName);
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
            if(setup.has("views")) {
                log.info("/- adding views using ibatis");
                final JSONArray viewsListing = setup.getJSONArray("views");
                for(int i = 0; i < viewsListing.length(); ++i) {
                    final String viewConfFile = viewsListing.getString(i);
                    insertView(conn, viewConfFile);
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

    private static long insertView(Connection conn, final String viewfile) throws IOException, SQLException {
        log.info("/ - /json/views/" + viewfile);
        String json = IOHelper.readString(DBHandler.class.getResourceAsStream("/json/views/" + viewfile));
        JSONObject view = JSONHelper.createJSONObject(json);
        log.debug(view);
        try {
            final String creator = view.optString("creator");
            executeSingleSql(conn, "INSERT INTO portti_view_supplement (is_public, creator) VALUES (" + view.getBoolean("public") +
                    "," + ConversionHelper.getLong(creator, -1) + " )");
            Map<String, String> supplementResult = selectSql(conn, "SELECT max(id) as id FROM portti_view_supplement");
            final long supplementId = ConversionHelper.getLong(supplementResult.get("id"), -1);

            final String insertViewSQL = "INSERT INTO portti_view (name, type, is_default, supplement_id, application, page, application_dev_prefix) " +
                    "VALUES (?,?,?,?,?,?,?)";
            PreparedStatement preparedStatement = conn.prepareStatement(insertViewSQL);
            preparedStatement.setString(1, view.getString("name"));
            preparedStatement.setString(2, view.getString("type"));
            preparedStatement.setBoolean(3, view.getBoolean("default"));
            preparedStatement.setLong(4, supplementId);
            final JSONObject oskari = JSONHelper.getJSONObject(view, "oskari");
            preparedStatement.setString(5, oskari.getString("application"));
            preparedStatement.setString(6, oskari.getString("page"));
            preparedStatement.setString(7, oskari.getString("development_prefix"));
            preparedStatement.executeUpdate();
            preparedStatement.close();

            Map<String, String> viewResult = selectSql(conn, "SELECT max(id) as id FROM portti_view");
            final long viewId = ConversionHelper.getLong(viewResult.get("id"), -1);

            final ViewService viewService = new ViewServiceIbatisImpl();
            final BundleService bundleService = new BundleServiceIbatisImpl();
            final View viewObj = viewService.getViewWithConf(viewId);
            final JSONArray bundles = view.getJSONArray("bundles");
            for (int i = 0; i < bundles.length(); ++i) {
                final JSONObject bJSON = bundles.getJSONObject(i);
                final Bundle bundle = bundleService.getBundleTemplateByName(bJSON.getString("id"));
                if(bundle == null) {
                    throw new Exception("Bundle not registered - id:" + bJSON.getString("id"));
                }
                if (bJSON.has("instance")) {
                    bundle.setBundleinstance(bJSON.getString("instance"));
                }
                if (bJSON.has("startup")) {
                    bundle.setStartup(bJSON.getJSONObject("startup").toString());
                }
                if (bJSON.has("config")) {
                    bundle.setConfig(bJSON.getJSONObject("config").toString());
                }
                if (bJSON.has("state")) {
                    bundle.setState(bJSON.getJSONObject("state").toString());
                }
                // set up seq number
                viewObj.addBundle(bundle);
                viewService.addBundleForView(viewId, bundle);
            }
            log.info("Added view from file: " + viewfile + "/viewId is:" + viewId);
            return viewId;
        } catch (Exception ex) {
            log.error(ex, "Unable to insert view! ");
        }
        return -1;
    }


    private static void registerBundle(Connection conn, final String namespace, final String bundlefile) throws IOException, SQLException {
        log.info("/ - /sql/views/01-bundles/" + namespace + "/" + bundlefile);
        String sqlContents = IOHelper.readString(DBHandler.class.getResourceAsStream("/sql/views/01-bundles/" + namespace + "/" + bundlefile));
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
            if (sql.indexOf("--") < 0) {
                stmt.execute(sql);
                if(!conn.getAutoCommit()) {
                    conn.commit();
                }
            }
        }
        stmt.close();
    }


    private static void executeSingleSql(Connection conn, final String sql) throws IOException, SQLException {
        final Statement stmt = conn.createStatement();
        if (sql.indexOf("--") < 0) {
            stmt.execute(sql);
            if(!conn.getAutoCommit()) {
                conn.commit();
            }
        }
        stmt.close();
    }

    private static Map<String, String> selectSql(Connection conn, final String query) throws IOException, SQLException {
        final Map<String, String> result = new HashMap<String, String>();
        final Statement stmt = conn.createStatement();

        final ResultSet rs = stmt.executeQuery(query);
        final int columns = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            for (int i = 1; i <= columns; ++i) {
                final String name = rs.getMetaData().getColumnName(i);
                final String value = rs.getString(i);
                result.put(name, value);
            }
        }
        stmt.close();
        rs.close();
        return result;
    }

    private static String readSQLFileAsString(final String dbName, final String fileName) throws java.io.IOException {

        try {
            InputStream is = DBHandler.class.getResourceAsStream("/sql/" + dbName + "/" + fileName);
            if (is == null) {
                is = DBHandler.class.getResourceAsStream("/sql/" + fileName);
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


    public static void printQuery(final String sql) {
        try {
            printQuery(sql, getConnection());
        } catch (Exception ex) {
            log.warn(ex, "Error printing query");
        }
    }


    public static void printQuery(String sql, Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery(sql);

        log.info("/-----------------------------");

        ResultSetMetaData metaData = rs.getMetaData();
        int count = metaData.getColumnCount();

        for (int i = 1; i < count; i++) {
            log.info(metaData.getColumnName(i) + " | ");
        }
        log.info("--");

        while (rs.next()) {

            for (int i = 1; i < count; i++) {
                log.info(rs.getString(i) + " | ");
            }
            log.info("--");

        }
        log.info("-----------------------------/");
    }

    /**
     * List directory contents for a resource folder. Not recursive.
     * This is basically a brute-force implementation.
     * Works for regular files and also JARs.
     *
     * @param clazz Any java class that lives in the same place as the resources you want.
     * @param path  Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws IOException
     * @author Greg Briggs
     */
    private static String[] getResourceListing(Class clazz, String path) {
        try {
            URL dirURL = clazz.getClassLoader().getResource(path);
            if (dirURL != null && dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough */
                return new File(dirURL.toURI()).list();
            }

            if (dirURL == null) {
            /*
             * In case of a jar file, we can't actually find a directory.
             * Have to assume the same jar as clazz.
             */
                String me = clazz.getName().replace(".", "/") + ".class";
                dirURL = clazz.getClassLoader().getResource(me);
            }

            if (dirURL.getProtocol().equals("jar")) {
            /* A JAR path */
                String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
                JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
                Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
                Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
                while (entries.hasMoreElements()) {
                    String name = entries.nextElement().getName();
                    if (name.startsWith(path)) { //filter according to the path
                        String entry = name.substring(path.length());
                        int checkSubdir = entry.indexOf("/");
                        if (checkSubdir >= 0) {
                            // if it is a subdirectory, we just return the directory name
                            entry = entry.substring(0, checkSubdir);
                        }
                        result.add(entry);
                    }
                }
                return result.toArray(new String[result.size()]);
            }

            throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        ;
        return new String[0];
    }
}
