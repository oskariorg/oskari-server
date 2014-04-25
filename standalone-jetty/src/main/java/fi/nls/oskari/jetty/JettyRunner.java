package fi.nls.oskari.jetty;

import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.eclipse.jetty.server.Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class JettyRunner {

    public static void main(String[] args) throws Exception {
        PropertyUtil.loadProperties("/oskari.properties");
        PropertyUtil.loadProperties("/oskari-ext.properties");
        loadOverridePropertiesFromFile(System.getProperty("oskari.conf"));

        String username = fromSystemPropertiesOrPropertyUtil("db.username");
        String password = fromSystemPropertiesOrPropertyUtil("db.password");

        Server server = JettyLauncher.launch(
                PropertyUtil.getOptional("oskari.server.port", 2373),
                PropertyUtil.get("oskari.client.version"),
                PropertyUtil.get("db.jndi.driverClassName", "org.postgresql.Driver"),
                PropertyUtil.get("db.url", "jdbc:postgresql://localhost:5432/oskaridb"),
                username,
                password,
                PropertyUtil.get("db.jndi.name", "jdbc/OskariPool"));
        server.start();
        server.join();
    }

    private static String fromSystemPropertiesOrPropertyUtil(String propertyKey) {
        String systemProperty = System.getProperty(propertyKey);
        return systemProperty != null ? systemProperty : PropertyUtil.get(propertyKey);
    }

    private static void loadOverridePropertiesFromFile(final String filename) {
        if(filename == null) {
            loadOverridePropertiesFromFile("./standalone.properties");
            return;
        }

        InputStream in = null;
        try {
            File file = new File(filename);
            if(file.exists() && file.isFile()) {
                System.out.println("Using override properties in: " + file.getAbsolutePath());
            }
            else {
                System.out.println("Using defaults - override properties file not found: " + file.getAbsolutePath());
                return;
            }
            final Properties prop = new Properties();
            in = new FileInputStream(file);
            prop.load(in);
            PropertyUtil.addProperties(prop, true);
        } catch (Exception ignored) {
        } finally {
            IOHelper.close(in);
        }
    }
}
