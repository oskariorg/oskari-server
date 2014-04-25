package fi.nls.test.util;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyLauncher {
  public static void main(String[] args) throws Exception {
    WebAppContext context = new WebAppContext("src/main/webapp", "/iki");
    context.setExtraClasspath("src/profiles/jetty/lib");
    context.setDescriptor(context + "/WEB-INF/web.xml");
    Server server = new Server(8080);
    server.setHandler(context);
    server.start();
    server.join();
  }
}
