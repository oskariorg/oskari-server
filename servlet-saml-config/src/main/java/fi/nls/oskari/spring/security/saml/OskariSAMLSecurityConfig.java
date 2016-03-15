package fi.nls.oskari.spring.security.saml;

/**
 * Spring security configuration for SAML based login in Oskari.
 */

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spring.EnvHelper;
import fi.nls.oskari.spring.security.OskariLoginFailureHandler;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.saml2.metadata.provider.ResourceBackedMetadataProvider;
import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.saml.*;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.context.SAMLContextProviderLB;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.*;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.*;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.net.URL;
import java.util.*;

/**
 * Configures Spring security SAML extension for Oskari.
 * Properties that can be used to configure this:
 *
 oskari.saml.idp.metadata=/saml/idp-meta-ssocircle.xml
 oskari.saml.sp.entityId=urn:org:oskari:servlet-app

 oskari.keystore.saml=classpath:/saml/oskariSAML.jks
 oskari.keystore.saml.storepass=oskari
 oskari.keystore.saml.defaultKey=oskariKey
 # optional if key password is not the storepass:
 #oskari.keystore.saml.key.oskariKey=oskariPass

 * Further info: http://docs.spring.io/spring-security-saml/docs/current/reference/html/security.html
 * Keytool commands used to create the dummy keystore/key:
 * - Generate new keystore: keytool -genkey -alias oskari -keyalg RSA -keystore oskariSAML.jks -keysize 2048
 * - Add key to keystore: keytool -genkeypair -alias oskariKey -keypass oskariPass -keystore oskariSAML.jks
 */
@Profile(EnvHelper.PROFILE_LOGIN_SAML)
@Configuration
@EnableWebMvcSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@ImportResource("classpath:/saml/samlBindings.xml")
public class OskariSAMLSecurityConfig extends WebSecurityConfigurerAdapter {

    private final static Logger log = LogFactory.getLogger(OskariSAMLSecurityConfig.class);

    private String SP_ENTITY_ID;
    private String AUTH_URL_SUCCESS;
    final private static String AUTH_URL_FAILURE = "/?loginState=failed";
    private String AUTH_URL_LOGOUT;
//    private String URL_IDP_SELECTION;

    private String IDP_META_LOCATION;
    private String SP_METADATA_ENTITY_BASE_URL;
    private String KEYSTORE_LOCATION;
    private String KEYSTORE_PASS;
    private String KEYSTORE_DEFAULT_KEY;
    private Map<String, String> KEYSTORE_KEYS = new HashMap<String, String>();

    public OskariSAMLSecurityConfig() {
        // internal redirects, using properties will result in /spring-map/spring-map/ path
        AUTH_URL_SUCCESS = "/";
        AUTH_URL_LOGOUT = "/";

//        URL_IDP_SELECTION = PropertyUtil.get("oskari.saml.idp.selection", "/saml/idpSelection");
        SP_ENTITY_ID = PropertyUtil.get("oskari.saml.sp.entityId", "urn:org:oskari:servlet-app");

        IDP_META_LOCATION = PropertyUtil.get("oskari.saml.idp.metadata", "/saml/idp-meta-ssocircle.xml");
        SP_METADATA_ENTITY_BASE_URL = PropertyUtil.getOptional("oskari.saml.sp.baseurl");
        if(SP_METADATA_ENTITY_BASE_URL == null) {
            SP_METADATA_ENTITY_BASE_URL = PropertyUtil.get("oskari.domain");
            final String mapUrl = PropertyUtil.get("oskari.map.url");
            if(!"/".equals(mapUrl)) {
                // only add map url if it's not root
                SP_METADATA_ENTITY_BASE_URL = SP_METADATA_ENTITY_BASE_URL + mapUrl;
            }
        }

        // keystore props
        KEYSTORE_LOCATION = PropertyUtil.get("oskari.keystore.saml", "classpath:/saml/oskariSAML.jks");
        KEYSTORE_PASS = PropertyUtil.get("oskari.keystore.saml.storepass", "oskari");
        KEYSTORE_DEFAULT_KEY = PropertyUtil.get("oskari.keystore.saml.defaultKey", "oskariKey");

        final String keyPrefix = "oskari.keystore.saml.key.";
        List<String> keys = PropertyUtil.getPropertyNamesStartingWith(keyPrefix);
        final int keyPrefixLength = keyPrefix.length();
        for(String key : keys) {
            KEYSTORE_KEYS.put(key.substring(keyPrefixLength), PropertyUtil.get(key));
        }
        if(KEYSTORE_KEYS.isEmpty()) {
            KEYSTORE_KEYS.put(KEYSTORE_DEFAULT_KEY, KEYSTORE_PASS);
            log.info("Keystore key specific passwords not defined - assuming storepass as password for default key.");
            log.info("To configure password for key -> define property 'oskari.keystore.saml.key.[name]=[password]'");
        }
        log.info("Using: \n",
                " Keystore:", KEYSTORE_LOCATION, "\n",
                " with key:", KEYSTORE_DEFAULT_KEY, "\n",
                " IDP metadata:", IDP_META_LOCATION, "\n",
                " SP entityId:", SP_ENTITY_ID, "\n",
                " SP baseURL:", SP_METADATA_ENTITY_BASE_URL, "\n");
    }

    @Autowired
    private OskariSAMLUserDetailsService samlUserDetailsServiceImpl;

    @Bean
    public MetadataProvider metadataProvider()
            throws MetadataProviderException, ResourceException {
        final Timer backgroundTaskTimer = new Timer(true);
        if(IDP_META_LOCATION.startsWith("http")) {
            HTTPMetadataProvider provider = new HTTPMetadataProvider(backgroundTaskTimer, httpClient(), IDP_META_LOCATION);
            provider.setParserPool(parserPool());
            return provider;
        }
        else if(!IDP_META_LOCATION.isEmpty()) {
            ClasspathResource res = new ClasspathResource(IDP_META_LOCATION);
            ResourceBackedMetadataProvider provider = new ResourceBackedMetadataProvider(backgroundTaskTimer, res);
            provider.setParserPool(parserPool());
            return new ExtendedMetadataDelegate(provider, new ExtendedMetadata());
        }
        throw new MetadataProviderException("Couldn't determine metadata provider from:" + IDP_META_LOCATION);
    }

    // IDP Metadata configuration - paths to metadata of IDPs in circle of trust
    // is here
    // Do no forget to call iniitalize method on providers
    @Bean
    @Qualifier("metadata")
    public CachingMetadataManager metadata() throws MetadataProviderException, ResourceException {
        List<MetadataProvider> providers = new ArrayList<MetadataProvider>();
        providers.add(metadataProvider());
        return new CachingMetadataManager(providers);
    }

    // Initialization of the velocity engine
    @Bean
    public VelocityEngine velocityEngine() {
        return VelocityFactory.getEngine();
    }

    // XML parser pool needed for OpenSAML parsing
    @Bean(initMethod = "initialize")
    public StaticBasicParserPool parserPool() {
        return new StaticBasicParserPool();
    }

    @Bean(name = "parserPoolHolder")
    public ParserPoolHolder parserPoolHolder() {
        return new ParserPoolHolder();
    }

    // Bindings, encoders and decoders used for creating and parsing messages
    @Bean
    public MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager() {
        return new MultiThreadedHttpConnectionManager();
    }

    @Bean
    public HttpClient httpClient() {
        return new HttpClient(multiThreadedHttpConnectionManager());
    }

    // SAML Authentication Provider responsible for validating of received SAML
    // messages
    @Bean
    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
        samlAuthenticationProvider.setUserDetails(samlUserDetailsServiceImpl);
        return samlAuthenticationProvider;
    }

    // Provider of default SAML Context
    @Bean
    public SAMLContextProviderImpl contextProvider() {
        // SAMLContextProviderLB fixes this error: "SAML message intended destination endpoint did not match recipient endpoint"
        // happens when there's an apache that takes the https-request and proxies it as http to servlet engine
        // http://stackoverflow.com/questions/24805895/recipient-endpoint-doesnt-match-with-saml-response
        SAMLContextProviderLB contextProviderLB = new SAMLContextProviderLB();
        try {
            URL tmp = new URL(SP_METADATA_ENTITY_BASE_URL);
            contextProviderLB.setScheme(tmp.getProtocol());
            contextProviderLB.setContextPath(tmp.getPath());
            contextProviderLB.setServerName(tmp.getHost());
            contextProviderLB.setServerPort(tmp.getPort());
            contextProviderLB.setIncludeServerPortInRequestURL(PropertyUtil.getOptional("oskari.saml.lb.includePort", false));
        } catch (Exception ex) {
            log.error(ex, "Error configuring context provider for SAML");
            return new SAMLContextProviderImpl();
        }
        return contextProviderLB;
    }

    // Initialization of OpenSAML library
    @Bean
    public static SAMLBootstrap sAMLBootstrap() {
        return new SAMLBootstrap();
    }

    // Logger for SAML messages and events
    @Bean
    public SAMLDefaultLogger samlLogger() {
        return new SAMLDefaultLogger();
    }

    // SAML 2.0 WebSSO Assertion Consumer
    @Bean
    public WebSSOProfileConsumer webSSOprofileConsumer() {
        return new WebSSOProfileConsumerImpl();
    }

    // SAML 2.0 Holder-of-Key WebSSO Assertion Consumer
    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
        return new WebSSOProfileConsumerHoKImpl();
    }

    // SAML 2.0 Web SSO profile
    @Bean
    public WebSSOProfile webSSOprofile() {
        return new WebSSOProfileImpl();
    }

    // SAML 2.0 Holder-of-Key Web SSO profile
    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOProfile() {
        return new WebSSOProfileConsumerHoKImpl();
    }

    // SAML 2.0 ECP profile
    @Bean
    public WebSSOProfileECPImpl ecpprofile() {
        return new WebSSOProfileECPImpl();
    }

    @Bean
    public SingleLogoutProfile logoutprofile() {
        return new SingleLogoutProfileImpl();
    }

    // Central storage of cryptographic keys
    @Bean
    public JKSKeyManager keyManager() {
        DefaultResourceLoader loader = new DefaultResourceLoader();
        Resource storeFile = loader.getResource(KEYSTORE_LOCATION);
        return new JKSKeyManager(storeFile, KEYSTORE_PASS, KEYSTORE_KEYS, KEYSTORE_DEFAULT_KEY);
    }

    @Bean
    public WebSSOProfileOptions defaultWebSSOProfileOptions() {
        WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
        webSSOProfileOptions.setIncludeScoping(false);
        return webSSOProfileOptions;
    }

    // Entry point to initialize authentication, default values taken from
    // properties file
    @Bean
    public SAMLEntryPoint samlEntryPoint() {
        SAMLEntryPoint samlEntryPoint = new SAMLEntryPoint();
        samlEntryPoint.setDefaultProfileOptions(defaultWebSSOProfileOptions());
        return samlEntryPoint;
    }

    /*
    // IDP Discovery Service
    @Bean
    public SAMLDiscovery samlIDPDiscovery() {
        SAMLDiscovery idpDiscovery = new SAMLDiscovery();
        idpDiscovery.setIdpSelectionPath(URL_IDP_SELECTION);
        return idpDiscovery;
    }
*/
    // Filter automatically generates default SP metadata
    @Bean
    public MetadataGenerator metadataGenerator() {
        MetadataGenerator metadataGenerator = new MetadataGenerator();
        metadataGenerator.setEntityId(SP_ENTITY_ID);
        metadataGenerator.setEntityBaseURL(SP_METADATA_ENTITY_BASE_URL);
        return metadataGenerator;
    }

    // The filter is waiting for connections on URL suffixed with filterSuffix
    // and presents SP metadata there
    @Bean
    public MetadataDisplayFilter metadataDisplayFilter() {
        return new MetadataDisplayFilter();
    }

    // Handler deciding where to redirect user after successful login
    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler() {
        OskariAuthenticationSuccessHandler successRedirectHandler =
                new OskariAuthenticationSuccessHandler();
        successRedirectHandler.setDefaultTargetUrl(AUTH_URL_SUCCESS);
        return successRedirectHandler;
    }

    // Handler deciding where to redirect user after failed login
    @Bean
    public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
        OskariLoginFailureHandler failureHandler = new OskariLoginFailureHandler();
        failureHandler.setUseForward(true);
        failureHandler.setDefaultFailureUrl(AUTH_URL_FAILURE);
        return failureHandler;
    }

    @Bean
    public SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter() throws Exception {
        SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter = new SAMLWebSSOHoKProcessingFilter();
        samlWebSSOHoKProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
        samlWebSSOHoKProcessingFilter.setAuthenticationManager(authenticationManager());
        samlWebSSOHoKProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return samlWebSSOHoKProcessingFilter;
    }

    // Processing filter for WebSSO profile messages
    @Bean
    public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
        SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
        samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManager());
        samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
        samlWebSSOProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return samlWebSSOProcessingFilter;
    }

    @Bean
    public MetadataGeneratorFilter metadataGeneratorFilter() {
        return new MetadataGeneratorFilter(metadataGenerator());
    }

    // Handler for successful logout
    @Bean
    public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
        SimpleUrlLogoutSuccessHandler successLogoutHandler = new SimpleUrlLogoutSuccessHandler();
        successLogoutHandler.setDefaultTargetUrl(AUTH_URL_LOGOUT);
        return successLogoutHandler;
    }

    // Logout handler terminating local session
    @Bean
    public SecurityContextLogoutHandler logoutHandler() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.setClearAuthentication(true);
        // TODO: delete 'oskaristate' cookie?
        return logoutHandler;
    }

    // Filter processing incoming logout messages
    // First argument determines URL user will be redirected to after successful
    // global logout
    @Bean
    public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
        return new SAMLLogoutProcessingFilter(successLogoutHandler(),
                logoutHandler());
    }

    // Overrides default logout processing filter with the one processing SAML
    // messages
    @Bean
    public SAMLLogoutFilter samlLogoutFilter() {
        return new SAMLLogoutFilter(successLogoutHandler(),
                new LogoutHandler[] { logoutHandler() },
                new LogoutHandler[] { logoutHandler() });
    }

    @Bean
    public FilterChainProxy samlFilter() throws Exception {

        List<SecurityFilterChain> chains = new ArrayList<SecurityFilterChain>();
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/login/**"),
                samlEntryPoint()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/logout/**"),
                samlLogoutFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/metadata/**"),
                metadataDisplayFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSO/**"),
                samlWebSSOProcessingFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSOHoK/**"),
                samlWebSSOHoKProcessingFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SingleLogout/**"),
                samlLogoutProcessingFilter()));
        /*
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/discovery/**"),
                samlIDPDiscovery()));
                */
        return new FilterChainProxy(chains);
    }

    /**
     * Returns the authentication manager currently used by Spring.
     * It represents a bean definition with the aim allow wiring from
     * other classes performing the Inversion of Control (IoC).
     *
     * @throws  Exception
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * Defines the web based security configuration.
     *
     * @param   http It allows configuring web based security for specific http requests.
     * @throws  Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .httpBasic()
                .authenticationEntryPoint(samlEntryPoint());

        http
                .addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class)
                .addFilterAfter(samlFilter(), BasicAuthenticationFilter.class);

        http
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/Oskari/**").permitAll()
                .antMatchers("/action").permitAll()
                .antMatchers("/error").permitAll()
                .antMatchers("/saml/**").permitAll()
                .anyRequest().authenticated();
    }

    /**
     * Sets a custom authentication provider.
     *
     * @param   auth SecurityBuilder used to create an AuthenticationManager.
     * @throws  Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(samlAuthenticationProvider());
    }

}