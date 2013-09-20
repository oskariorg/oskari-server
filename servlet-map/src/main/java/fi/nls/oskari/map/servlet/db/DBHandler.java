package fi.nls.oskari.map.servlet.db;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceIbatisImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
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
 */
public class DBHandler {

    private static Connection getConnection() throws SQLException {

        InitialContext ctx = null;
        DataSource ds = null;
        try {
            ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/OskariPool");
        } catch (NamingException e) {
            e.printStackTrace();
        }
        Connection conn = ds.getConnection();
        return conn;
    }

    public static void createContentIfNotCreated() {
        try {
            Connection conn = getConnection();
            DatabaseMetaData dbmeta = conn.getMetaData();

            final String dbName = dbmeta.getDatabaseProductName().replace(' ', '_');
            String[] types = null;

            ResultSet result = dbmeta.getTables(null, null, "PORTTI_%", types);
            final String propertyDropDB = System.getProperty("oskari.dropdb");

            // Portti tables available ?
            if ("true".equals(propertyDropDB) || !result.next()) {
                System.out.println("Creating db for " + dbName);

                createContent(conn, dbName);
                try {
                    conn.commit();
                } catch (SQLException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            result.close();
            //log.debug("db size:" + rs.getFetchSize());
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void createContent(Connection conn, final String dbname) {

        try {
            System.out.println("/ Create DB");

            executeSqlFromFile(conn, dbname, "/00-create-tables.sql");
            System.out.println("/- 00-create-tables.sql");

            System.out.println("/- registering bundles:");
            registerBundle(conn, "framework", "001-openlayers-default-theme.sql");
            registerBundle(conn, "framework", "002-mapfull.sql");
            registerBundle(conn, "framework", "003-divmanazer.sql");
            registerBundle(conn, "framework", "004-toolbar.sql");
            registerBundle(conn, "framework", "005-statehandler.sql");
            registerBundle(conn, "framework", "006-infobox.sql");
            registerBundle(conn, "framework", "007-search.sql");
            registerBundle(conn, "framework", "008-layerselector2.sql");
            registerBundle(conn, "framework", "009-layerselection2.sql");
            registerBundle(conn, "framework", "010-personaldata.sql");
            registerBundle(conn, "framework", "011-publisher.sql");
            registerBundle(conn, "framework", "012-coordinatedisplay.sql");
            registerBundle(conn, "framework", "013-maplegend.sql");
            registerBundle(conn, "framework", "014-userguide.sql");
            registerBundle(conn, "framework", "015-metadataflyout.sql");
            registerBundle(conn, "framework", "016-featuredata.sql");
            registerBundle(conn, "framework", "017-myplaces2.sql");
            registerBundle(conn, "framework", "018-guidedtour.sql");
            registerBundle(conn, "framework", "019-backendstatus.sql");
            registerBundle(conn, "framework", "020-printout.sql");
            registerBundle(conn, "framework", "021-postprocessor.sql");
            registerBundle(conn, "framework", "022-statsgrid.sql");
            registerBundle(conn, "framework", "023-parcel.sql");
            registerBundle(conn, "framework", "024-parcelselector.sql");
            registerBundle(conn, "framework", "025-parcelinfo.sql");
            registerBundle(conn, "framework", "026-promote.sql");
            registerBundle(conn, "framework", "027-publishedgrid.sql");
            registerBundle(conn, "framework", "028-featuredata2.sql");
            registerBundle(conn, "framework", "029-admin-layerrights.sql");

            registerBundle(conn, "integration", "001-admin-layerselector.sql");

            if ("PostgreSQL".equals(dbname)) {
                System.out.println("/- 01-adding view with bundles using ibatis");
                insertView(conn, "default-view.json");
                insertView(conn, "postgres-view.json");
                insertView(conn, "publisher-template-view.json");
            } else {
                executeSqlFromFile(conn, dbname, "/02-create-default-view.sql");
                System.out.println("/- 02-create-default-view.sql");
            }

            executeSqlFromFile(conn, dbname, "exampleLayersAndRoles.sql");
            System.out.println("/-  exampleLayersAndRoles.sql");

        } catch (Exception e) {
            try {
                printQuery("SELECT * FROM portti_bundle", conn);
                printQuery("SELECT * FROM portti_view", conn);
                printQuery("SELECT * FROM portti_view_bundle_seq", conn);
            } catch (SQLException e1) {
                System.out.println("Error printing debug info");
            }
            e.printStackTrace();
        }

    }

    private static long insertView(Connection conn, final String viewfile) throws IOException, SQLException {
        System.out.println("/ - /json/views/" + viewfile);
        String json = IOHelper.readString(DBHandler.class.getResourceAsStream("/json/views/" + viewfile));
        JSONObject view = JSONHelper.createJSONObject(json);
        System.out.println(view);
        try {
            executeSingleSql(conn, "INSERT INTO portti_view_supplement (is_public) VALUES (" + view.getBoolean("public") + ")");
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
                if (bJSON.has("instance")) {
                    bundle.setBundleinstance(bJSON.getString("instance"));
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
            System.out.println("Added view from file: " + viewfile + "/viewId is:" + viewId);
            return viewId;
        } catch (Exception ex) {
            System.err.println("Unable to insert view! ");
            ex.printStackTrace();
        }
        return -1;
    }


    private static void registerBundle(Connection conn, final String namespace, final String bundlefile) throws IOException, SQLException {
        System.out.println("/ - /sql/views/01-bundles/" + namespace + "/" + bundlefile);
        String sqlContents = IOHelper.readString(DBHandler.class.getResourceAsStream("/sql/views/01-bundles/" + namespace + "/" + bundlefile));
        executeMultilineSql(conn, sqlContents);
    }

    private static void executeSqlFromFile(Connection conn, final String dbName, final String fileName) throws IOException, SQLException {
        String sqlFile = readFileAsString(dbName, fileName);
        executeMultilineSql(conn, sqlFile);
    }

    private static void executeMultilineSql(Connection conn, final String sqlContents) throws IOException, SQLException {

        String[] sqlStrings = sqlContents.split(";");
        Statement stmt = conn.createStatement();
        for (String sql : sqlStrings) {
            if (sql.indexOf("--") < 0) {
                stmt.execute(sql);
                conn.commit();
            }
        }
        stmt.close();
    }


    private static void executeSingleSql(Connection conn, final String sql) throws IOException, SQLException {
        final Statement stmt = conn.createStatement();
        if (sql.indexOf("--") < 0) {
            stmt.execute(sql);
            conn.commit();
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

    private static String readFileAsString(final String dbName, final String fileName) throws java.io.IOException {

        try {
            InputStream is = DBHandler.class.getResourceAsStream("/sql/" + dbName + "/" + fileName);
            if (is == null) {
                is = DBHandler.class.getResourceAsStream("/sql/" + fileName);
            }
            return IOHelper.readString(is);
        } catch (Exception ex) {
            System.err.println("Error reading sql file for dbName " + dbName + " and file " + fileName);
            System.err.println("  " + ex.getMessage());
        }
        return "";
    }


    public static void printQuery(final String sql) {
        try {
            printQuery(sql, getConnection());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static void printQuery(String sql, Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery(sql);

        System.out.println("/-----------------------------");

        ResultSetMetaData metaData = rs.getMetaData();
        int count = metaData.getColumnCount();

        for (int i = 1; i < count; i++) {
            System.out.print(metaData.getColumnName(i) + " | ");
        }
        System.out.println();

        while (rs.next()) {

            for (int i = 1; i < count; i++) {
                System.out.print(rs.getString(i) + " | ");
            }
            System.out.println();

        }
        System.out.println("-----------------------------/");
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
