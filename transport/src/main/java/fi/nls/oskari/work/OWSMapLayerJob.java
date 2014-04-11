package fi.nls.oskari.work;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.operation.MathTransform;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.Layer;
import fi.nls.oskari.pojo.Location;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.pojo.Tile;
import fi.nls.oskari.pojo.Units;
import fi.nls.oskari.pojo.WFSLayerPermissionsStore;
import fi.nls.oskari.pojo.WFSLayerStore;
import fi.nls.oskari.transport.TransportService;
import fi.nls.oskari.utils.HttpHelper;
import fi.nls.oskari.wfs.WFSImage;

/**
 * Job for WFS Map Layer
 */
public abstract class OWSMapLayerJob extends Job {

    protected static final Logger log = LogFactory
            .getLogger(OWSMapLayerJob.class);

    public static enum Type {
        NORMAL("normal"), HIGHLIGHT("highlight"), MAP_CLICK("mapClick"), GEOJSON(
                "geoJSON");

        protected final String name;

        private Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final String OUTPUT_LAYER_ID = "layerId";
    public static final String OUTPUT_ONCE = "once";
    public static final String OUTPUT_MESSAGE = "message";
    public static final String OUTPUT_FEATURES = "features";
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
    protected TransportService service;
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
    protected Type type;
    protected FeatureCollection<SimpleFeatureType, SimpleFeature> features;
    protected List<String> processedFIDs = new ArrayList<String>();
    protected List<List<Object>> featureValuesList;

    protected WFSImage image = null;
    protected Units units = new Units();

    // API
    public static final String PERMISSIONS_API = "GetLayerIds";
    public static final String LAYER_CONFIGURATION_API = "GetWFSLayerConfiguration&id=";

    // COOKIE
    public static final String ROUTE_COOKIE_NAME = "ROUTEID=";

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
    public OWSMapLayerJob(TransportService service, Type type,
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
    public OWSMapLayerJob(TransportService service, Type type,
            SessionStore store, String layerId, boolean reqSendFeatures,
            boolean reqSendImage, boolean reqSendHighlight) {
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

    /**
     * Gets service path for local API
     * 
     * Path for Layer configuration and permissions request
     * 
     * @param sessionId
     * @return URL
     */
    public static String getAPIUrl(String sessionId) {
        String session = "";
        if (TransportService.SERVICE_URL_SESSION_PARAM != null) {
            session = ";" + TransportService.SERVICE_URL_SESSION_PARAM + "="
                    + sessionId;
        }
        return TransportService.SERVICE_URL + TransportService.SERVICE_URL_PATH
                + session + TransportService.SERVICE_URL_LIFERAY_PATH;
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
            log.warn(getAPIUrl(sessionId) + LAYER_CONFIGURATION_API + layerId);
            String cookies = null;
            if(route != null && !route.equals("")) {
                cookies = ROUTE_COOKIE_NAME + route;
            }
            // NOTE: result is not handled
            String result = HttpHelper.getRequest(getAPIUrl(sessionId) + LAYER_CONFIGURATION_API + layerId, cookies);
            json = WFSLayerStore.getCache(layerId);
            if(json == null)
                return null;
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
    @Override
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
    @Override
    public abstract void run() ;

    /**
     * Wrapper for normal type job's handlers
     */
    protected abstract boolean normalHandlers(List<Double> bounds, boolean first);

    public interface RequestResponse {
        
        public void flush() throws IOException;

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
        log.debug("Image parsing failed");
        Map<String, Object> output = new HashMap<String, Object>();
        output.put(OUTPUT_LAYER_ID, this.layerId);
        output.put(OUTPUT_ONCE, true);
        output.put(OUTPUT_MESSAGE, "wfs_image_parsing_failed");
        this.service.send(session.getClient(), TransportService.CHANNEL_ERROR,
                output);
    }

    /**
     * Checks if enough information for running the task type
     * 
     * @return <code>true</code> if enough information for type;
     *         <code>false</code> otherwise.
     */
    protected abstract boolean validateType() ;

    /**
     * Sets which resources will be sent (features, image)
     */
    protected abstract void setResourceSending() ;

    /**
     * Checks if the map scale is valid
     * 
     * @return <code>true</code> if map scale is valid; <code>false</code>
     *         otherwise.
     */
    protected abstract boolean validateMapScales() ;
    /**
     * Creates image url
     * 
     * @param style
     * @param bbox
     */
    protected String createImageURL(final String style, Double[] bbox) {
        return "/image" + "?" + OUTPUT_LAYER_ID + "=" + this.layerId + "&"
                + OUTPUT_STYLE + "=" + style + "&" + OUTPUT_IMAGE_SRS + "="
                + this.session.getLocation().getSrs() + "&" + OUTPUT_IMAGE_BBOX
                + "=" + bbox[0] + "," + bbox[1] + "," + bbox[2] + "," + bbox[3]
                + "&" + OUTPUT_IMAGE_ZOOM + "="
                + this.session.getLocation().getZoom();
    }

    /**
     * Sends properties (fields and locales)
     * 
     * @param fields
     * @param locales
     */
    protected abstract void sendWFSProperties(List<String> fields, List<String> locales) ;
    /**
     * Sends one feature
     * 
     * @param values
     */
    protected abstract void sendWFSFeature(List<Object> values) ;
    /**
     * Sends list of features
     * 
     * @param features
     * @param channel
     */
    protected abstract void sendWFSFeatures(List<List<Object>> features, String channel) ;
    /**
     * Sends image as an URL to IE 8 & 9, base64 data for others
     * 
     * @param url
     * @param bufferedImage
     * @param bbox
     * @param isTiled
     */
    protected abstract void sendWFSImage(String url, BufferedImage bufferedImage,
            Double[] bbox, boolean isTiled, boolean isboundaryTile) ;

    public abstract RequestResponse request(Type type, WFSLayerStore layer,
            SessionStore session, List<Double> bounds,
            MathTransform transformService);

    public abstract FeatureCollection<SimpleFeatureType, SimpleFeature> response(
            WFSLayerStore layer, RequestResponse response);

    /**
     * Gets image from cache
     * 
     * @param bbox
     */
    protected abstract BufferedImage getImageCache(Double[] bbox);

    /**
     * Sets image to cache
     * 
     * @param bufferedImage
     * @param style
     * @param bbox
     * @param persistent
     */
    protected abstract void setImageCache(BufferedImage bufferedImage,
            final String style, Double[] bbox, boolean persistent);

    protected WFSImage createResponseImage() {
        return new WFSImage(this.layer, this.session.getClient(), this.session
                .getLayers().get(this.layerId).getStyleName(), null);
    }

    protected WFSImage createHighlightImage() {
        return new WFSImage(this.layer, this.session.getClient(), this.session
                .getLayers().get(this.layerId).getStyleName(),
                Type.HIGHLIGHT.toString());
    }
}