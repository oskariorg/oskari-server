package fi.nls.oskari.spring;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.servlet.WebappHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Initializes resources needed for Oskari and sorts HandlerMethodArgumentResolvers so the
 * custom extension (OskariParamMethodArgumentResolver) is the first on the list to be processed.
 */
@Configuration
@EnableWebMvc
@ComponentScan(
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {SpringConfig.class}),
        basePackages = "fi.nls.oskari")
public class SpringConfig extends WebMvcConfigurerAdapter {

    private static final Logger LOG = LogFactory.getLogger(SpringConfig.class);

    @PostConstruct
    public void oskariInit()
            throws Exception {
        // check DB connections/content
        WebappHelper.loadProperties();
        WebappHelper.initializeOskariContext();
    }

    @Bean
    public ViewResolver getDefaultsViewResolver() {
        final InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(JstlView.class);
        // fallback to default JSP in spring-map
        resolver.setPrefix("/WEB-INF/jsp/");
        resolver.setSuffix(".jsp");
        resolver.setOrder(999999);
        return resolver;
    }

    @PreDestroy
    public void tearDown() {
        LOG.info("Teardown");
        WebappHelper.teardown();
    }
}
