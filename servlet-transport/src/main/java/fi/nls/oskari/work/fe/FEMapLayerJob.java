package fi.nls.oskari.work.fe;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;


import fi.nls.oskari.eu.elf.recipe.universal.ELF_path_parse_worker;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import fi.nls.oskari.work.JobType;
import fi.nls.oskari.work.hystrix.HystrixMapLayerJob;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
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

import fi.nls.oskari.domain.map.wfs.WFSSLDStyle;
import fi.nls.oskari.fe.engine.FEEngineManager;
import fi.nls.oskari.fe.engine.FeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.transport.TransportService;
import fi.nls.oskari.wfs.WFSFilter;
import fi.nls.oskari.wfs.WFSImage;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.work.RequestResponse;
import fi.nls.oskari.work.ResultProcessor;

public class FEMapLayerJob extends HystrixMapLayerJob {

    final ArrayList<String> selectedProperties = new ArrayList<String>();

    final Map<Resource, Integer> selectedPropertiesIndex = new HashMap<Resource, Integer>();

    public FEMapLayerJob(ResultProcessor service, JobType type,
            SessionStore store, String layerId) {
        super(service, type, store, layerId);

    }

    public FEMapLayerJob(ResultProcessor service, JobType type,
            SessionStore store, String layerId, boolean reqSendFeatures,
            boolean reqSendImage, boolean reqSendHighlight) {
        super(service, type, store, layerId, reqSendFeatures, reqSendImage,
                reqSendHighlight);

    }

    protected WFSImage createHighlightImage() {

        final Style style = getSLD();

        return new WFSImage(this.layer, this.session.getClient(),
                WFSImage.STYLE_DEFAULT, JobType.HIGHLIGHT.toString()) {
            protected Style getSLDStyle(WFSLayerStore layer, String styleName) {
                return style;
            }
        };
    }

    protected WFSImage createResponseImage() {

        final Style style = getSLD();

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

        for (List<Object> feature : featureValuesList) {
            this.sendWFSFeature(feature);
        }

    }

    protected void propertiesHandler() {
        this.sendWFSProperties(selectedProperties,
                this.layer.getFeatureParamsLocales(this.session.getLanguage()));
    }

    /**
     * Builds the WFS request from template. Issues HTTP request with WFS
     * request Processes WFS response with 'feature-engine' i.e Groovy scripts.
     * 
     */
    public RequestResponse request(final JobType type, final WFSLayerStore layer,
            final SessionStore session, final List<Double> bounds,
            final MathTransform transformService) {

        final ArrayList<List<Object>> resultsList = new ArrayList<List<Object>>();
        final Map<Resource, SimpleFeatureCollection> responseCollections = new HashMap<Resource, SimpleFeatureCollection>();

        final FERequestResponse requestResponse = new FERequestResponse();

        if (!validateMapScales()) {
            log.debug("[fe] Map scale was not valid for layer " + this.layerId);

            return requestResponse;
        }

        Filter filter = WFSFilter.initBBOXFilter(session.getLocation(), layer);
        requestResponse.setFilter(filter);
        requestResponse.setResponse(responseCollections);
        requestResponse.setLocation(session.getLocation());

        final String urlTemplate = layer.getURL();
        final String requestTemplatePath = layer.getRequestTemplate();
        final String recipePath = layer.getResponseTemplate();
        final String username = layer.getUsername();
        final String password = layer.getPassword();

        final String srsName = layer.getSRSName();
        final String featureNs = layer.getFeatureNamespaceURI();
        final String featureName = layer.getFeatureElement();
        final String WFSver = layer.getWFSVersion();
        final String geomProp = layer.getGMLGeometryProperty();
        final String geomNs = layer.getGeometryNamespaceURI();

        JSONObject selectedFeatureParams = layer.getSelectedFeatureParams();
        JSONObject parseConfig = JSONHelper.createJSONObject(layer.getParseConfig());

        final FERequestTemplate backendRequestTemplate = getRequestTemplate(requestTemplatePath);
        if (backendRequestTemplate == null) {
            log.error("NO Request Template available");
            return requestResponse;
        }

        backendRequestTemplate.setRequestFeatures(srsName, featureNs,
                featureName, WFSver, geomProp, geomNs);

        FeatureEngine featureEngine = null;
        try {
            featureEngine = getFeatureEngine(recipePath);
        } catch (InstantiationException e3) {
            log.error(e3);
        } catch (IllegalAccessException e3) {
            log.error(e3);
        } catch (ClassNotFoundException e) {
            log.error(e);
        }

        if (featureEngine == null) {
            log.error("NO FeatureEngine available");
            return requestResponse;
        }

        // Is parsing based on parse config
        if(parseConfig != null){
            ELF_path_parse_worker worker = new ELF_path_parse_worker(parseConfig);
            featureEngine.getRecipe().setParseWorker(worker);
        }

        final FeatureEngine engine = featureEngine;

        log.debug("[fe] request template " + requestTemplatePath
                + " instantiated as " + backendRequestTemplate);
        log.debug("[fe] featureEngine " + recipePath + " instantiated as "
                + featureEngine);

        this.featureValuesList = resultsList;

        try {
            /* CRS */
            final CoordinateReferenceSystem crs = CRS.decode(session
                    .getLocation().getSrs());

            AxisDirection dir0 = crs.getCoordinateSystem().getAxis(0)
                    .getDirection();
            log.debug("[fe] SESSION CRS AXIS 0 " + dir0);

            final MathTransform transform = this.session.getLocation()
                    .getTransformForClient(this.layer.getCrs(), true);

            /* FeatureEngine InputProcessor */
            final XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

            final OutputProcessor outputProcessor = new FEOutputProcessor(
                    resultsList, responseCollections, crs, requestResponse,
                    selectedProperties, selectedPropertiesIndex, transform);

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
            HttpConnectionParams.setConnectionTimeout(httpParams, IOHelper.getConnectionTimeoutMs());
            HttpConnectionParams.setSoTimeout(httpParams, IOHelper.getReadTimeoutMs());
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

            } catch (ClientProtocolException e) {
                log.error("Error parsing response:", log.getCauseMessages(e));
                log.debug(e);
            } catch (IOException e) {
                log.error("Error fetching response:", log.getCauseMessages(e));
                log.debug(e);
            } finally {
                // When HttpClient instance is no longer needed,
                // shut down the connection manager to ensure
                // immediate deallocation of all system resources
                backendHttpClient.getConnectionManager().shutdown();
                log.debug("[fe] http shutdown for " + url);
            }

        } catch (NoSuchAuthorityCodeException e) {
            log.error(e);
        } catch (FactoryException e) {
            log.error(e);
        } catch (XPathExpressionException e) {
            log.error(e);
        } catch (TransformException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        } catch (ParserConfigurationException e) {
            log.error(e);
        } catch (SAXException e) {
            log.error(e);
        } catch (TransformerException e) {
            log.error(e);
        } catch (URISyntaxException e) {
            log.error(e);
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
        if (username == null || password == null) {
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
    protected Style getSLD() {

        List<WFSSLDStyle> sldStyles = this.layer.getSLDStyles();

        WFSSLDStyle sldStyle = null;
        for (WFSSLDStyle s : sldStyles) {
            if ("oskari-feature-engine".equals(s.getName())) {
                log.debug("[fe] SLD for  " + this.layerId + " FE style found");
                sldStyle = s;
                break;
            }
        }

        if (sldStyle == null) {
            log.debug("[fe] SLD for  " + this.layerId + " not found");
            return null;
        }

        String sldPath = sldStyle.getSLDStyle();

        Style sld = FEStyledLayerDescriptorManager.getSLD(sldPath);

        return sld;

    }

    @Override
    public FeatureCollection<SimpleFeatureType, SimpleFeature> response(
            WFSLayerStore layer, RequestResponse requestResponse) {
        FeatureCollection<SimpleFeatureType, SimpleFeature> responseFeatures = ((FERequestResponse) requestResponse)
                .getResponse().get(
                        ((FERequestResponse) requestResponse).getFeatureIri());

        Filter filter = ((FERequestResponse) requestResponse).getFilter();
        /*
         * if (responseFeatures != null && filter != null) {
         * 
         * log.debug("APPLYING FILTER " + filter + " to " + responseFeatures);
         * log.debug("- GT ThreadSafety ..?"); responseFeatures =
         * responseFeatures.subCollection(filter);
         * 
         * }
         */

        return responseFeatures;
    }

    /**
     * hook to simplify testing
     */
    protected boolean hasPermissionsForJob() {
        return getPermissions(layerId, this.session.getSession(),
                this.session.getRoute());
    }

    /**
     * hook to simplify testing
     */
    protected WFSLayerStore getLayerForJob() {
        return getLayerConfiguration(this.layerId, this.session.getSession(),
                this.session.getRoute());
    }

    /**
     * Process of the job
     * 
     * Worker calls this when starts the job.
     * 
     * Duplicated to enable refactoring in the near future.
     * 
     */
    public String run() {
        setStartTime();
        log.debug(PROCESS_STARTED + " " + getKey());

        if (!this.validateType()) {
            log.debug("[fe] Not enough information to continue the task ("
                    + this.type + ")");
            throw new HystrixBadRequestException("Not enough information to continue the task (" +  this.type + ")");
        }

        if (!goNext()) {
            log.debug("[fe] Cancelled");
            return STATUS_CANCELED;
        }

        this.layerPermission = hasPermissionsForJob();
        if (!this.layerPermission) {
            log.debug("[fe] Session (" + this.session.getSession()
                    + ") has no permissions for getting the layer ("
                    + this.layerId + ")");
            Map<String, Object> output = new HashMap<String, Object>();
            output.put(OUTPUT_LAYER_ID, this.layerId);
            output.put(OUTPUT_ONCE, true);
            output.put(OUTPUT_MESSAGE, "wfs_no_permissions");
            this.service.addResults(session.getClient(),
                    TransportService.CHANNEL_ERROR, output);
            throw new HystrixBadRequestException("Session (" +  this.session.getSession() + ") has no permissions for getting the layer (" + this.layerId + ")");
        }

        if (!goNext()) {
            log.debug("FE Cancelled");
            return STATUS_CANCELED;
        }
        this.layer = getLayerForJob();
        if (this.layer == null) {
            log.debug("[fe] Layer (" + this.layerId
                    + ") configurations couldn't be fetched");
            Map<String, Object> output = new HashMap<String, Object>();
            output.put(OUTPUT_LAYER_ID, this.layerId);
            output.put(OUTPUT_ONCE, true);
            output.put(OUTPUT_MESSAGE, "wfs_configuring_layer_failed");
            this.service.addResults(session.getClient(),
                    TransportService.CHANNEL_ERROR, output);
            throw new RuntimeException("Layer (" +  this.layerId + ") configurations couldn't be fetched");
        }

        setResourceSending();

        if (!validateMapScales()) {
            log.debug("[fe] Map scale was not valid for layer " + this.layerId);
            throw new HystrixBadRequestException("Map scale was not valid for layer (" + this.layerId + ")");
        }

        // if different SRS, create transforms for geometries
        if (!this.session.getLocation().getSrs()
                .equals(this.layer.getSRSName())) {
            this.transformService = this.session.getLocation()
                    .getTransformForService(this.layer.getCrs(), true);
            this.transformClient = this.session.getLocation()
                    .getTransformForClient(this.layer.getCrs(), true);
        }

        String cacheStyleName = this.session.getLayers().get(this.layerId)
                .getStyleName();
        if (cacheStyleName.startsWith(WFSImage.PREFIX_CUSTOM_STYLE)) {
            cacheStyleName += "_" + this.session.getSession();
        }

        // init enlarged envelope
        List<List<Double>> grid = this.session.getGrid().getBounds();
        if (grid.size() > 0) {
            this.session.getLocation().setEnlargedEnvelope(grid.get(0));
        }

        if (!goNext()) {
            log.debug("[fe] Cancelled");
            return STATUS_CANCELED;
        }

        if (this.type == JobType.NORMAL) { // tiles for grid
            if (!this.layer.isTileRequest()) { // make single request
                log.debug("[fe] single request");
                if (!this.normalHandlers(null, true)) {
                    log.debug("[fe] !normalHandlers leaving");
                    return STATUS_CANCELED;
                } else {
                    log.debug("[fe] single request - continue");
                }
            } else {
                log.debug("[fe] MAKING TILED REQUESTS");
            }

            boolean first = true;
            int index = 0;
            for (List<Double> bounds : grid) {

                log.debug("[fe] ... " + bounds);
                if (!goNext()) {
                    log.debug("[fe] JOB cancelled - leaving");
                    return STATUS_CANCELED;
                }

                if (this.layer.isTileRequest()) { // make a request per tile
                    log.debug("[fe] MAKING TILE REQUEST " + bounds);
                    if (!this.normalHandlers(bounds, first)) {
                        log.debug("FE !normalHandlers continuing tiles");
                        continue;
                    }
                }

                if (!goNext()) {
                    log.debug("[fe] JOB cancelled - leaving");
                    return STATUS_CANCELED;
                }

                boolean isThisTileNeeded = true;

                if (!this.sendImage) {
                    log.debug("[fe] !sendImage - not sending PNG");
                    isThisTileNeeded = false;
                }

                if (!this.sessionLayer.isTile(bounds)) {
                    log.debug("[fe] !layer.isTile - not sending PNG");
                    isThisTileNeeded = false;
                }

                if (isThisTileNeeded) {// this.sendImage ) { // &&
                                       // this.sessionLayer.isTile(bounds)) { //
                                       // check
                                       // if
                                       // needed
                                       // tile
                    Double[] bbox = new Double[4];
                    for (int i = 0; i < bbox.length; i++) {
                        bbox[i] = bounds.get(i);
                    }

                    // get from cache
                    BufferedImage bufferedImage = getImageCache(bbox);
                    boolean fromCache = (bufferedImage != null);
                    boolean isboundaryTile = this.session.getGrid()
                            .isBoundsOnBoundary(index);

                    if (!fromCache) {
                        if (this.image == null) {
                            this.image = createResponseImage();
                        }
                        bufferedImage = this.image.draw(
                                this.session.getTileSize(),
                                this.session.getLocation(), bounds,
                                this.features);
                        if (bufferedImage == null) {
                            this.imageParsingFailed();
                            throw new RuntimeException("Image parsing failed!");
                        }

                        // set to cache
                        if (!isboundaryTile) {
                            setImageCache(bufferedImage, cacheStyleName, bbox,
                                    true);
                        } else { // non-persistent cache - for ie
                            setImageCache(bufferedImage, cacheStyleName, bbox,
                                    false);
                        }
                    }

                    String url = createImageURL(
                            this.session.getLayers().get(this.layerId)
                                    .getStyleName(), bbox);
                    this.sendWFSImage(url, bufferedImage, bbox, true,
                            isboundaryTile);
                } else {
                    log.debug("[fe] Tile not needed? " + bounds);
                }

                if (first) {
                    first = false;
                    this.session.setKeepPrevious(true); // keep the next tiles
                }
                index++;
            }
        } else if (this.type == JobType.HIGHLIGHT) {

            /* NOPE */

        } else if (this.type == JobType.MAP_CLICK) {
            if (!this.requestHandler(null)) {
                return STATUS_CANCELED;
            }
            this.featuresHandler();
            if (!goNext()) {
                log.debug("[fe] Cancelled");
                return STATUS_CANCELED;
            }

            Double[] bounds = session.getLocation().getBboxArray();
            List<Double> boundsList = Arrays.asList(bounds);
            BufferedImage bufferedImage = null;

            boolean isboundaryTile = false;

            this.image = createHighlightImage();

            bufferedImage = this.image.draw(this.session.getMapSize(),
                    this.session.getLocation(), this.features);
            if (bufferedImage == null) {
                this.imageParsingFailed();
                throw new RuntimeException("Image parsing failed!");
            }

            String imageURL = createImageURL(
                    this.session.getLayers().get(this.layerId).getStyleName(),
                    bounds);
            this.type = JobType.HIGHLIGHT;
            this.sendWFSImage(imageURL, bufferedImage, bounds, false, false);

            bufferedImage.flush();

            /* features */

            if (this.sendFeatures) {
                log.debug("[fe] sending features for map click");
                this.sendWFSFeatures(this.featureValuesList,
                        TransportService.CHANNEL_MAP_CLICK);
            } else {
                log.debug("[fe] NOT sending features for map click");
            }

        } else if (this.type == JobType.GEOJSON) {
            if (!this.requestHandler(null)) {
                return STATUS_CANCELED;
            }
            this.featuresHandler();
            if (!goNext()) {
                log.debug("[fe] Cancelled");
                return STATUS_CANCELED;
            }
            if (this.sendFeatures) {
                this.sendWFSFeatures(this.featureValuesList,
                        TransportService.CHANNEL_FILTER);
            }
        } else {
            log.debug("[fe] Type is not handled " + this.type);
        }

        log.debug("[fe] " + PROCESS_ENDED + " " + getKey());
        return "success";
    }

    /**
     * Checks if enough information for running the task type
     *
     * @return <code>true</code> if enough information for type;
     *         <code>false</code> otherwise.
     */
    protected boolean validateType() {
        // TODO: check if this was just forgotten or can't FE handle PROPERTY_FILTER at all
        if(this.type == JobType.PROPERTY_FILTER) {
            return false;
        }
        return super.validateType();
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

        Map<String, Object> output = new HashMap<String, Object>();
        try {

            // request failed
            if (response == null) {
                log.debug("Request failed for layer" + layer.getLayerId());
                output.put(OUTPUT_LAYER_ID, layer.getLayerId());
                output.put(OUTPUT_ONCE, true);
                output.put(OUTPUT_MESSAGE, "wfs_request_failed");
                this.service.addResults(session.getClient(),
                        TransportService.CHANNEL_ERROR, output);
                log.debug(PROCESS_ENDED + getKey());
                return false;
            }

            // parse response
            this.features = response(layer, response);

            // parsing failed
            if (this.features == null) {
                log.debug("Parsing failed for layer " + this.layerId);
                output.put(OUTPUT_LAYER_ID, this.layerId);
                output.put(OUTPUT_ONCE, true);
                output.put(OUTPUT_MESSAGE, "features_parsing_failed");
                this.service.addResults(session.getClient(),
                        TransportService.CHANNEL_ERROR, output);
                log.debug(PROCESS_ENDED + getKey());
                return false;
            }

            // 0 features found - send size
            if (this.type == JobType.MAP_CLICK && this.features.size() == 0) {
                log.debug("Empty result for map click" + this.layerId);
                output.put(OUTPUT_LAYER_ID, this.layerId);
                output.put(OUTPUT_FEATURES, "empty");
                output.put(OUTPUT_KEEP_PREVIOUS, this.session.isKeepPrevious());
                this.service.addResults(session.getClient(),
                        TransportService.CHANNEL_MAP_CLICK, output);
                log.debug(PROCESS_ENDED + getKey());
                return false;
            } else if (this.type == JobType.GEOJSON && this.features.size() == 0) {
                log.debug("Empty result for filter" + this.layerId);
                output.put(OUTPUT_LAYER_ID, this.layerId);
                output.put(OUTPUT_FEATURES, "empty");
                this.service.addResults(session.getClient(),
                        TransportService.CHANNEL_FILTER, output);
                log.debug(PROCESS_ENDED + getKey());
                return false;
            } else {
                if (this.features.size() == 0) {
                    log.debug("Empty result" + this.layerId);
                    output.put(OUTPUT_LAYER_ID, this.layerId);
                    output.put(OUTPUT_FEATURE, "empty");
                    this.service.addResults(session.getClient(),
                            TransportService.CHANNEL_FEATURE, output);
                    log.debug(PROCESS_ENDED + getKey());
                    return false;
                } else if (this.features.size() == layer.getMaxFeatures()) {
                    log.debug("Max feature result" + this.layerId);
                    output.put(OUTPUT_LAYER_ID, this.layerId);
                    output.put(OUTPUT_FEATURE, "max");
                    this.service.addResults(session.getClient(),
                            TransportService.CHANNEL_FEATURE, output);
                }
            }

            log.debug("Features count" + this.features.size());
        } catch (Exception ee) {
            log.debug("exception: " + ee);
        }

        return true;
    }

}
