package fi.nls.oskari.work.fe;

import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.domain.map.wfs.WFSSLDStyle;
import fi.nls.oskari.eu.elf.recipe.universal.ELF_path_parse_worker;
import fi.nls.oskari.fe.engine.FEEngineManager;
import fi.nls.oskari.fe.engine.FeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fi.rysp.generic.WFS11_path_parse_worker;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.pojo.GeoJSONFilter;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.transport.TransportJobException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.wfs.WFSExceptionHelper;
import fi.nls.oskari.wfs.WFSFilter;
import fi.nls.oskari.wfs.WFSImage;
import fi.nls.oskari.wfs.WFSParser;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.work.JobType;
import fi.nls.oskari.work.OWSMapLayerJob;
import fi.nls.oskari.work.RequestResponse;
import fi.nls.oskari.work.ResultProcessor;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.referencing.CRS;
import org.geotools.styling.Style;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FEMapLayerJob extends OWSMapLayerJob {

    final ArrayList<String> selectedProperties = new ArrayList<String>();

    final Map<Resource, Integer> selectedPropertiesIndex = new HashMap<Resource, Integer>();

    public FEMapLayerJob(ResultProcessor service, JobType type,
            SessionStore store, WFSLayerStore layer) {
        super(service, type, store, layer);
    }

    public FEMapLayerJob(ResultProcessor service, JobType type,
            SessionStore store, WFSLayerStore layer, boolean reqSendFeatures,
            boolean reqSendImage, boolean reqSendHighlight) {
        super(service, type, store, layer, reqSendFeatures, reqSendImage,
                reqSendHighlight);

    }

    protected WFSImage createHighlightImage() {

        final Style style = getSLD(this.layer.getGMLGeometryPropertyNoNamespace(),"highlight");

        return new WFSImage(this.layer, this.session.getClient(),
                WFSImage.STYLE_DEFAULT, JobType.HIGHLIGHT.toString()) {
            protected Style getSLDStyle(WFSLayerStore layer, String styleName) {
                return style;
            }
        };
    }

    @Override
    protected WFSImage createResponseImage() {

        final Style style = getSLD(this.layer.getGMLGeometryPropertyNoNamespace(),
                this.sessionLayer != null ? this.sessionLayer.getStyleName() : "default");

        return new WFSImage(this.layer, this.session.getClient(),
                WFSImage.STYLE_DEFAULT, null) {

            protected Style getSLDStyle(WFSLayerStore layer, String styleName) {
                return style;
            }

        };
    }

    /**
     * Parses features values
     */
    protected void featuresHandler() {
        log.debug("features handler");

        this.geomValuesList = new ArrayList<List<Object>>();

        // WFSMaplayerJob only sends if type is normal, but there is additional processing for the feature anyways
        // For now we just want highlight to NOT send a feature.
        if(this.type != JobType.HIGHLIGHT) {
            for (List<Object> feature : featureValuesList) {
                    this.sendWFSFeature(feature);
            }
        }

        boolean geometryParingFailures = false;

        //send geometries as well, if requested
        if (this.session.isGeomRequest() && this.features != null) {
            // send feature geometry
            FeatureIterator<SimpleFeature> featuresIter = this.features.features();
            while (goNext(featuresIter.hasNext())) {
                SimpleFeature feature = featuresIter.next();
                String fid = feature.getIdentifier().getID();
                log.debug("Processing geom property of feature:", fid);

                // get feature geometry (transform if needed) and get geometry center
                Geometry geometry = WFSParser.getFeatureGeometry(feature, this.layer.getGMLGeometryProperty(), this.transformClient);
                log.debug("Requested geometry", fid);
                List<Object> gvalues = new ArrayList<Object>();
                gvalues.add(fid);
                if (geometry != null) {
                    gvalues.add(geometry.toText());
                } else {
                    log.debug("Feature geometry parsing failed", fid);
                    gvalues.add(null);
                    geometryParingFailures = true;
                }
                this.geomValuesList.add(gvalues);
            }
            if(geometryParingFailures){
                Map<String, Object> output = this.createCommonWarningResponse(
                        "Geometry parsing of some features failed (unknown geometry property or transformation error",
                        WFSExceptionHelper.WARNING_GEOMETRY_PARSING_FAILED);
                this.sendCommonErrorResponse(output, true);
            }
        }
    }

    protected void propertiesHandler() {
        this.sendWFSProperties(selectedProperties,
                this.layer.getFeatureParamsLocales(this.session.getLanguage()));
    }

    /**
     * Builds the WFS request from template. Issues HTTP request with WFS
     * request Processes WFS response with 'feature-engine' i.e Groovy scripts.
     */
    public RequestResponse request(final JobType type, final WFSLayerStore layer,
            final SessionStore session, final List<Double> bounds,
            final MathTransform transformService) {

        final ArrayList<List<Object>> resultsList = new ArrayList<List<Object>>();
        final Map<Resource, SimpleFeatureCollection> responseCollections = new HashMap<Resource, SimpleFeatureCollection>();

        final FERequestResponse requestResponse = new FERequestResponse();

        Filter filter = WFSFilter.initBBOXFilter(session.getLocation(), layer, false);
        requestResponse.setFilter(filter);
        requestResponse.setResponse(responseCollections);
        requestResponse.setLocation(session.getLocation());

        final String urlTemplate = layer.getURL();
        final String requestTemplatePath = layer.getRequestTemplate();
        final String recipePath = layer.getResponseTemplate();
        final String username = layer.getUsername();
        final String password = layer.getPassword();

        final String srsName = session.getLocation().getSrs();
        final String featureNs = layer.getFeatureNamespaceURI();
        final String featurePrefix = layer.getFeatureNamespace();
        final String featureName = layer.getFeatureElement();
        final String WFSver = layer.getWFSVersion();
        final String geomProp = layer.getGMLGeometryProperty();
        final String geomNs = layer.getGeometryNamespaceURI();
        final String maxCount = Integer.toString(layer.getMaxFeatures());
        final Boolean resolveDepth = JSONHelper.getBooleanFromJSON(layer.getAttributes(), "resolveDepth", false);

        JSONObject parseConfig = layer.getParseConfig();

        final FERequestTemplate backendRequestTemplate = getRequestTemplate(requestTemplatePath);
        if (backendRequestTemplate == null) {
            log.error("NO Request Template available");
            throw new TransportJobException("NO Request Template available [fe]",
                    WFSExceptionHelper.ERROR_GETFEATURE_PAYLOAD_FAILED);
        }

        backendRequestTemplate.setRequestFeatures(srsName, featureNs, featurePrefix,
                featureName, WFSver, geomProp, geomNs, maxCount, resolveDepth);

        FeatureEngine featureEngine = null;
        try {
            featureEngine = getFeatureEngine(recipePath);
        } catch (Exception e) {
            throw new TransportJobException(e.getMessage(),
                    e.getCause(),
                    WFSExceptionHelper.ERROR_GETFEATURE_ENGINE_FAILED);
        }

        if (featureEngine == null) {
            log.error("NO FeatureEngine available - maybe invalid wfs layer configuration");
            throw new TransportJobException("NO FeatureEngine available - maybe invalid wfs layer configuration",
                    WFSExceptionHelper.ERROR_GETFEATURE_ENGINE_FAILED);
        }

        // Is parsing based on parse config
        if(parseConfig != null){
            ELF_path_parse_worker worker = new ELF_path_parse_worker(parseConfig);
            featureEngine.getRecipe().setParseWorker(worker);
            WFS11_path_parse_worker wfs11worker = new WFS11_path_parse_worker(parseConfig);
            featureEngine.getRecipe().setWFS11ParseWorker(wfs11worker);
        }

        final FeatureEngine engine = featureEngine;

        log.debug("[fe] request template " + requestTemplatePath
                + " instantiated as " + backendRequestTemplate);
        log.debug("[fe] featureEngine " + recipePath + " instantiated as "
                + featureEngine);

        this.featureValuesList = resultsList;

        try {
            /* CRS */
            //Axis order is x=lon y=lat for each projection in a OL Map
            final CoordinateReferenceSystem crs = CRS.decode(session
                    .getLocation().getSrs(), true);

            final MathTransform transform = this.session.getLocation()
                    .getTransformForClient(crs, true);

            /* FeatureEngine InputProcessor */
            final XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

            final OutputProcessor outputProcessor = new FEOutputProcessor(
                    resultsList, responseCollections, crs, requestResponse,
                    selectedProperties, selectedPropertiesIndex, transform, geomProp);

            /* Backend HTTP URI info */
            FEUrl backendUrlInfo = getBackendURL(urlTemplate);

            URL url = new URL(backendUrlInfo.url);

            log.debug("[fe] using URL " + url);

            /* Backend Proxy */
            HttpHost backendProxy = null;

            if (System.getProperty("http.proxyHost") != null
                    && System.getProperty("http.proxyPort") != null) {

                if (backendUrlInfo.proxy) {
                    backendProxy = new HttpHost(
                            System.getProperty("http.proxyHost"),
                            Integer.valueOf(
                                    System.getProperty("http.proxyPort"), 10),
                            "http");
                }
            }
            /* backendResponseHandler processes HTTP response */

            FEResponseHandler backendResponseHandler = new FEResponseHandler(
                    engine, inputProcessor, outputProcessor);

            /* Backend HTTP Request */
            HttpUriRequest backendUriRequest = null;
            if (backendRequestTemplate.isPost) {
                HttpPost httppost = new HttpPost(new URI(url.toExternalForm()));

                StringBuffer params = new StringBuffer();

                backendRequestTemplate.buildParams(params, type, layer,
                        session, bounds, transform, crs);

                log.debug("WFS POST Body " + params);

                StringEntity entity = new StringEntity(params.toString());
                httppost.setHeader(new BasicHeader("Content-Type",
                        "text/xml; charset=UTF-8"));
                httppost.setEntity(entity);
                log.debug("[fe] HTTP POST " + httppost.getRequestLine());

                backendUriRequest = httppost;

            } else {

                URIBuilder builder = new URIBuilder();
                builder.setScheme(url.getProtocol());
                builder.setHost(url.getHost());
                builder.setPort(url.getPort());
                builder.setPath(url.getPath());
                backendRequestTemplate.buildParams(builder, type, layer,
                        session, bounds, transform, crs);

                HttpGet httpget = new HttpGet(builder.build());
                log.debug("[fe] HTTP GET " + httpget.getRequestLine());

                backendUriRequest = httpget;

            }

            /* Backend HTTP Executor */
            final HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, this.FE_READ_TIMEOUT_MS); //IOHelper.getConnectionTimeoutMs());
            HttpConnectionParams.setSoTimeout(httpParams, this.FE_READ_TIMEOUT_MS);
            DefaultHttpClient backendHttpClient = new DefaultHttpClient(httpParams);
            try {
                HttpHost backendHttpHost = new HttpHost(url.getHost(),
                        url.getPort(), url.getProtocol());

                UsernamePasswordCredentials backendCredentials = getCredentials(
                        username, password);

                BasicHttpContext backendLocalContext = null;
                if (backendCredentials != null) {

                    log.debug("[fe] using Credentials "
                            + backendCredentials.getUserName() + " for " + url);

                    backendHttpClient.getCredentialsProvider().setCredentials(
                            new AuthScope(backendHttpHost.getHostName(),
                                    backendHttpHost.getPort()),
                            backendCredentials);

                    // Create AuthCache instance
                    AuthCache authCache = new BasicAuthCache();
                    // Generate BASIC scheme object and add it to the local
                    // auth cache
                    BasicScheme basicAuth = new BasicScheme();
                    authCache.put(backendHttpHost, basicAuth);

                    backendLocalContext = new BasicHttpContext();
                    backendLocalContext.setAttribute(ClientContext.AUTH_CACHE,
                            authCache);
                }

                if (backendProxy != null) {
                    log.debug("[fe] setting proxy for " + url);
                    backendHttpClient.getParams().setParameter(
                            ConnRoutePNames.DEFAULT_PROXY,

                            backendProxy);
                }

                Boolean succee = backendLocalContext != null ? backendHttpClient
                        .execute(backendUriRequest, backendResponseHandler,
                                backendLocalContext) : backendHttpClient
                        .execute(backendUriRequest, backendResponseHandler);

                log.debug("[fe] execute response " + succee + " for " + url);

            } catch (HttpResponseException e) {
                log.error("Error parsing response:", log.getCauseMessages(e));
                log.debug(e);
                throw new ServiceRuntimeException("Status code: " + Integer.toString(e.getStatusCode()) + " " + e.getMessage(),
                        e.getCause(),
                        WFSExceptionHelper.ERROR_GETFEATURE_POSTREQUEST_FAILED);
            } catch (IOException e) {
                log.error("Error fetching response:", log.getCauseMessages(e));
                log.debug(e);
                throw new ServiceRuntimeException(e.getMessage(),
                        e.getCause(),
                        WFSExceptionHelper.ERROR_GETFEATURE_POSTREQUEST_FAILED);
            } catch (Exception e) {
                log.error("Error fetching response:", log.getCauseMessages(e));
                log.debug(e);
                throw new ServiceRuntimeException(e.getMessage(),
                        e.getCause(),
                        WFSExceptionHelper.ERROR_GETFEATURE_POSTREQUEST_FAILED);
            } finally {
                // When HttpClient instance is no longer needed,
                // shut down the connection manager to ensure
                // immediate deallocation of all system resources
                backendHttpClient.getConnectionManager().shutdown();
                log.debug("[fe] http shutdown for " + url);
            }

        } catch (ServiceRuntimeException e) {
            log.error(e);
            throw new TransportJobException(e.getMessage(),
                    e.getCause(),
                    e.getMessageKey());
        } catch (Exception e) {
            log.error(e);
            throw new TransportJobException(e.getMessage(),
                    e.getCause(),
                    WFSExceptionHelper.ERROR_FEATURE_PARSING);
        } finally {
            log.debug("[fe] end of process");
        }

        return requestResponse;
    }

    private FeatureEngine getFeatureEngine(String recipePath)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        return FEEngineManager.getEngineForRecipe(recipePath);
    }

    /**
     * builds the Backend URL and adds proxy from System properties
     * 
     * @param urlTemplate
     * @return
     */
    protected FEUrl getBackendURL(String urlTemplate) {

        String url = null;
        boolean isProxy = false;

        if (System.getProperty("http.proxyHost") != null
                && System.getProperty("http.proxyPort") != null) {

            if (urlTemplate.indexOf('|') != -1) {
                url = urlTemplate.substring(0, urlTemplate.indexOf('|'));
                isProxy = true;

            } else {
                url = urlTemplate;
                isProxy = true;
            }

            if (url.charAt(0) == '!') {
                url = url.substring(1);
                isProxy = false;
            }

        } else {
            if (urlTemplate.indexOf('|') != -1) {
                url = urlTemplate.substring(urlTemplate.indexOf('|') + 1);
                isProxy = false;
            } else {
                url = urlTemplate;
                isProxy = false;
            }
        }

        return new FEUrl(url, isProxy);

    }

    /**
     * builds credentials for HTTP request
     * 
     * @param username
     * @param password
     * @return
     */
    protected UsernamePasswordCredentials getCredentials(String username,
            String password) {
        if (username == null || username.isEmpty()) {
            return null;
        }
        if (password == null || password.isEmpty()) {
            return null;
        }
        log.debug("[fe] building credentials for " + this.layerId + " as "
                + username);
        return new UsernamePasswordCredentials(username, password);
    }

    /**
     * Builds Request Template
     * 
     * @param requestTemplatePath
     * @return
     */
    protected FERequestTemplate getRequestTemplate(String requestTemplatePath) {

        if (requestTemplatePath
                .equals("oskari-feature-engine:QueryArgsBuilder_KTJkii_LEGACY")) {
            log.debug("[fe] using specific GET request template for "
                    + this.layerId + " is " + requestTemplatePath);
            return new FERequestTemplate(new KTJRestQueryArgsBuilder());
        } else if (requestTemplatePath
                .equals("oskari-feature-engine:QueryArgsBuilder_WFS_GET")) {
            log.debug("[fe] using GET WFS request template for " + this.layerId
                    + " is " + requestTemplatePath);
            return new FERequestTemplate(new FEWFSGetQueryArgsBuilder());
        } else {
            log.debug("[fe] using POST WFS request template for "
                    + this.layerId + " is " + requestTemplatePath);
            return new FERequestTemplate(requestTemplatePath);
        }

    }

    /**
     * Looks up (cached) SLD styling for WFS
     * 
     * @return
     */
    protected Style getSLD(String geomPropertyname, String styleName) {

        List<WFSSLDStyle> sldStyles = this.layer.getSLDStyles();

        WFSSLDStyle sldStyle = null;
        for (WFSSLDStyle s : sldStyles) {
            if (styleName.equals(s.getName())) {
                log.debug("[fe] SLD for  " + this.layerId + " FE style found");
                sldStyle = s;
                break;
            }
        }
        String sldPath = null;
        String sldName = null;
        if (sldStyle == null) {
            log.debug("[fe] SLD for  " + this.layerId + " not found - use default");
            sldPath = this.DEFAULT_FE_SLD_STYLE_PATH+geomPropertyname.toLowerCase()+".xml";
        }
        else {
            sldPath = sldStyle.getSLDStyle();
            sldName = sldStyle.getName();

        }



        Style sld = FEStyledLayerDescriptorManager.getSLD(sldName, sldPath);

        return sld;

    }

    @Override
    public FeatureCollection<SimpleFeatureType, SimpleFeature> response(
            WFSLayerStore layer, RequestResponse requestResponse) {
        FeatureCollection<SimpleFeatureType, SimpleFeature> responseFeatures = ((FERequestResponse) requestResponse)
                .getResponse().get(
                        ((FERequestResponse) requestResponse).getFeatureIri());
        return responseFeatures;
    }

    @Override
    public boolean runPropertyFilterJob() {
        return runUnknownJob();
    }

    /**
     * Checks if enough information for running the task type
     *
     * @return <code>true</code> if enough information for type;
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean hasValidParams() {
        // FE doesn't handle PROPERTY_FILTER
        if(this.type == JobType.PROPERTY_FILTER) {
            return false;
        }
        return super.hasValidParams();
    }

    /**
     * Makes request and parses response to features
     *
     * @param bounds
     * @return <code>true</code> if thread should continue; <code>false</code>
     *         otherwise.
     */
    /**
     * Makes request and parses response to features
     *
     * @param bounds
     * @return <code>true</code> if thread should continue; <code>false</code>
     *         otherwise.
     */
    protected boolean requestHandler(List<Double> bounds) {

        // make a request
        RequestResponse response = request(type, layer, session, bounds,
                transformService);

        Map<String, Object> output = createCommonResponse();
        try {

            // request failed
            if (response == null) {
                log.debug("Request failed for layer" + layer.getLayerId());
                log.debug(PROCESS_ENDED + getKey());
                throw new ServiceRuntimeException("Request failed for layer: " + layer.getLayerName() + "id: " + layer.getLayerId(),
                        WFSExceptionHelper.ERROR_GETFEATURE_POSTREQUEST_FAILED);
            }

            // parse response
            this.features = response(layer, response);

            // parsing failed
            if (this.features == null) {
                log.debug("Parsing failed for layer " + this.layerId);
                log.debug(PROCESS_ENDED + getKey());
                throw new ServiceRuntimeException("Request failed for layer: " + layer.getLayerName(),
                        WFSExceptionHelper.ERROR_FEATURE_PARSING);
            }

            // Swap XY in feature geometry, if reverseXY setup in layer attributes
            if(layer.isReverseXY(session.getLocation().getSrs())){
                ProjectionHelper.swapGeometryXY(this.features);
            }

            // 0 features found - send size
            if (this.type == JobType.MAP_CLICK && this.features.size() == 0) {
                log.debug("Empty result for map click" + this.layerId);
                output.put(OUTPUT_FEATURES, "empty");
                output.put(OUTPUT_KEEP_PREVIOUS, this.session.isKeepPrevious());
                this.service.addResults(session.getClient(),
                        ResultProcessor.CHANNEL_MAP_CLICK, output);
                log.debug(PROCESS_ENDED + getKey());
                return false;
            } else if (this.type == JobType.GEOJSON && this.features.size() == 0) {
                log.debug("Empty result for filter" + this.layerId);
                output.put(OUTPUT_FEATURES, "empty");
                this.service.addResults(session.getClient(),
                        ResultProcessor.CHANNEL_FILTER, output);
                log.debug(PROCESS_ENDED + getKey());
                return false;
            } else {
                if (this.features.size() == 0) {
                    log.debug("Empty result" + this.layerId);
                    output.put(OUTPUT_FEATURE, "empty");
                    this.service.addResults(session.getClient(),
                            ResultProcessor.CHANNEL_FEATURE, output);
                    log.debug(PROCESS_ENDED + getKey());
                    return false;
                } else if (this.features.size() == layer.getMaxFeatures()) {
                    log.debug("Max feature result" + this.layerId);
                    output.put(OUTPUT_FEATURE, "max");
                    this.service.addResults(session.getClient(),
                            ResultProcessor.CHANNEL_FEATURE, output);
                }
            }

            log.debug("Features count" + this.features.size());
        } catch (ServiceRuntimeException e) {
            log.error(e);
            throw new TransportJobException(e.getMessage(),
                    e.getCause(),
                    e.getMessageKey());
        } catch (Exception ee) {
            log.debug("exception: " + ee);
            throw new TransportJobException(ee.getMessage(),
                    ee.getCause(),
                    WFSExceptionHelper.ERROR_FEATURE_PARSING);
        }

        return true;
    }

}
