package org.oskari.spring;

import fi.nls.oskari.control.ActionControl;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.oskari.spring.extension.OskariParamMethodArgumentResolver;
import org.oskari.spring.extension.OskariViewResolver;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.init.OskariInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Initializes resources needed for Oskari and sorts HandlerMethodArgumentResolvers so the
 * custom extension (OskariParamMethodArgumentResolver) is the first on the list to be processed.
 */
@Configuration
@EnableWebMvc
@ComponentScan(
        excludeFilters = @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value={SpringConfig.class}),
        basePackages="fi.nls.oskari, org.oskari")
public class SpringConfig implements WebMvcConfigurer, ServletContextAware, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LogFactory.getLogger(SpringConfig.class);

    private ServletContext servletContext;

    public void setServletContext(ServletContext context) {
        this.servletContext = context;
    }

    @PostConstruct
    public void oskariInit() {
        // check DB connections/content
        OskariInitializer.init();
    }

    //  --------- locale handling -------------
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
        registry.addInterceptor(new OskariRequestInterceptor());
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor(){
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        // this ignores calls like lang=$temp <- erronous values
        localeChangeInterceptor.setIgnoreInvalidLocale(true);
        return localeChangeInterceptor;
    }

    @Bean(name = "localeResolver")
    public LocaleResolver localeResolver() {
        ValidatingCookieLocaleResolver resolver = new ValidatingCookieLocaleResolver();
        resolver.setDefaultLocale(new Locale(PropertyUtil.getDefaultLanguage()));
        resolver.setSupportedLocales(PropertyUtil.getSupportedLocales());
        resolver.setSupportedLanguages(PropertyUtil.getSupportedLanguages());
        resolver.setCookieName("oskari.language");
        resolver.setCookieMaxAge(-1);
        resolver.setCookiePath(servletContext.getContextPath());

        return resolver;
    }
    //  --------- /locale handling -------------

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        /*
         * Moves the custom argument resolver to first resolver so it
         * gets called even if built-in resolvers are used.
         */
        final RequestMappingHandlerAdapter adapter =
                event.getApplicationContext().getBean(RequestMappingHandlerAdapter.class);
        final OskariParamMethodArgumentResolver resolver =
                event.getApplicationContext().getBean(OskariParamMethodArgumentResolver.class);

        final List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>(adapter.getArgumentResolvers());
        argumentResolvers.remove(resolver);
        argumentResolvers.add(0, resolver);
        adapter.setArgumentResolvers(argumentResolvers);

    }

    @Bean
    public ViewResolver getExtensionHookViewResolver() {
        // OskariViewResolver extends InternalResourceViewResolver but lets missing views
        // pass to next viewResolver so you don't need to copy/paste defaults when adding new JSPs
        final OskariViewResolver resolver = new OskariViewResolver();
        resolver.setViewClass(JstlView.class);
        // if 'oskari.server.jsp.location' is defined -> assume runnable jar and use JSPs defined place
        // else -> assume WEB-INF/jsp from webapp-spring
        resolver.setPrefix(PropertyUtil.get("oskari.server.jsp.location", "/WEB-INF/jsp/"));
        resolver.setSuffix(".jsp");
        resolver.setOrder(999998);
        return resolver;
    }

    @Bean
    public ViewResolver getDefaultsViewResolver() {
        final InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(JstlView.class);
        // fallback to default JSP in spring-map
        resolver.setPrefix("/spring-map-jsp/");
        resolver.setSuffix(".jsp");
        resolver.setOrder(999999);
        return resolver;
    }

    @PreDestroy
    public void tearDown() {
        LOG.info("Teardown");
        ActionControl.teardown();
        OskariInitializer.teardown();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/xhr-prioritizer.js")
                .addResourceLocations("classpath:service-workers/xhr-prioritizer.js")
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
        // search from configured path or from classpath root
        String faviconPath = PropertyUtil.get("favicon.path", "classpath:/");
        registry
                .addResourceHandler("/favicon.ico")
                .addResourceLocations(faviconPath)
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // check for extension first, then the defaults
        messageSource.setBasenames("classpath:locale/messages-ext", "classpath:locale/messages");
        // if true, the key of the message will be displayed if the key is not
        // found, instead of throwing a NoSuchMessageException
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultEncoding("UTF-8");
        // # -1 : never reload, 0 always reload
        boolean isDevMode = PropertyUtil.getOptional("development", true);
        if(isDevMode) {
            messageSource.setCacheSeconds(0);
        } else {
            messageSource.setCacheSeconds(-1);
        }

        return messageSource;
    }

}
