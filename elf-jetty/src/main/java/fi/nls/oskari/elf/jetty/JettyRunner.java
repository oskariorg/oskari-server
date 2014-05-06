package fi.nls.oskari.elf.jetty;

import fi.nls.oskari.jetty.JettyLauncher;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;

import java.net.URL;
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
        
        
        Server server = new Server(PropertyUtil.getOptional("oskari.server.port", 2373));
        
        WebAppContext main = 
                JettyLauncher.createServletContext(                       
                        PropertyUtil.get("oskari.client.version"),
                        PropertyUtil.get("db.jndi.driverClassName", "org.postgresql.Driver"),
                        PropertyUtil.get("db.url", "jdbc:postgresql://localhost:5432/oskaridb"),
                        username,
                        password,
                        PropertyUtil.get("db.jndi.name", "jdbc/OskariPool"));

        /* ELF additional stuff */
        WebAppContext geonetwork = new WebAppContext();
        geonetwork.setServer(server);
        geonetwork.setContextPath("/geonetwork");
     
        URL location = JettyRunner.class.getResource("/webapps/geonetwork.war");
        geonetwork.setWar(location.toExternalForm());
        geonetwork.setAttribute("org.eclipse.jetty.webapp.basetempdir", "./tmp");
        geonetwork.setTempDirectory(new java.io.File("./tmp"));
        geonetwork.setClassLoader(Thread.currentThread().getContextClassLoader());
        
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { main, geonetwork });
        server.setHandler(handlers);
        server.start();
        server.join();
    }

    private static String fromSystemPropertiesOrPropertyUtil(String propertyKey) {
        String systemProperty = System.getProperty(propertyKey);
        return systemProperty != null ? systemProperty : PropertyUtil.get(propertyKey);
    }

    private static void loadOverridePropertiesFromFile(final String filename) {
        if(filename == null) {
            loadOverridePropertiesFromFile("./elf.properties");
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
            System.out.println(prop);
            PropertyUtil.addProperties(prop, true);
        } catch (Exception ignored) {
        } finally {
            IOHelper.close(in);
        }
    }
}
