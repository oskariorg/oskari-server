package fi.nls.oskari.jetty;

import fi.nls.oskari.map.servlet.OskariContextInitializer;
import fi.nls.oskari.map.servlet.PrincipalAuthenticationFilter;
import fi.nls.oskari.map.servlet.MapFullServlet;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.naming.NamingException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.HashMap;

public class JettyLauncher {
    public static Server launch(int serverPort,
                                String oskariClientVersion,
                                String jndiDriverClassName,
                                String jndiDbUrl,
                                String jndiDbUsername,
                                String jndiDbPassword,
                                String jndiDbPoolName) throws Exception {
        Server server = new Server(serverPort);
        server.setHandler(createServletContext(oskariClientVersion, jndiDriverClassName, jndiDbUrl, jndiDbUsername, jndiDbPassword, jndiDbPoolName));
        return server;
    }

    private static WebAppContext createServletContext(String oskariClientVersion,
                                                      String jndiDriverClassName,
                                                      String jndiDbUrl,
                                                      String jndiDbUsername,
                                                      String jndiDbPassword,
                                                      String jndiDbPoolName) throws Exception {
        WebAppContext servletContext = new WebAppContext();
        servletContext.setConfigurationClasses(new String[]{"org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration"});
        servletContext.setResourceBase("src/main/webapp");
        servletContext.setContextPath("/");
        servletContext.addEventListener(new OskariContextInitializer());

        // setup JSP/static resources
        servletContext.setBaseResource(createResourceCollection());
        servletContext.addServlet(createFrontEndServlet(), "/Oskari/*");
        servletContext.addServlet(JspServlet.class, "*.jsp");
        servletContext.addServlet(DebugServlet.class, "/debug");

        // map servlet
        servletContext.addServlet(createMapServlet(oskariClientVersion), "/");

        // TODO: replace these with actual impls
        servletContext.addServlet(NotImplementedYetServlet.class, "/transport/*");
        servletContext.addServlet(NotImplementedYetServlet.class, "/geoserver/*");

        setupDatabaseConnectionInContext(servletContext, jndiDriverClassName, jndiDbUrl, jndiDbUsername, jndiDbPassword, jndiDbPoolName);

        setupJaasInContext(servletContext, jndiDbPoolName);

        return servletContext;
    }

    private static void setupJaasInContext(WebAppContext servletContext, String jndiDbPoolName) {
        Configuration.setConfiguration(new JNDILoginConfiguration(jndiDbPoolName));
        servletContext.setSecurityHandler(createJaasSecurityHandler());
        servletContext.addFilter(PrincipalAuthenticationFilter.class, "/", EnumSet.noneOf(DispatcherType.class));
    }

    private static Resource createResourceCollection() throws Exception {
        final String[] paths = {
                PropertyUtil.get("oskari.server.jsp.location", "src/main/webapp"),
                PropertyUtil.get("oskari.client.location", "../..")
        };
        try {
            return new ResourceCollection(paths);
        }
        catch (Exception ex) {
            // TODO: maybe a bit more generic paths handling
            System.out.println("Problem with resource paths: " + paths[0] + " and " + paths[1]);
            throw ex;
        }
    }

    private static ServletHolder createFrontEndServlet() {
        ServletHolder holder = new ServletHolder(DefaultServlet.class);
        holder.setInitParameter("useFileMappedBuffer", "false");
        return holder;
    }

    private static ServletHolder createMapServlet(String oskariClientVersion) {
        ServletHolder holder = new ServletHolder(MapFullServlet.class);
        holder.setInitParameter("version", oskariClientVersion);
        return holder;
    }

    private static void setupDatabaseConnectionInContext(WebAppContext servletContext,
                                                         String jndiDriverClassName,
                                                         String jndiDbUrl,
                                                         String jndiDbUsername,
                                                         String jndiDbPassword,
                                                         String jndiDbPoolName) throws NamingException {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(jndiDriverClassName);
        dataSource.setUrl(jndiDbUrl);
        dataSource.setUsername(jndiDbUsername);
        dataSource.setPassword(jndiDbPassword);
        new EnvEntry(servletContext, jndiDbPoolName, dataSource, true);
    }

    private static SecurityHandler createJaasSecurityHandler() {
        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        JAASLoginService loginService = new JAASLoginService();
        loginService.setName("OskariRealm");
        loginService.setLoginModuleName("oskariLoginModule");
        securityHandler.setLoginService(loginService);
        securityHandler.setAuthenticator(new FormAuthenticator("/", "/?loginState=failed", true));
        securityHandler.setRealmName("OskariRealm");
        return securityHandler;
    }

    private static class JNDILoginConfiguration extends Configuration {
        private final String jndiDbPoolName;

        public JNDILoginConfiguration(String jndiDbPoolName) {
            this.jndiDbPoolName = jndiDbPoolName;
        }

        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            HashMap<String, String> loginModuleOptions = new HashMap<String, String>();
            loginModuleOptions.put("debug", "true");
            loginModuleOptions.put("dbJNDIName", jndiDbPoolName);
            loginModuleOptions.put("userTable", "oskari_jaas_users");
            loginModuleOptions.put("userField", "login");
            loginModuleOptions.put("credentialField", "password");
            loginModuleOptions.put("userRoleTable", "oskari_jaas_roles");
            loginModuleOptions.put("userRoleUserField", "login");
            loginModuleOptions.put("userRoleRoleField", "role");

            return new AppConfigurationEntry[] {
                    new AppConfigurationEntry("org.eclipse.jetty.jaas.spi.DataSourceLoginModule",
                            AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, loginModuleOptions)
            };
        }
    }
}
