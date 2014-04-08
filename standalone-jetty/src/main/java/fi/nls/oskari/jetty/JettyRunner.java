package fi.nls.oskari.jetty;

import org.eclipse.jetty.server.Server;

public class JettyRunner {
    public static void main(String[] args) throws Exception {
        System.out.println("JettyRunner.main");

        Server server = new Server(2373);
        server.setHandler(new DebugHandler());
        server.start();
        server.join();
    }
}
