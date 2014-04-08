package fi.nls.oskari.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class JettyRunner {
    public static void main(String[] args) throws Exception {
        System.out.println("JettyRunner.main");

        Server server = new Server(2373);
        ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContext.setContextPath("/");
        servletContext.addServlet(DebugServlet.class, "/");
        server.setHandler(servletContext);
        server.start();
        server.join();
    }
}
