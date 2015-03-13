package fi.nls.oskari.work.hystrix;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.*;
import fi.nls.oskari.transport.TransportService;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wfs.WFSImage;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.wfs.util.HttpHelper;
import fi.nls.oskari.work.JobType;
import fi.nls.oskari.work.RequestResponse;
import fi.nls.oskari.work.ResultProcessor;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.operation.MathTransform;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

/**
 * Job for WFS Map Layer
 */
public abstract class HystrixMapLayerJob extends HystrixJob {

    public static final String STATUS_CANCELED = "canceled";

    protected static final Logger log = LogFactory
            .getLogger(HystrixMapLayerJob.class);

    protected static Set<String> excludedProperties = new HashSet<String>();
    static {
        excludedProperties.add("metaDataProperty");
        excludedProperties.add("description");
        excludedProperties.add("name");
        excludedProperties.add("boundedBy");
        excludedProperties.add("location");
    }

    public static final String OUTPUT_LAYER_ID = "layerId";
    public static final String OUTPUT_ONCE = "once";
    public static final String OUTPUT_MESSAGE = "message";
    public static final String OUTPUT_FEATURES = "features";
    public static final String OUTPUT_GEOMETRIES = "geometries";
    public static final String OUTPUT_FEATURE = "feature";
    public static final String OUTPUT_FIELDS = "fields";
    public static final String OUTPUT_LOCALES = "locales";
    public static final String OUTPUT_KEEP_PREVIOUS = "keepPrevious";
    public static final String OUTPUT_STYLE = "style";

    public static final String OUTPUT_IMAGE_SRS = "srs";
    public static final String OUTPUT_IMAGE_BBOX = "bbox";
    public static final String OUTPUT_IMAGE_ZOOM = "zoom";
    public static final String OUTPUT_IMAGE_TYPE = "type";
    public static final String OUTPUT_IMAGE_WIDTH = "width";
    public static final String OUTPUT_IMAGE_HEIGHT = "height";
    public static final String OUTPUT_IMAGE_URL = "url";
    public static final String OUTPUT_IMAGE_DATA = "data";
    public static final String OUTPUT_BOUNDARY_TILE = "boundaryTile";

    public static final String BROWSER_MSIE = "msie";

    public static final String PROCESS_STARTED = "Started";
    public static final String PROCESS_ENDED = "Ended";

    // process information
    protected ResultProcessor service;
    protected SessionStore session;
    protected Layer sessionLayer;
    protected WFSLayerStore layer;
    protected WFSLayerPermissionsStore permissions;
    protected String layerId;
    protected boolean layerPermission;
    protected boolean reqSendFeatures;
    protected boolean reqSendImage;
    protected boolean reqSendHighlight;
    protected boolean sendFeatures;
    protected boolean sendImage;
    protected boolean sendHighlight;
    protected MathTransform transformService;
    protected MathTransform transformClient;
    protected JobType type;
    protected FeatureCollection<SimpleFeatureType, SimpleFeature> features;
    protected List<String> processedFIDs = new ArrayList<String>();
    protected List<List<Object>> featureValuesList;
    protected List<List<Object>> geomValuesList;

    protected WFSImage image = null;
    protected Units units = new Units();

    // API
    public static final String PERMISSIONS_API = "GetLayerIds";
    public static final String LAYER_CONFIGURATION_API = "GetWFSLayerConfiguration&id=";

    protected static final List<List<Object>> EMPTY_LIST = new ArrayList();
    // COOKIE
    public static final String ROUTE_COOKIE_NAME = PropertyUtil.get("oskari.cookie.route", "ROUTEID") + "=";

    // API URL (action routes)
    private final static String PARAM_ROUTE = "action_route";
    private static String SERVICE_URL = null; // default value perhaps?
    private static String SERVICE_URL_PATH = null;
    private static String SERVICE_URL_QUERYSTRING = null;
    private static String SERVICE_URL_SESSION_PARAM = PropertyUtil.get("oskari.cookie.session", null);

    /**
     * Creates a new runnable job with own Jedis instance
     *
     * Parameters define client's service (communication channel), session and
     * layer's id. Sends all resources that the layer configuration allows.
     *
     * @param service
     * @param store
     * @param layerId
     */
    public HystrixMapLayerJob(ResultProcessor service, JobType type,
                              SessionStore store, String layerId) {
        this(service, type, store, layerId, true, true, true);
    }

    /**
     * Creates a new runnable job with own Jedis instance
     *
     * Parameters define client's service (communication channel), session and
     * layer's id. Also sets resources that will be sent if the layer
     * configuration allows.
     *
     * @param service
     * @param store
     * @param layerId
     * @param reqSendFeatures
     * @param reqSendImage
     * @param reqSendHighlight
     */
    public HystrixMapLayerJob(ResultProcessor service, JobType type,
                              SessionStore store, String layerId, boolean reqSendFeatures,
                              boolean reqSendImage, boolean reqSendHighlight) {
        super("transport", "LayerJob_" + layerId + "_" + type.toString());
        setupAPIUrl();
        this.service = service;
        this.type = type;
        this.session = store;
        this.layerId = layerId;
        this.sessionLayer = this.session.getLayers().get(this.layerId);
        this.layer = null;
        this.permissions = null;
        this.layerPermission = false;
        this.reqSendFeatures = reqSendFeatures;
        this.reqSendImage = reqSendImage;
        this.reqSendHighlight = reqSendHighlight;
        this.transformService = null;
        this.transformClient = null;
    }

    private synchronized static void setupAPIUrl() {
        if(SERVICE_URL != null) {
            return;
        }
        SERVICE_URL = PropertyUtil.get("oskari.domain", null);
        final String[] path = PropertyUtil.get("oskari.ajax.url.prefix", "/?").split("\\?");
        SERVICE_URL_PATH = path[0];
        SERVICE_URL_QUERYSTRING = "?";
        if(path.length == 2) {
            SERVICE_URL_QUERYSTRING = SERVICE_URL_QUERYSTRING + path[1];
        }
        if(!SERVICE_URL_QUERYSTRING.endsWith("?") && !SERVICE_URL_QUERYSTRING.endsWith("&")) {
            SERVICE_URL_QUERYSTRING = SERVICE_URL_QUERYSTRING + "&";
        }
        SERVICE_URL_QUERYSTRING = SERVICE_URL_QUERYSTRING + PARAM_ROUTE + "=";
        // querystring should now be "?action_route=" or something like "?qwer=ty&action_route="
        log.debug("Constructed path:", SERVICE_URL_PATH, "- and querystr: ", SERVICE_URL_QUERYSTRING, "from:", path);
    }

    /**
     * Gets service path for local API
     * 
     * Path for Layer configuration and permissions request
     * 
     * @param sessionId
     * @return URL
     */
    public static String getAPIUrl(String sessionId) {
        setupAPIUrl();
        String session = "";
        if (SERVICE_URL_SESSION_PARAM != null) {
            // TODO: move into cookie same as route
            // construct url session token if specified
            session = ";" + SERVICE_URL_SESSION_PARAM + "=" + sessionId;
        }
        return SERVICE_URL + SERVICE_URL_PATH + session + SERVICE_URL_QUERYSTRING;
    }

    /**
     * Gets layer permissions (uses cache)
     *
     * @param layerId
     * @param sessionId
     * @param route
     * @return <code>true</code> if rights to use the layer; <code>false</code>
     *         otherwise.
     */
    public static boolean getPermissions(String layerId, String sessionId, String route) {
        String json = WFSLayerPermissionsStore.getCache(sessionId);
        boolean fromCache = (json != null);
        if(!fromCache) {
            log.warn(getAPIUrl(sessionId) + PERMISSIONS_API);
            String cookies = null;
            if(route != null && !route.equals("")) {
                cookies = ROUTE_COOKIE_NAME + route;
            }
            json = HttpHelper.getRequest(getAPIUrl(sessionId) + PERMISSIONS_API, cookies);
            if(json == null)
                return false;
        }
        try {
            WFSLayerPermissionsStore permissions = WFSLayerPermissionsStore.setJSON(json);
            return permissions.isPermission(layerId);
        } catch (IOException e) {
            log.error(e, "JSON parsing failed for WFSLayerPermissionsStore \n" + json);
        }

        return false;
    }

    /**
     * Gets layer configuration (uses cache)
     *
     * @param layerId
     * @param sessionId
     * @param route
     * @return layer
     */
    public static WFSLayerStore getLayerConfiguration(String layerId, String sessionId, String route) {
        String json = WFSLayerStore.getCache(layerId);
        boolean fromCache = (json != null);
        if(!fromCache) {
            final String apiUrl = getAPIUrl(sessionId) + LAYER_CONFIGURATION_API + layerId;
            log.debug("Fetching layer data from", apiUrl);
            String cookies = null;
            if(route != null && !route.equals("")) {
                cookies = ROUTE_COOKIE_NAME + route;
            }
            // NOTE: result is not handled
            String result = HttpHelper.getRequest(apiUrl, cookies);
            json = WFSLayerStore.getCache(layerId);
            if(json == null) {
                log.error("Couldn't find JSON for WFSLayerStore with id:", layerId, " - API url:", apiUrl);
                return null;
            }
        }
        try {
            return WFSLayerStore.setJSON(json);
        } catch (Exception e) {
            log.error(e, "JSON parsing failed for WFSLayerStore \n" + json);
        }

        return null;
    }




    /**
     * Releases all when removed
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    /**
     * Unique key definition
     */
    public String getKey() {
        return this.getClass().getSimpleName() + "_" + this.session.getClient()
                + "_" + this.layerId + "_" + this.type;
    }

    /**
     * Process of the job
     *
     * Worker calls this when starts the job.
     *
     */
    public abstract String run() ;

    /**
     * Wrapper for normal type job's handlers
     */
    protected boolean normalHandlers(List<Double> bounds, boolean first) {
        if(!this.requestHandler(bounds)) {
            log.debug("Cancelled by request handler");
            return false;
        }
        if(first) {
            propertiesHandler();
            if(!goNext()) {
                return false;
            }
        }
        if(!goNext()) {
            return false;
        }
        this.featuresHandler();
        if(!goNext()) {
            return false;
        }
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
    protected abstract boolean requestHandler(List<Double> bounds) ;

    /**
     * Parses features properties and sends to appropriate channels
     */
    protected abstract void propertiesHandler() ;

    /**
     * Parses features values
     */
    protected abstract void featuresHandler() ;

    /**
     * Send image parsing error
     */
    protected void imageParsingFailed() {
        imageParsingFailed(ResultProcessor.ERROR_WFS_IMAGE_PARSING);
    }
    /**
     * Send image parsing error
     */
    protected void imageParsingFailed(final String message) {
        log.debug("Image parsing failed");
        Map<String, Object> output = new HashMap<String, Object>();
        output.put(OUTPUT_LAYER_ID, this.layerId);
        output.put(OUTPUT_ONCE, true);
        output.put(OUTPUT_MESSAGE, message);
        this.service.addResults(session.getClient(), TransportService.CHANNEL_ERROR,
                output);
    }

    /**
     * Checks if enough information for running the task type
     *
     * @return <code>true</code> if enough information for type;
     *         <code>false</code> otherwise.
     */
    protected boolean validateType() {
        if(this.type == JobType.HIGHLIGHT) {
            if(this.sessionLayer.getHighlightedFeatureIds() != null &&
                    this.sessionLayer.getHighlightedFeatureIds().size() > 0) {
                return true;
            }
        } else if(this.type == JobType.MAP_CLICK) {
            if(session.getMapClick() != null) {
                return true;
            }
        } else if(this.type == JobType.GEOJSON) {
            if(session.getFilter() != null) {
                return true;
            }
        }
        else if(this.type == JobType.PROPERTY_FILTER) {
            if(session.getPropertyFilter() != null) {
                return true;
            }
        } else if(this.type == JobType.NORMAL) {
            return true;
        }
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
        if(!this.reqSendFeatures && this.sendFeatures)
            this.sendFeatures = false;
        if(!this.reqSendImage && this.sendImage)
            this.sendImage = false;
        if(!this.reqSendHighlight && this.sendHighlight)
            this.sendHighlight = false;

        log.debug("send - features:", this.sendFeatures, "image:", this.sendImage, "highlight:", this.sendHighlight);
    }

    /**
     * Checks if the map scale is valid
     *
     * @return <code>true</code> if map scale is valid;
     *         <code>false</code> otherwise.
     */
    protected boolean validateMapScales() {
        double scale = this.session.getMapScales().get((int)this.session.getLocation().getZoom());
        log.debug("Scale in:", layer.getSRSName(), scale, "[", layer.getMaxScale(), ",", layer.getMinScale(), "]");
        // if scale value is -1 -> ignore scale check on that boundary
        boolean minScaleOk = (layer.getMinScale() == -1);
        boolean maxScaleOk = (layer.getMaxScale() == -1);
        // min == biggest value
        if(!minScaleOk) {
            double minScaleInMapSrs = units.getScaleInSrs(layer.getMinScale(), layer.getSRSName(), session.getLocation().getSrs());
            log.debug("Scale in:", session.getLocation().getSrs(), scale, "[min:", minScaleInMapSrs, "]");
            minScaleOk = (minScaleInMapSrs >= scale);
        }
        if(!maxScaleOk) {
            double maxScaleInMapSrs = units.getScaleInSrs(layer.getMaxScale(), layer.getSRSName(), session.getLocation().getSrs());
            log.debug("Scale in:", session.getLocation().getSrs(), scale, "[max:", maxScaleInMapSrs, "]");
            maxScaleOk = maxScaleInMapSrs <= scale;
        }

        return minScaleOk && maxScaleOk;
    }
    /**
     * Creates image url
     * 
     * @param style
     * @param bbox
     */
    protected String createImageURL(final String style, Double[] bbox) {
        return "/image" +
                "?" + OUTPUT_LAYER_ID + "=" + this.layerId +
                "&" + OUTPUT_STYLE + "=" + style +
                "&" + OUTPUT_IMAGE_SRS + "=" + this.session.getLocation().getSrs() +
                "&" + OUTPUT_IMAGE_BBOX + "=" + bbox[0] +
                "," + bbox[1] +
                "," + bbox[2] +
                "," + bbox[3] +
                "&" + OUTPUT_IMAGE_ZOOM + "=" + this.session.getLocation().getZoom();
    }

    /**
     * Sends properties (fields and locales)
     *
     * @param fields
     * @param locales
     */
    protected void sendWFSProperties(List<String> fields, List<String> locales) {
        if(fields == null || fields.size() == 0) {
            log.warn("Failed to send properties");
            return;
        }

        fields.add(0, "__fid");
        fields.add("__centerX");
        fields.add("__centerY");

        if(locales != null && !locales.isEmpty()) {
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

        this.service.addResults(this.session.getClient(), ResultProcessor.CHANNEL_PROPERTIES, output);
    }

    /**
     * Sends one feature
     *
     * @param values
     */
    protected void sendWFSFeature(List<Object> values) {
        if(values == null || values.size() == 0) {
            log.warn("Failed to send feature");
            return;
        }
        Map<String, Object> output = new HashMap<String, Object>();
        output.put(OUTPUT_LAYER_ID, this.layerId);
        output.put(OUTPUT_FEATURE, values);

        this.service.addResults(this.session.getClient(), ResultProcessor.CHANNEL_FEATURE, output);
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

        log.debug("Sending", features.size(), "features");
        this.service.addResults(this.session.getClient(), channel, output);
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
        if(bufferedImage == null) {
            log.warn("Failed to send image");
            return;
        }

        Map<String, Object> output = new HashMap<String, Object>();
        output.put(OUTPUT_LAYER_ID, this.layerId);

        Location location = this.session.getLocation();

        Tile tileSize = null;
        if(isTiled) {
            tileSize = this.session.getTileSize();
        } else {
            tileSize = this.session.getMapSize();
        }

        output.put(OUTPUT_IMAGE_SRS, location.getSrs());
        output.put(OUTPUT_IMAGE_BBOX, bbox);
        output.put(OUTPUT_IMAGE_ZOOM, location.getZoom());
        output.put(OUTPUT_IMAGE_TYPE, this.type.toString()); // "normal" | "highlight"
        output.put(OUTPUT_KEEP_PREVIOUS, this.session.isKeepPrevious());
        output.put(OUTPUT_BOUNDARY_TILE, isboundaryTile);
        output.put(OUTPUT_IMAGE_WIDTH, tileSize.getWidth());
        output.put(OUTPUT_IMAGE_HEIGHT, tileSize.getHeight());
        output.put(OUTPUT_IMAGE_URL, url);

        byte[] byteImage = WFSImage.imageToBytes(bufferedImage);
        String base64Image = WFSImage.bytesToBase64(byteImage);
        int base64Size = (base64Image.length()*2)/1024;

        // IE6 & IE7 doesn't support base64, max size in base64 for IE8 is 32KB
        if(!(this.session.getBrowser().equals(BROWSER_MSIE) && this.session.getBrowserVersion() < 8 ||
                this.session.getBrowser().equals(BROWSER_MSIE) && this.session.getBrowserVersion() == 8 &&
                        base64Size >= 32)) {
            output.put(OUTPUT_IMAGE_DATA, base64Image);
        }

        this.service.addResults(this.session.getClient(), ResultProcessor.CHANNEL_IMAGE, output);
    }

    /**
     * Sends list of feature geometries
     *
     * @param geometries
     * @param channel
     */
    protected void sendWFSFeatureGeometries(List<List<Object>> geometries, String channel) {
        if(geometries == null || geometries.size() == 0) {
            log.warn("Failed to send feature geometries");
            return;
        }
        Map<String, Object> output = new HashMap<String, Object>();
        output.put(OUTPUT_LAYER_ID, this.layerId);
        output.put(OUTPUT_GEOMETRIES, geometries);
        output.put(OUTPUT_KEEP_PREVIOUS, this.session.isKeepPrevious());

        log.debug("Sending", geometries.size(), "geometries");
        this.service.addResults(this.session.getClient(), channel, output);
    }
    public abstract RequestResponse request(JobType type, WFSLayerStore layer,
                                            SessionStore session, List<Double> bounds,
                                            MathTransform transformService);


    public abstract FeatureCollection<SimpleFeatureType, SimpleFeature> response(
            WFSLayerStore layer, RequestResponse response);

    /**
     * Gets image from cache
     *
     * @param bbox
     */
    protected BufferedImage getImageCache(Double[] bbox) {
        return WFSImage.getCache(
                this.layerId,
                this.session.getLayers().get(this.layerId).getStyleName(),
                this.session.getLocation().getSrs(),
                bbox,
                this.session.getLocation().getZoom()
        );
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

    protected WFSImage createResponseImage() {
        return new WFSImage(this.layer, this.session.getClient(), this.session
                .getLayers().get(this.layerId).getStyleName(), null);
    }

    protected WFSImage createHighlightImage() {
        return new WFSImage(this.layer, this.session.getClient(), this.session
                .getLayers().get(this.layerId).getStyleName(),
                JobType.HIGHLIGHT.toString());
    }

    public void notifyError() {
        notifyError(null);
    }

    public void notifyError(String error) {
        if (error == null) {
            error = "Something went wrong";
        }
        log.error("On Error", error);
        Map<String, Object> output = new HashMap<String, Object>();
        output.put(OUTPUT_LAYER_ID, this.layerId);
        output.put(OUTPUT_MESSAGE, error);
        this.service.addResults(session.getClient(), TransportService.CHANNEL_ERROR,
                output);
    }

    // TODO: check if this actually works!
    public String getFallback() {
        notifyError();
        return "success";
    }
}