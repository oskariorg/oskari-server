package fi.nls.oskari.service;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Service for proxying a request to a new url based on serviceId.
 * ServiceIds are mapped to a modifier class or urls with given headers.
 * The active serviceIds are mapped in property "oskari.proxyservices".
 * The individual services are then mapped with properties:
 * - oskari.proxy.[serviceId].modifier=[fully qualified classname for a ProxyServiceConfig implementation] (optional if url is defined)
 * - oskari.proxy.[serviceId].url=[url to call for this service] (optional if modifier is defined, modifier can construct url based on request parameters)
 * - oskari.proxy.[serviceId].user=[username for basic auth] (optional)
 * - oskari.proxy.[serviceId].pass=[password for basic auth] (optional)
 * - oskari.proxy.[serviceId].params=[comma separated list of parameter names, named params will be passed to the proxy url] (optional)
 * - oskari.proxy.[serviceId].charset=[charset to use when encoding params and reading response] (optional, defaults to UTF-8)
 * - oskari.proxy.[serviceId].header.[header name]=[header value] (optional)
 * @author SMAKINEN
 */
public class ProxyService {

    private static final Logger log = LogFactory.getLogger(ProxyService.class);
    private static final Map<String, ProxyServiceConfig> availableServices = new TreeMap<String, ProxyServiceConfig>();

    /**
     * Reads properties to initialize services
     */
    public static void init() {
        if(!availableServices.isEmpty()) {
            // already initialized
            return;
        }

        final String[] activeServiceIDs = PropertyUtil.getCommaSeparatedList("oskari.proxyservices");

        if (activeServiceIDs.length == 0) {
            log.warn("No proxy services selected.");
            return;
        } else {
            log.info("Instantiating proxy services:", activeServiceIDs);
        }

        for (String serviceID : activeServiceIDs) {
            final String propertiesPrefix = "oskari.proxy." + serviceID + ".";
            final String handlerClassname = PropertyUtil.getOptional(propertiesPrefix + "handler");
            final ProxyServiceConfig config = getConfig(handlerClassname);
            if(config == null) {
                log.error("Handler misconfiguration for service:", serviceID);
                continue;
            }
            final String url = PropertyUtil.getOptional(propertiesPrefix + "url");
            config.setUrl(url);
            // sanity check
            if(!config.isValid()) {
                log.error("Handler misconfiguration for service:", serviceID);
                continue;
            }

            config.setUsername(PropertyUtil.getOptional(propertiesPrefix + "user"));
            config.setPassword(PropertyUtil.getOptional(propertiesPrefix + "pass"));
            config.setParamNames(PropertyUtil.getCommaSeparatedList(propertiesPrefix + "params"));
            config.setEncoding(PropertyUtil.getOptional(propertiesPrefix + "charset"));

            final String headerPropPrefix = propertiesPrefix + "header.";
            final List<String> headerPropNames = PropertyUtil.getPropertyNamesStartingWith(headerPropPrefix);
            for (String propName : headerPropNames) {
                final String header = propName.substring(headerPropPrefix.length());
                final String value = PropertyUtil.get(propName);
                config.addHeader(header, value);
            }
            availableServices.put(serviceID, config);
        }
    }

    /**
     * Returns config that should be used when proxying.
     * If modifier is not specified, returns a ProxyServiceConfig.
     * If modifier is specified and can be instantiated, returns the custom modifier.
     * If modifier is specified BUT it can't be instantiated, returns null.
     * @param handlerClassName fully qualified class name for a custom modifier or null for defaulting to basic ProxyServiceConfig
     */
    private static ProxyServiceConfig getConfig(final String handlerClassName) {
        if(handlerClassName == null) {
            return new ProxyServiceConfig();
        }
        try {
            final Class clazz = Class.forName(handlerClassName);
            return (ProxyServiceConfig) clazz.newInstance();
        } catch (Exception e) {
            log.error(e, "Error adding proxy service handler for class:", handlerClassName);
        }
        return null;
    }

    /**
     * Proxies request to given service using the given params.
     * @param serviceKey id to map the service
     * @param params params that should be used when proxying
     * @return Response from the service
     * @throws ActionException if something goes wrong when proxying
     */
    public static String proxy(final String serviceKey, final ActionParameters params) throws ActionException {

        if(!availableServices.containsKey(serviceKey)) {
            throw new ActionParamsException("Service not available");
        }
        final byte[] payload = getPayload(params);
        // get base config
        final ProxyServiceConfig baseConfig = availableServices.get(serviceKey);
        // getConfig returns a params based modified config
        final ProxyServiceConfig config = baseConfig.getConfig(params);
        final HttpURLConnection connection = getConnection(config, params);
        try {
            IOHelper.writeToConnection(connection, payload);
            //final String response = IOHelper.readString(connection.getInputStream(), config.getEncoding());
            // giving whole connection detects possible gzip response
            final String response = IOHelper.readString(connection, config.getEncoding());
            return response;
        } catch (Exception e) {
            throw new ActionException("Couldn't proxy request to service:" + serviceKey, e);
        }
    }
    /**
     * Proxies request to given service using the given params.
     * @param serviceKey id to map the service
     * @param params params that should be used when proxying
     * @return Response from the service
     * @throws ActionException if something goes wrong when proxying
     */
    public static byte[] proxyBinary(final String serviceKey, final ActionParameters params) throws ActionException {

        if(!availableServices.containsKey(serviceKey)) {
            throw new ActionParamsException("Service not available");
        }
        final byte[] payload = getPayload(params);
        // get base config
        final ProxyServiceConfig baseConfig = availableServices.get(serviceKey);
        // getConfig returns a params based modified config
        final ProxyServiceConfig config = baseConfig.getConfig(params);
        final HttpURLConnection connection = getConnection(config, params);
        try {
            IOHelper.writeToConnection(connection, payload);
            final byte[] response = IOHelper.readBytes(connection);
            return response;
        } catch (Exception e) {
            throw new ActionException("Couldn't proxy request to service:" + serviceKey, e);
        }
    }


    /**
     * Gets the connection to the proxy service.
     * @param config config providing url, headers and authentication
     * @param params parameters used to create the url
     * @return Connection to the service
     * @throws ActionException if something goes wrong when connecting to the service
     */
    private static HttpURLConnection getConnection(final ProxyServiceConfig config, final ActionParameters params) throws ActionException {
        try {
            final HttpURLConnection connection =
                    IOHelper.getConnection(config.getUrl(params), config.getUsername(), config.getPassword());
            IOHelper.writeHeaders(connection, config.getHeaders());
            return connection;
        } catch (IOException e) {
            throw new ActionException("Couldn't connect to service:" + config.getUrl(params));
        }
    }

    /**
     * Reads the body from the request in params and returns it as a byte array.
     * @param params providing the request body content
     * @return body content as byte array
     * @throws ActionException if something goes wrong when reading the input
     */
    private static byte[] getPayload(final ActionParameters params) throws ActionException {
        try {
            final HttpServletRequest httpRequest = params.getRequest();
            return IOHelper.readBytes(httpRequest.getInputStream());
        } catch (Exception e) {
            throw new ActionException("Failed to read input from request", e);
        }
    }
}
