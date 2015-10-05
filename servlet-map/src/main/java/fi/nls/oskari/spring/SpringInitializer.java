package fi.nls.oskari.spring;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.servlet.WebappHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

/**
 * Programmatic initialization of webapp ("web.xml")
 */
public class SpringInitializer implements WebApplicationInitializer {

    private Logger log = LogFactory.getLogger(SpringInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        // IMPORTANT! read properties at startup - needed for profile selection
        WebappHelper.loadProperties();
        // re-init logger so we get the one configured in properties
        log = LogFactory.getLogger(SpringInitializer.class);
        final WebApplicationContext context = getContext();
        servletContext.addListener(new ContextLoaderListener(context));
        ServletRegistration.Dynamic dispatcher = servletContext.addServlet("DispatcherServlet", new DispatcherServlet(context));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");
    }

    private AnnotationConfigWebApplicationContext getContext() {
        final AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.getEnvironment().setDefaultProfiles(EnvHelper.PROFILE_LOGIN_DB);
        final String[] configuredProfiles = PropertyUtil.getCommaSeparatedList("oskari.profiles");
        if (configuredProfiles.length > 0) {
            log.info("Using profiles:", configuredProfiles);
            context.getEnvironment().setActiveProfiles(configuredProfiles);
        }
        context.setConfigLocation("fi.nls.oskari.spring");
        return context;
    }

}
