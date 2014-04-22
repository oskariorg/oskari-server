package fi.nls.oskari.jetty;

import fi.nls.oskari.util.PropertyUtil;
import org.eclipse.jetty.server.Server;

public class JettyRunner {
    public static void main(String[] args) throws Exception {
        PropertyUtil.loadProperties("/oskari.properties");
        PropertyUtil.loadProperties("/oskari-ext.properties");
        PropertyUtil.loadProperties("/standalone.properties");

        String username = fromSystemPropertiesOrPropertyUtil("db.username", "db.jndi.username");
        String password = fromSystemPropertiesOrPropertyUtil("db.password", "db.jndi.password");

        Server server = JettyLauncher.launch(
                PropertyUtil.getOptional("oskari.server.port", 2373),
                PropertyUtil.get("oskari.client.version"),
                PropertyUtil.get("db.jndi.driverClassName", "org.postgresql.Driver"),
                PropertyUtil.get("db.jndi.url", "jdbc:postgresql://localhost:5432/oskaridb"),
                username,
                password,
                PropertyUtil.get("db.jndi.name", "jdbc/OskariPool"));
        server.start();
        server.join();
    }

    private static String fromSystemPropertiesOrPropertyUtil(String systemPropertyKey, String propertyUtilKey) {
        String systemProperty = System.getProperty(systemPropertyKey);
        return systemProperty != null ? systemProperty : PropertyUtil.get(propertyUtilKey);
    }
}
