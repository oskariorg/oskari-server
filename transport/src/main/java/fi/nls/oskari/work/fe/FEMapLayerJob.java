package fi.nls.oskari.work.fe;

import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.fe.engine.GroovyFeatureEngine;
import fi.nls.oskari.fe.input.XMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.StaxGMLInputProcessor;
import fi.nls.oskari.fe.input.format.gml.recipe.GroovyParserRecipe;
import fi.nls.oskari.fe.iri.Resource;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.fe.schema.XSDDatatype;
import fi.nls.oskari.pojo.Location;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.pojo.Tile;
import fi.nls.oskari.pojo.WFSLayerStore;
import fi.nls.oskari.transport.TransportService;
import fi.nls.oskari.wfs.WFSFilter;
import fi.nls.oskari.wfs.WFSImage;
import fi.nls.oskari.work.OWSMapLayerJob;
import fi.nls.oskari.work.RequestResponse;
import fi.nls.oskari.work.ResultProcessor;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.sld.SLDConfiguration;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

public class FEMapLayerJob extends OWSMapLayerJob {
    
    /*
     * 
     * WFSLayerStore
     * 
     *   "customParser" : "oskari-feature-engine",
     *   "requestTemplate" : "/resource/path/to/request/template.xml",
     *   "responseTemplate" : "/resource/path/to/response/groovy.groovy"1
     */


    static GroovyClassLoader gcl = new GroovyClassLoader();

    static Map<String, Class<GroovyParserRecipe>> recipeClazzes =
            new HashMap<String, Class<GroovyParserRecipe>>();

    static GroovyParserRecipe getRecipe(String recipePath)
            throws InstantiationException, IllegalAccessException {

        Class<GroovyParserRecipe> recipeClazz = recipeClazzes.get(recipePath);

        if (recipeClazz == null) {

            InputStreamReader reader = new InputStreamReader(
                    FEMapLayerJob.class.getResourceAsStream(recipePath));

            GroovyCodeSource codeSource = new GroovyCodeSource(reader,
                    recipePath, ".");

            recipeClazz = (Class<GroovyParserRecipe>) gcl
                    .parseClass(codeSource, true);


            recipeClazzes.put(recipePath, recipeClazz);
        }

        return recipeClazz.newInstance();

    }

    final ArrayList<String> selectedProperties = new ArrayList<String>();

    final Map<Resource, Integer> selectedPropertiesIndex = new HashMap<Resource, Integer>();

    public FEMapLayerJob(ResultProcessor service, Type type,
                         SessionStore store, String layerId) {
        super(service, type, store, layerId);

    }

    public FEMapLayerJob(ResultProcessor service, Type type,
                         SessionStore store, String layerId, boolean reqSendFeatures,
                         boolean reqSendImage, boolean reqSendHighlight) {
        super(service, type, store, layerId, reqSendFeatures, reqSendImage,
                reqSendHighlight);

    }

    protected WFSImage createHighlightImage() {
        final String styleName = WFSImage.STYLE_HIGHLIGHT;
        final Style style =
                createSLDStyle(WFSImage.class.getResourceAsStream(WFSImage.DEFAULT_SLD)); // getClass() (non-static)

        return new WFSImage(this.layer, this.session.getClient(),
                WFSImage.STYLE_DEFAULT, Type.HIGHLIGHT.toString()) {
            protected Style getSLDStyle(WFSLayerStore layer, String styleName) {
                return style;
            }
        };
    }

    /**
     * Parses SLD style from a String (XML)
     *
     * @param xml
     * @return sld
     */
    static protected Style createSLDStyle(String xml) {
        if (xml == null) {
            log.info("Trying to create style from <null> String!");
            return null;
        }
        final Style style = createSLDStyle(new ByteArrayInputStream(xml.getBytes()));
        if (style == null) {
            log.warn("Couldn't create style from XML:", xml);
        }
        return style;
    }

    static protected Style createSLDStyle(InputStream xml) {
        Configuration config = new SLDConfiguration();

        Parser parser = new Parser(config);
        StyledLayerDescriptor sld = null;
        try {
            sld = (StyledLayerDescriptor) parser.parse(xml);
        } catch (Exception e) {
            log.debug(e + "Failed to create SLD Style");

            return null;
        }
        return SLD.styles(sld)[0];
    }


    protected WFSImage createResponseImage() {
        final String styleName = WFSImage.STYLE_HIGHLIGHT;
        final Style style = createSLDStyle(WFSImage.class.getResourceAsStream(WFSImage.DEFAULT_SLD)); // getClass() (non-static)

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

    /**
     * Gets image from cache
     *
     * @param bbox
     */
    protected BufferedImage getImageCache(Double[] bbox) {
        return WFSImage.getCache(this.layerId,
                this.session.getLayers().get(this.layerId).getStyleName(),
                this.session.getLocation().getSrs(), bbox, this.session
                .getLocation().getZoom());

    }

    /**
     * Parses features properties and sends to appropriate channels
     */
    protected void propertiesHandler() {
        this.sendWFSProperties(selectedProperties,
                this.layer.getFeatureParamsLocales(this.session.getLanguage()));
    }

    @Override
    public RequestResponse request(final Type type, final WFSLayerStore layer,
                                   final SessionStore session, final List<Double> bounds,
                                   final MathTransform transformService) {

        final String layerUrl = layer.getURL(); // "krysp";
        final String srsName = layer.getSRSName();
        final String featureNs = layer.getFeatureNamespaceURI();
        final String featureName = layer.getFeatureElement();
        final String username = layer.getUsername();
        final String password = layer.getPassword();
        final String WFSver = layer.getWFSVersion();
        final String geomProp = layer.getGMLGeometryProperty();
        final String geomNs = layer.getGeometryNamespaceURI();
        final FERequestTemplate backendRequestTemplate = new FERequestTemplate(layer.getRequestTemplate());
        final FERequestResponse requestResponse = new FERequestResponse();
        //

        final ArrayList<List<Object>> list = new ArrayList<List<Object>>();
        this.featureValuesList = list;

        final Map<Resource, SimpleFeatureCollection> responseCollections = new HashMap<Resource, SimpleFeatureCollection>();
        final Map<Resource, SimpleFeatureBuilder> responseBuilders = new HashMap<Resource, SimpleFeatureBuilder>();
        final Map<Resource, List<SimpleFeature>> responseFeatures = new HashMap<Resource, List<SimpleFeature>>();

        if (!validateMapScales()) {
            log.debug("Map scale was not valid for layer " + this.layerId);

            return requestResponse;
        }

        if (backendRequestTemplate == null) {
            return requestResponse;
        }

        try {
            /* CRS */
            final CoordinateReferenceSystem crs = CRS.decode(session
                    .getLocation().getSrs());
            final MathTransform transform = this.session.getLocation()
                    .getTransformForClient(this.layer.getCrs(), true);

            /* FeatureEngine Engine */
            final GroovyFeatureEngine engine = new GroovyFeatureEngine();

            /* FeatureEngine InputProcessor */
            final XMLInputProcessor inputProcessor = new StaxGMLInputProcessor();

            /* FeatureEngine OutputProcessor */
            final OutputProcessor outputProcessor = new OutputProcessor() {

                // private SimpleFeatureBuilder sfb;
                // private List<SimpleFeature> sfc;

                public void begin() throws IOException {
                    /* Setup MAP */

                }

                @Override
                public void edge(Resource subject, Resource predicate,
                                 Resource value) throws IOException {
                }

                @Override
                public void end() throws IOException {
                    for (Resource type : responseFeatures.keySet()) {
                        List<SimpleFeature> sfc = getAndSetListSimpleFeature(type);

                        SimpleFeatureCollection fc = DataUtilities
                                .collection(sfc);
                        responseCollections.put(type, fc);
                    }
                }

                @Override
                public void flush() throws IOException {
                }

                public SimpleFeatureBuilder getAndSetFeatureBuilder(
                        Resource type) {

                    SimpleFeatureBuilder sfb = responseBuilders.get(type);
                    if (sfb == null) {

                        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
                        ftb.setName(type.getLocalPart());
                        ftb.setNamespaceURI(type.getNs());

                        // add a geometry property

                        ftb.setCRS(crs); // set crs first
                        ftb.add("geom", Geometry.class, crs); // then add
                        // geometry

                        SimpleFeatureType schema = ftb.buildFeatureType();

                        sfb = new SimpleFeatureBuilder(schema);

                        responseBuilders.put(type, sfb);
                    }
                    return sfb;
                }

                public List<SimpleFeature> getAndSetListSimpleFeature(
                        Resource type) {

                    List<SimpleFeature> list = responseFeatures.get(type);
                    if (list == null) {
                        list = new LinkedList<SimpleFeature>();
                        responseFeatures.put(type, list);
                    }
                    return list;
                }

                @Override
                public void prefix(String prefix, String ns) throws IOException {
                }

                @Override
                public void type(Resource type,
                                 List<Pair<Resource, XSDDatatype>> simpleProperties,
                                 List<Pair<Resource, Object>> linkProperties,
                                 List<Pair<Resource, String>> geometryProperties)
                        throws IOException {
                    requestResponse.setFeatureIri(type);

                    log.debug("properties handler");

                    /*
                     * List<String> layerSelectedProperties = layer
                     * .getSelectedFeatureParams(session.getLanguage());
                     */
                    selectedProperties.add(0, "__fid");

                    for (Pair<Resource, XSDDatatype> prop : simpleProperties) {

                        selectedPropertiesIndex.put(prop.getKey(),
                                selectedProperties.size());

                        selectedProperties.add(prop.getKey().getLocalPart());

                    }
                    selectedProperties.add("__centerX");
                    selectedProperties.add("__centerY");

                }

                public void vertex(final Resource iri, final Resource type,
                                   final List<Pair<Resource, ?>> simpleProperties,
                                   final List<Pair<Resource, ?>> linkProperties)
                        throws IOException {
                }

                public void vertex(Resource iri, Resource type,
                                   List<Pair<Resource, ?>> simpleProperties,
                                   List<Pair<Resource, ?>> linkProperties,
                                   List<Pair<Resource, Geometry>> geometryProperties)
                        throws IOException {

                    SimpleFeatureBuilder sfb = getAndSetFeatureBuilder(type);
                    List<SimpleFeature> sfc = getAndSetListSimpleFeature(type);

                    for (Pair<Resource, Geometry> geomPair : geometryProperties) {
                        Geometry geom = geomPair.getValue();

                        try {
                            geom = JTS.transform(geom, transform);
                        } catch (MismatchedDimensionException e) {

                            throw new IOException(e);
                        } catch (TransformException e) {

                            throw new IOException(e);
                        }

                        sfb.add(geom);

                        SimpleFeature f = sfb.buildFeature(iri.toString());

                        sfc.add(f);

                    }

                    if (!(type.getNs().equals(
                            requestResponse.getFeatureIri().getNs()) && type
                            .getLocalPart().equals(
                                    requestResponse.getFeatureIri()
                                            .getLocalPart()))) {
                        return;
                    }

                    if (selectedProperties != null
                            && selectedProperties.size() > 0) {
                        ArrayList<Object> props = new ArrayList<Object>(
                                selectedProperties.size());
                        for (String field : selectedProperties) {
                            props.add(null);
                        }
                        props.set(0, iri.toString());
                        for (Pair<Resource, ?> pair : simpleProperties) {
                            Integer keyIndex = selectedPropertiesIndex.get(pair
                                    .getKey());
                            if (keyIndex == null) {

                                continue;
                            }
                            props.set(keyIndex, pair.getValue());
                        }

                        list.add(props);
                    }
                }

            };

            /* Backend HTTP URI info */
            URL url = new URL(layerUrl);

            /* Backend Proxy */
            HttpHost backendProxy = null;

            if (System.getProperty("http.proxyHost") != null
                    && System.getProperty("http.proxyPort") != null) {


                backendProxy = new HttpHost(
                        System.getProperty("http.proxyHost"),
                        Integer.valueOf(
                                System.getProperty("http.proxyPort"), 10),
                        "http");

            }

            /* Recipe */
            final GroovyParserRecipe recipe = getRecipe(layer.getResponseTemplate());

            /* Backend HTTP Response Handler */
            ResponseHandler<Boolean> backendResponseHandler = new ResponseHandler<Boolean>() {

                @Override
                public Boolean handleResponse(HttpResponse response)
                        throws ClientProtocolException, IOException {

                    Boolean succee = false;

                    StatusLine statusLine = response.getStatusLine();
                    HttpEntity entity = response.getEntity();
                    if (statusLine.getStatusCode() >= 300) {
                        throw new HttpResponseException(
                                statusLine.getStatusCode(),
                                statusLine.getReasonPhrase());
                    }
                    if (entity == null) {
                        throw new ClientProtocolException(
                                "Response contains no content");
                    }

                    ContentType contentType = ContentType.getOrDefault(entity);
                    Charset charset = contentType.getCharset();

                    BufferedInputStream inp = new BufferedInputStream(
                            entity.getContent());

                    try {

                        inputProcessor.setInput(inp);

                        engine.setRecipe(recipe);

                        engine.setInputProcessor(inputProcessor);
                        engine.setOutputProcessor(outputProcessor);

                        engine.process();

                        requestResponse.setResponse(responseCollections);

                        requestResponse.setLocation(session.getLocation());

                        Filter filter = WFSFilter.initBBOXFilter(
                                session.getLocation(), layer);
                        requestResponse.setFilter(filter);

                        succee = true;

                    } catch (XMLStreamException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } finally {
                        if (inp != null) {
                            inp.close();
                        }
                    }

                    return succee;
                }

            };

            /* Backend HTTP Request */
            HttpUriRequest backendUriRequest = null;
            if (backendRequestTemplate.isPost) {
                HttpPost httppost = new HttpPost(new URI(url.toExternalForm()));

                StringBuffer params = new StringBuffer();

                backendRequestTemplate.buildParams(params, type, layer,
                        session, bounds, transform, crs);

                StringEntity entity = new StringEntity(params.toString());

                httppost.setEntity(entity);

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
                backendUriRequest = httpget;

            }

            /* Backend HTTP Executor */
            DefaultHttpClient backendHttpClient = new DefaultHttpClient();
            try {
                HttpHost backendHttpHost = new HttpHost(url.getHost(),
                        url.getPort(), url.getProtocol());


                UsernamePasswordCredentials backendCredentials = null;
                if (username != null && password != null) {
                    backendCredentials = new UsernamePasswordCredentials(username, password);
                }

                BasicHttpContext backendLocalContext = null;
                if (backendCredentials != null) {
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
                    backendHttpClient.getParams().setParameter(
                            ConnRoutePNames.DEFAULT_PROXY,

                            backendProxy);
                }

                Boolean succee = backendLocalContext != null ? backendHttpClient
                        .execute(backendUriRequest, backendResponseHandler,
                                backendLocalContext) : backendHttpClient
                        .execute(backendUriRequest, backendResponseHandler);

            } finally {
                // When HttpClient instance is no longer needed,
                // shut down the connection manager to ensure
                // immediate deallocation of all system resources
                backendHttpClient.getConnectionManager().shutdown();
            }

            /* TO-DO fix some error handling and user feedback */
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        } catch (NoSuchAuthorityCodeException e2) {
            e2.printStackTrace();
        } catch (FactoryException e2) {
            e2.printStackTrace();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        } catch (TransformException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } finally {

        }

        return requestResponse;
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
     * Process of the job
     * <p/>
     * Worker calls this when starts the job.
     */
    @Override
    public void run() {
        log.debug(PROCESS_STARTED + " " + getKey());

        if (!this.validateType()) {
            log.debug("Not enough information to continue the task ("
                    + this.type + ")");
            return;
        }

        if (!goNext()) {
            log.debug("Cancelled");
            return;
        }

        this.layerPermission = getPermissions(layerId,
                this.session.getSession(), this.session.getRoute());
        if (!this.layerPermission) {
            log.debug("Session (" + this.session.getSession()
                    + ") has no permissions for getting the layer ("
                    + this.layerId + ")");
            Map<String, Object> output = new HashMap<String, Object>();
            output.put(OUTPUT_LAYER_ID, this.layerId);
            output.put(OUTPUT_ONCE, true);
            output.put(OUTPUT_MESSAGE, "wfs_no_permissions");
            this.service.addResults(session.getClient(),
                    TransportService.CHANNEL_ERROR, output);
            return;
        }

        if (!goNext()) {
            log.debug("Cancelled");
            return;
        }
        this.layer = getLayerConfiguration(this.layerId,
                this.session.getSession(), this.session.getRoute());
        if (this.layer == null) {
            log.debug("Layer (" + this.layerId
                    + ") configurations couldn't be fetched");
            Map<String, Object> output = new HashMap<String, Object>();
            output.put(OUTPUT_LAYER_ID, this.layerId);
            output.put(OUTPUT_ONCE, true);
            output.put(OUTPUT_MESSAGE, "wfs_configuring_layer_failed");
            this.service.addResults(session.getClient(),
                    TransportService.CHANNEL_ERROR, output);
            return;
        }

        setResourceSending();

        if (!validateMapScales()) {
            log.debug("Map scale was not valid for layer " + this.layerId);
            return;
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
            log.debug("Cancelled");
            return;
        }

        if (this.type == Type.NORMAL) { // tiles for grid
            if (!this.layer.isTileRequest()) { // make single request
                log.debug("MAKING SINGLE REQUEST");
                if (!this.normalHandlers(null, true)) {
                    log.debug("!normalHandlers leaving");
                    return;
                }
            } else {
                log.debug("MAKING TILED REQUESTS");
            }

            boolean first = true;
            int index = 0;
            for (List<Double> bounds : grid) {

                log.debug("... " + bounds);
                if (!goNext()) {
                    log.debug("JOB cancelled - leaving");
                    return;
                }

                if (this.layer.isTileRequest()) { // make a request per tile
                    log.debug("MAKING TILE REQUEST " + bounds);
                    if (!this.normalHandlers(bounds, first)) {
                        log.debug("!normalHandlers continuing tiles");
                        continue;
                    }
                }

                if (!goNext()) {
                    log.debug("JOB cancelled - leaving");
                    return;
                }

                if (this.sendImage && this.sessionLayer.isTile(bounds)) { // check
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
                            return;
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
                    log.debug("Tile not needed? " + bounds);
                }

                if (first) {
                    first = false;
                    this.session.setKeepPrevious(true); // keep the next tiles
                }
                index++;
            }
        } else if (this.type == Type.HIGHLIGHT) {

            /* NOPE */

        } else if (this.type == Type.MAP_CLICK) {
            if (!this.requestHandler(null)) {
                return;
            }
            this.featuresHandler();
            if (!goNext()) {
                log.debug("Cancelled");
                return;
            }

            Double[] bounds = session.getLocation().getBboxArray();
            List<Double> boundsList = Arrays.asList(bounds);
            BufferedImage bufferedImage = null;

            boolean isboundaryTile = false;

            this.image = createHighlightImage();

            bufferedImage = this.image.draw(this.session.getMapSize(),
                    this.session.getLocation(), this.features);
            if (bufferedImage == null) {
                return;
            }

            String url = createImageURL(
                    this.session.getLayers().get(this.layerId).getStyleName(),
                    bounds);
            this.type = Type.HIGHLIGHT;
            this.sendWFSImage(url, bufferedImage, bounds, false, false);

            bufferedImage.flush();

            /* features */

            /*
             * if (this.sendFeatures) { log.debug("... sending features");
             * this.sendWFSFeatures(this.featureValuesList,
             * TransportService.CHANNEL_MAP_CLICK); } else {
             * log.debug("NOT sending features"); }
             */

        } else if (this.type == Type.GEOJSON) {
            if (!this.requestHandler(null)) {
                return;
            }
            this.featuresHandler();
            if (!goNext()) {
                log.debug("Cancelled");
                return;
            }
            if (this.sendFeatures) {
                this.sendWFSFeatures(this.featureValuesList,
                        TransportService.CHANNEL_FILTER);
            }
        } else {
            log.debug("Type is not handled " + this.type);
        }

        log.debug(PROCESS_ENDED + " " + getKey());
    }

    /**
     * Sends one feature
     *
     * @param values
     */
    protected void sendWFSFeature(List<Object> values) {
        if (values == null || values.size() == 0) {
            log.debug("Failed to send feature");
            return;
        }

        Map<String, Object> output = new HashMap<String, Object>();
        output.put(OUTPUT_LAYER_ID, this.layerId);
        output.put(OUTPUT_FEATURE, values);

        this.service.addResults(this.session.getClient(),
                TransportService.CHANNEL_FEATURE, output);

    }

    /**
     * Sends list of features
     *
     * @param features
     * @param channel
     */
    protected void sendWFSFeatures(List<List<Object>> features, String channel) {
        if (features == null || features.size() == 0) {
            log.debug("No features to Send");
            return;
        }

        log.debug("#### Sending " + features.size() + "  FEATURES");

        Map<String, Object> output = new HashMap<String, Object>();
        output.put(OUTPUT_LAYER_ID, this.layerId);
        output.put(OUTPUT_FEATURES, features);
        if (channel.equals(TransportService.CHANNEL_MAP_CLICK)) {
            output.put(OUTPUT_KEEP_PREVIOUS, this.session.isKeepPrevious());
        }

        this.service.addResults(this.session.getClient(), channel, output);
    }


    /**
     * Sends properties (fields and locales)
     *
     * @param fields
     * @param locales
     */
    protected void sendWFSProperties(List<String> fields, List<String> locales) {
        if (fields == null || fields.size() == 0) {
            log.debug("Failed to send properties");
            return;
        }

        if (locales != null) {
            locales.add(0, "ID");
            locales.add("x");
            locales.add("y");
        } else {
            locales = new ArrayList<String>();
        }

        Map<String, Object> output = new HashMap<String, Object>();
        output.put(OUTPUT_LAYER_ID, this.layerId);
        output.put(OUTPUT_FIELDS, fields);
        output.put(OUTPUT_LOCALES, locales);

        this.service.addResults(this.session.getClient(),
                TransportService.CHANNEL_PROPERTIES, output);
    }

    /**
     * Sets image to cache
     *
     * @param bufferedImage
     * @param style
     * @param bbox
     * @param persistent
     */
    protected void setImageCache(BufferedImage bufferedImage,
                                 final String style, Double[] bbox, boolean persistent) {

        WFSImage.setCache(bufferedImage, this.layerId, style, this.session
                .getLocation().getSrs(), bbox, this.session.getLocation()
                .getZoom(), persistent);

    }

    protected boolean validateMapScales() {
        double scale = this.session.getMapScales().get(
                (int) this.session.getLocation().getZoom());
        double minScaleInMapSrs = units.getScaleInSrs(layer.getMinScale(),
                session.getLocation().getSrs(), session.getLocation().getSrs());
        double maxScaleInMapSrs = units.getScaleInSrs(layer.getMaxScale(),
                session.getLocation().getSrs(), session.getLocation().getSrs());

        log.debug("Scale in:" + layer.getSRSName() + scale + "["
                + layer.getMaxScale() + "," + layer.getMinScale() + "]");
        log.debug("Scale in:" + session.getLocation().getSrs() + scale + "["
                + maxScaleInMapSrs + "," + minScaleInMapSrs + "]");
        if (minScaleInMapSrs >= scale && maxScaleInMapSrs <= scale) // min ==
            // biggest
            // value
            return true;
        return false;
    }

    /**
     * Sets which resources will be sent (features, image)
     */
    protected void setResourceSending() {
        // layer configuration is the default
        this.sendFeatures = layer.isGetFeatureInfo();
        this.sendImage = layer.isGetMapTiles();
        this.sendHighlight = layer.isGetHighlightImage();

        // if request defines false and layer configuration allows
        if (!this.reqSendFeatures && this.sendFeatures)
            this.sendFeatures = false;
        if (!this.reqSendImage && this.sendImage)
            this.sendImage = false;
        if (!this.reqSendHighlight && this.sendHighlight)
            this.sendHighlight = false;

        log.debug("send - features:", this.sendFeatures, "image:", this.sendImage, "highlight:", this.sendHighlight);
    }

    /**
     * Sends image as an URL to IE 8 & 9, base64 data for others
     *
     * @param url
     * @param bufferedImage
     * @param bbox
     * @param isTiled
     */
    protected void sendWFSImage(String url, BufferedImage bufferedImage, Double[] bbox, boolean isTiled, boolean isboundaryTile) {
        if (bufferedImage == null) {
            log.warn("Failed to send image");
            return;
        }

        Map<String, Object> output = new HashMap<String, Object>();
        output.put(OUTPUT_LAYER_ID, this.layerId);

        Location location = this.session.getLocation();

        Tile tileSize = null;
        if (isTiled) {
            tileSize = this.session.getTileSize();
        } else {
            tileSize = this.session.getMapSize();
        }

        output.put(OUTPUT_IMAGE_SRS, location.getSrs());
        output.put(OUTPUT_IMAGE_BBOX, bbox);
        output.put(OUTPUT_IMAGE_ZOOM, location.getZoom());
        output.put(OUTPUT_IMAGE_TYPE, this.type); // "normal" | "highlight"
        output.put(OUTPUT_KEEP_PREVIOUS, this.session.isKeepPrevious());
        output.put(OUTPUT_BOUNDARY_TILE, isboundaryTile);
        output.put(OUTPUT_IMAGE_WIDTH, tileSize.getWidth());
        output.put(OUTPUT_IMAGE_HEIGHT, tileSize.getHeight());
        output.put(OUTPUT_IMAGE_URL, url);

        byte[] byteImage = WFSImage.imageToBytes(bufferedImage);
        String base64Image = WFSImage.bytesToBase64(byteImage);
        int base64Size = (base64Image.length() * 2) / 1024;

        // IE6 & IE7 doesn't support base64, max size in base64 for IE8 is 32KB
        if (!(this.session.getBrowser().equals(BROWSER_MSIE) && this.session.getBrowserVersion() < 8 ||
                this.session.getBrowser().equals(BROWSER_MSIE) && this.session.getBrowserVersion() == 8 &&
                        base64Size >= 32)) {
            output.put(OUTPUT_IMAGE_DATA, base64Image);
        }

        this.service.addResults(this.session.getClient(), TransportService.CHANNEL_IMAGE, output);
    }

    /**
     * Checks if enough information for running the task type
     *
     * @return <code>true</code> if enough information for type; <code>false</code>
     *         otherwise.
     */
    protected boolean validateType() {
        if (this.type == Type.HIGHLIGHT) {
            if (this.sessionLayer.getHighlightedFeatureIds() != null &&
                    this.sessionLayer.getHighlightedFeatureIds().size() > 0) {
                return true;
            }
        } else if (this.type == Type.MAP_CLICK) {
            if (session.getMapClick() != null) {
                return true;
            }
        } else if (this.type == Type.GEOJSON) {
            if (session.getFilter() != null) {
                return true;
            }
        } else if (this.type == Type.NORMAL) {
            return true;
        }
        return false;
    }

    /**
     * Wrapper for normal type job's handlers
     */
    protected boolean normalHandlers(List<Double> bounds, boolean first) {
        if (!this.requestHandler(bounds)) {
            log.debug("Cancelled by request handler");
            return false;
        }
        if (first) {
            propertiesHandler();
            if (!goNext())
                return false;
        }
        if (!goNext())
            return false;
        this.featuresHandler();
        if (!goNext())
            return false;
        return true;
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
            if (this.type == Type.MAP_CLICK && this.features.size() == 0) {
                log.debug("Empty result for map click" + this.layerId);
                output.put(OUTPUT_LAYER_ID, this.layerId);
                output.put(OUTPUT_FEATURES, "empty");
                output.put(OUTPUT_KEEP_PREVIOUS, this.session.isKeepPrevious());
                this.service.addResults(session.getClient(),
                        TransportService.CHANNEL_MAP_CLICK, output);
                log.debug(PROCESS_ENDED + getKey());
                return false;
            } else if (this.type == Type.GEOJSON && this.features.size() == 0) {
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
