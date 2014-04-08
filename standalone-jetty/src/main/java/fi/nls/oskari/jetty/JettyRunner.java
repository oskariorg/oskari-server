package fi.nls.oskari.jetty;

import fi.nls.oskari.map.servlet.MapFullServlet;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
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

        servletContext.setBaseResource(createResourceCollection());

        servletContext.addServlet(createFrontEndServlet(), "/Oskari/*");
        servletContext.addServlet(JspServlet.class, "*.jsp");
        servletContext.addServlet(DebugServlet.class, "/debug");
        servletContext.addServlet(createMapServlet(), "/");

        setupDatabaseConnectionInContext(servletContext);

        return servletContext;
    }

    private static Resource createResourceCollection() {
        ResourceCollection collection = new ResourceCollection();
        collection.setResourcesAsCSV("src/main/webapp,../..");
        return collection;
    }

    private static ServletHolder createFrontEndServlet() {
        ServletHolder holder = new ServletHolder(DefaultServlet.class);
        holder.setInitParameter("useFileMappedBuffer", "false");
        return holder;
    }

    private static ServletHolder createMapServlet() {
        ServletHolder holder = new ServletHolder(MapFullServlet.class);
        // TODO: Read oskari.client.version from properties and set to init parameter
        holder.setInitParameter("version", "ADD_VERSION_NUMBER_HERE");
        return holder;
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
