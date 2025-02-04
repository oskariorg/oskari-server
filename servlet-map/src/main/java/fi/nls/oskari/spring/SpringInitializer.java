package fi.nls.oskari.spring;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spring.session.RedisSessionConfig;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.init.OskariInitializer;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;

/**
 * Programmatic initialization of webapp ("web.xml")
 */
public class SpringInitializer extends AbstractHttpSessionApplicationInitializer {

    private Logger log = LogFactory.getLogger(SpringInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) {
        // IMPORTANT! read properties at startup - needed for profile selection
        OskariInitializer.loadProperties();
        // re-init logger so we get the one configured in properties
        log = LogFactory.getLogger(SpringInitializer.class);
        final WebApplicationContext context = getContext();
        servletContext.addListener(new ContextLoaderListener(context));
        ServletRegistration.Dynamic dispatcher = servletContext.addServlet("DispatcherServlet", new DispatcherServlet(context));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");
        dispatcher.setAsyncSupported(true);
        dispatcher.setInitParameter("throwExceptionIfNoHandlerFound", "true");
        if (isRedisSessionActived(context)) {
            // only start handling sessions if redis is used to store them.
            // Otherwise just use session tracking provided by the servlet container (Jetty/Tomcat)
            super.onStartup(servletContext);
        }
    }

    /**
     * @see fi.nls.oskari.cache.JedisManager#isClusterEnv()
     * @param context
     * @return
     */
    private boolean isRedisSessionActived(WebApplicationContext context) {
        String[] profiles = context.getEnvironment().getActiveProfiles();
        for (String profile: profiles) {
            if (profile.equalsIgnoreCase(RedisSessionConfig.PROFILE)) {
                return true;
            }
        }
        return false;
    }

    private AnnotationConfigWebApplicationContext getContext() {
        final AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.getEnvironment().setDefaultProfiles(SpringEnvHelper.PROFILE_LOGIN_DB);
        final String[] configuredProfiles = PropertyUtil.getCommaSeparatedList("oskari.profiles");
        if (configuredProfiles.length > 0) {
            log.info("Using profiles:", configuredProfiles);
            context.getEnvironment().setActiveProfiles(configuredProfiles);
        }
        context.setConfigLocations("org.oskari.spring", "fi.nls.oskari.spring");
        return context;
    }

}
