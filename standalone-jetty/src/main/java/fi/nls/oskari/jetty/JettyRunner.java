package fi.nls.oskari.jetty;

import fi.nls.oskari.map.servlet.JaasAuthenticationFilter;
import fi.nls.oskari.map.servlet.MapFullServlet;
import fi.nls.oskari.util.DuplicateException;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Properties;

public class JettyRunner {
    public static void main(String[] args) throws Exception {
        addProperties("/standalone.properties");

        Server server = new Server(PropertyUtil.getOptional("oskari.server.port", 2373));
        server.setHandler(createServletContext());
        server.start();
        server.join();
    }

    private static void addProperties(String propertiesFile) throws IOException, DuplicateException {
        Properties properties = new Properties();
        InputStream inputStream = JettyRunner.class.getResourceAsStream(propertiesFile);
        properties.load(inputStream);
        PropertyUtil.addProperties(properties);
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

        setupJaasInContext(servletContext);

        return servletContext;
    }

    private static void setupJaasInContext(WebAppContext servletContext) {
        Configuration.setConfiguration(new JNDILoginConfiguration());
        servletContext.setSecurityHandler(createJaasSecurityHandler());
        servletContext.addFilter(JaasAuthenticationFilter.class, "/", EnumSet.noneOf(DispatcherType.class));
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
        holder.setInitParameter("version", PropertyUtil.get("oskari.client.version"));
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
        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            HashMap<String, String> loginModuleOptions = new HashMap<String, String>();
            loginModuleOptions.put("debug", "true");
            loginModuleOptions.put("dbJNDIName", "jdbc/OskariPool");
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
