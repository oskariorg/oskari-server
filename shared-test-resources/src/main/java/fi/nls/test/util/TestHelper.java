package fi.nls.test.util;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.PropertyUtil;
import org.h2.jdbcx.JdbcDataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

/**
 * Helper for checking if we have correct proxy settings available. Tests can use
 * assume(TestHelper.canDoHttp()) to ignore a test if http is not available.
 */
public class TestHelper {

    public static final String DB_PROPS_KEY = "oskari_db_test_props";

    public static final String TEST_URL = "http://httpbin.org/ip";
    private static enum STATUS {
        NONE,
        ENABLED,
        DISABLED;

        public static STATUS getEnabled(boolean enabled) {
            return enabled ? ENABLED : DISABLED;
        }
    };
    private static STATUS networkingStatus = STATUS.NONE;
    private static STATUS redisStatus = STATUS.NONE;
    private static STATUS dbStatus = STATUS.NONE;

    /**
     * Tests network connectivity by polling #TEST_URL with 3 second timeout.
     * @return
     */
    public static boolean canDoHttp() {
        if(networkingStatus.equals(STATUS.NONE)) {
            // check status if not yet checked
            try {
                URL url = new URL(TEST_URL);
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setReadTimeout(3000);
                urlConn.connect();

                networkingStatus = STATUS.getEnabled(HttpURLConnection.HTTP_OK == urlConn.getResponseCode());
            } catch (IOException e) {
                System.err.println("Couldn't connect to test URL (" + TEST_URL + "). Skipping http tests.");
                networkingStatus = STATUS.DISABLED;
            }
        }
        return networkingStatus.equals(STATUS.ENABLED);
    }

    public static boolean redisAvailable() {
        if(redisStatus.equals(STATUS.NONE)) {
            JedisManager.connect(1, "localhost", 6379);
            final String testKey = "testing";
            final String testValue = "availability";
            try {
                final String msg = JedisManager.setex(testKey, 10, testValue);
                if(msg == null) {
                    redisStatus = STATUS.DISABLED;
                }
                redisStatus = STATUS.getEnabled(testValue.equals(JedisManager.get(testKey)));
            } catch (ServiceRuntimeException ex) {
                redisStatus = STATUS.DISABLED;
            }
        }
        return redisStatus.equals(STATUS.ENABLED);
    }

    public static boolean dbAvailable() {
        if(dbStatus.equals(STATUS.NONE)) {
            DataSource ds = getDBforUnitTest();
            try {
                if(ds == null || ds.getConnection() == null) {
                    dbStatus = STATUS.DISABLED;
                } else {
                    dbStatus = STATUS.ENABLED;
                }
            } catch (SQLException ex) {
                dbStatus = STATUS.DISABLED;
            }
        }
        return dbStatus.equals(STATUS.ENABLED);
    }

    public static DataSource getDBforUnitTest() {
        String propFileLocation = System.getenv(DB_PROPS_KEY);
        if(propFileLocation == null) {
            propFileLocation = System.getProperty(DB_PROPS_KEY);
            if(propFileLocation == null) {
                return null;
            }
        }
        File file = new File(propFileLocation);
        try (FileInputStream fis = new FileInputStream(file)){
            Properties prop = new Properties();
            prop.load(fis);
            PropertyUtil.addProperties(prop, true);
            return DatasourceHelper.getInstance().createDataSource();
        } catch (Exception ex) {
            System.err.println("Error reading properties from " + propFileLocation);
        }
        return null;
    }

    public static DataSource createMemDBforUnitTest() throws SQLException {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1");
        // in theory we should be able to initialize database schema/tables by calling
        // FlywaydbMigrator.migrate(ds);
        // BUT there's an issue with cyclic dependencies for Maven that we need to
        // deal with first to get this running
        return ds;
    }


    public static void registerTestDataSource() throws SQLException {
        registerTestDataSource(createMemDBforUnitTest());
    }

    public static void registerTestDataSource(final DataSource ds) {
        try {
            DatasourceHelper.getInstance()
                    .registerDataSource(
                            DatasourceHelper.DEFAULT_DATASOURCE_NAME, ds);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void teardown() {
        DatasourceHelper.getInstance().teardown();
    }


    public static DataSource createMemDBforUnitTest(List<String> sqlStatementsForInit) throws SQLException {
        DataSource ds = createMemDBforUnitTest();
        if (sqlStatementsForInit != null) {
            try (Connection c = ds.getConnection();
                 Statement s = c.createStatement()) {
                for (String sql : sqlStatementsForInit) {
                    s.execute(sql);
                }
            }
        }
        return ds;
    }
}
