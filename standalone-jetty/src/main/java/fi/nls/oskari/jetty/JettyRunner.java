package fi.nls.oskari.jetty;

import fi.nls.oskari.util.DuplicateException;
import fi.nls.oskari.util.PropertyUtil;
import org.eclipse.jetty.server.Server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JettyRunner {
    public static void main(String[] args) throws Exception {
        addProperties("/standalone.properties");

        Server server = JettyLauncher.launch(
                PropertyUtil.getOptional("oskari.server.port", 2373),
                PropertyUtil.get("oskari.client.version"),
                PropertyUtil.get("db.jndi.driverClassName", "org.postgresql.Driver"),
                PropertyUtil.get("db.jndi.url", "jdbc:postgresql://localhost:5432/oskaridb"),
                PropertyUtil.get("db.jndi.username"),
                PropertyUtil.get("db.jndi.password"));
        server.start();
        server.join();
    }

    private static void addProperties(String propertiesFile) throws IOException, DuplicateException {
        Properties properties = new Properties();
        InputStream inputStream = JettyRunner.class.getResourceAsStream(propertiesFile);
        properties.load(inputStream);
        PropertyUtil.addProperties(properties);
    }
}
