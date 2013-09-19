package fi.nls.oskari.map.servlet.db;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.*;
import java.sql.*;

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
            // Portti tables available ?
            if (!result.next()) {

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
            executeSqlFromFile(conn, "/sql/" + dbname + "/exampleLayersAndRoles.sql");
            System.out.println("/-  exampleLayersAndRoles.sql");

            executeSqlFromFile(conn, "/sql/" + dbname + "/00-create-tables.sql");
            System.out.println("/- 00-create-tables.sql");

            executeSqlFromFile(conn, "/sql/" + dbname + "/01-register-bundles.sql");
            System.out.println("/- 01-register-bundles.sql");

            executeSqlFromFile(conn, "/sql/" + dbname + "/02-create-default-view.sql");
            System.out.println("/- 02-create-default-view.sql");

        } catch (Exception e) {
            try {
                printQuery("SELECT * FROM portti_bundle", conn);
                printQuery("SELECT * FROM portti_view", conn);
                printQuery("SELECT * FROM portti_view_bundle_seq", conn);
            } catch (SQLException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            e.printStackTrace();
        }

    }

    private static void executeSqlFromFile(Connection conn, String fileName) throws IOException, SQLException {

        String[] sqlStrings = readFileAsString(fileName).split(";");
        Statement stmt = conn.createStatement();
        for (String sql : sqlStrings) {
            if (sql.indexOf("--") < 0) {
                stmt.execute(sql);
                conn.commit();
            }
        }

        stmt.close();
    }


    private static String readFileAsString(String file) throws java.io.IOException {

        InputStream is = DBHandler.class.getResourceAsStream(file);
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
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
}
