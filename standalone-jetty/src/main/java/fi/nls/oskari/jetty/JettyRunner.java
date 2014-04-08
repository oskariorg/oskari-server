package fi.nls.oskari.jetty;

import org.apache.commons.dbcp.BasicDataSource;
import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.naming.NamingException;

public class JettyRunner {
    public static void main(String[] args) throws Exception {
        System.out.println("JettyRunner.main");

        Server server = new Server(2373);
        WebAppContext servletContext = createServletContext();
        server.setHandler(servletContext);
        server.start();
        server.join();
    }

    private static WebAppContext createServletContext() throws NamingException {
        WebAppContext servletContext = new WebAppContext();
        servletContext.setConfigurationClasses(new String[]{"org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration"});
        servletContext.setResourceBase("src/main/webapp");
        servletContext.setContextPath("/");
        servletContext.addServlet(DebugServlet.class, "/debug");

        setupDatabaseConnectionInContext(servletContext);

        return servletContext;
    }

    private static void setupDatabaseConnectionInContext(WebAppContext servletContext) throws NamingException {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5433/oskaridb");
        dataSource.setUsername("vagrant");
        dataSource.setPassword("secret");
        new EnvEntry(servletContext, "jdbc/OskariPool", dataSource, true);
    }
}
