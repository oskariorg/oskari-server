package fi.nls.oskari.work;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.*;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.transport.TransportJobException;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wfs.WFSExceptionHelper;
import fi.nls.oskari.wfs.WFSImage;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.worker.AbstractJob;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.operation.MathTransform;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Job for WFS Map Layer
 */
public abstract class OWSMapLayerJob extends AbstractJob<String> {

    public static final String STATUS_CANCELED = "canceled";

    protected static final Logger log = LogFactory
            .getLogger(OWSMapLayerJob.class);

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
    public static final String OUTPUT_KEY = "key";
    public static final String OUTPUT_LEVEL = "level";
    public static final String OUTPUT_CAUSE = "cause";
    public static final String OUTPUT_ZOOMSCALE = "zoomscale";
    public static final String OUTPUT_MINSCALE = "minscale";
    public static final String OUTPUT_MAXSCALE = "maxscale";
    public static final String OUTPUT_JOBTYPE = "type";
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

    public static final String DEFAULT_FE_SLD_STYLE_PATH ="/fi/nls/oskari/fe/output/style/default/default_fe_sld_";

    // process information
    protected ResultProcessor service;
    protected SessionStore session;
    protected Layer sessionLayer;
    protected WFSLayerPermissionsStore permissions;
    protected String layerId;
    protected WFSLayerStore layer;
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

    public static final int FE_READ_TIMEOUT_MS = PropertyUtil.getOptional("oskari.wfs.read.timeout", 30000);



    protected static final List<List<Object>> EMPTY_LIST = new ArrayList();


    /**
     * Creates a new runnable job with own Jedis instance
     * 
     * Parameters define client's service (communication channel), session and
     * layer's id. Sends all resources that the layer configuration allows.
     * 
     * @param service
     * @param store
     * @param layer
     */
    public OWSMapLayerJob(ResultProcessor service, JobType type,
            SessionStore store, WFSLayerStore layer) {
        this(service, type, store, layer, true, true, true);
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
     * @param reqSendFeatures
     * @param reqSendImage
     * @param reqSendHighlight
     */
    public OWSMapLayerJob(ResultProcessor service, JobType type,
            SessionStore store, WFSLayerStore layer, boolean reqSendFeatures,
            boolean reqSendImage, boolean reqSendHighlight) {
        this.service = service;
        this.type = type;
        this.session = store;
        this.layer = layer;
        this.layerId = layer.getLayerId();
        this.sessionLayer = this.session.getLayers().get(this.layerId);
        this.permissions = null;
        this.layerPermission = false;
        this.reqSendFeatures = reqSendFeatures;
        this.reqSendImage = reqSendImage;
        this.reqSendHighlight = reqSendHighlight;
        this.transformService = null;
        this.transformClient = null;
    }

    public String getSessionId() {
        return this.session.getSession();
    }

    public String getRoute() {
        return this.session.getRoute();
    }

    public JobType getType() {
        return type;
    }

    public String getLayerId() {
        return layerId;
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
    public String run() {

        log.debug(PROCESS_STARTED + " " + getKey());
        setResourceSending();

        // if different SRS, create transforms for geometries
        // Use layer.getSRSName() for srsName, if transform must be supported
        String srsName = session.getLocation().getSrs();
        if(!this.session.getLocation().getSrs().equals(srsName)) {
            this.transformService = this.session.getLocation().getTransformForService(this.layer.getCrs(), true);
            this.transformClient = this.session.getLocation().getTransformForClient(this.layer.getCrs(), true);
        }

        // check that the job isn't canceled
        if (!goNext()) {
            log.debug("[fe] Cancelled");
            return STATUS_CANCELED;
        }
        // run job specific tasks
        boolean completed = runRequestedJob();
        if(!completed) {
            log.debug("[fe] Cancelled");
            return STATUS_CANCELED;
        }
        log.debug("[fe] " + PROCESS_ENDED + " " + getKey());
        return "success";
    }

    private boolean runRequestedJob() {
        // tiles for grid
        if (this.type == JobType.NORMAL) {
            return runNormalJob();
        }
        else if (this.type == JobType.HIGHLIGHT) {
            return runHighlightJob();
        }
        else if (this.type == JobType.MAP_CLICK) {
            return runMapClickJob();
        }
        else if (this.type == JobType.GEOJSON) {
            return runGeoJSONJob();
        }
        else if (this.type == JobType.PROPERTY_FILTER) {
            return runPropertyFilterJob();
        }
        return runUnknownJob();
    }

    public boolean runNormalJob() {
        // make single request
        if (!this.layer.isTileRequest()) {
            log.debug("single request");
            if (!this.normalHandlers(null, true)) {
                log.debug("!normalHandlers leaving");
                return false;
            } else {
                log.debug("single request - continue");
            }
        } else {
            log.debug("MAKING TILED REQUESTS");
        }
        log.debug("normal tile images handling");

        // init enlarged envelope
        List<List<Double>> grid = this.session.getGrid().getBounds();
        if (grid.size() > 0) {
            this.session.getLocation().setEnlargedEnvelope(grid.get(0));
        }

        boolean first = true;
        int index = 0;
        for(List<Double> bounds : grid) {
            if (!goNext()) {
                return false;
            }

            log.debug("Tile bounds:", bounds);

            // make a request per tile
            if(this.layer.isTileRequest()) {
                if(!this.normalHandlers(bounds, first)) {
                    continue;
                }
            }

            if(!goNext()) {
                return false;
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

            if (isThisTileNeeded) {
                Double[] bbox = bounds.toArray(new Double[4]);

                // get from cache
                BufferedImage bufferedImage = getImageCache(bbox);
                boolean isboundaryTile = this.session.getGrid().isBoundsOnBoundary2(this.session.getLocation(), bbox);

                if(bufferedImage == null) {
                    if(this.image == null) {
                        this.image = createResponseImage();
                        // Style check
                        if(this.image.getStyle() == null) {
                            Map<String, Object> output = this.createCommonWarningResponse(
                                    "SDL style parsing failed for the layer (custon or default)",
                                    WFSExceptionHelper.WARNING_SLDSTYLE_PARSING_FAILED);
                            this.sendCommonErrorResponse(output, true);
                        }
                    }
                    bufferedImage = this.image.draw(this.session.getTileSize(),
                            this.session.getLocation(),
                            bounds,
                            this.features);
                    if(bufferedImage == null) {
                        // Break tile loop, if one tile fails
                        throw new TransportJobException("Tile image parsing failed for features",
                                WFSExceptionHelper.ERROR_WFS_IMAGE_PARSING_FAILED);
                    }

                    // setup cachekey
                    String cacheStyleName = this.session.getLayers().get(this.layerId).getStyleName();
                    if (cacheStyleName.startsWith(WFSImage.PREFIX_CUSTOM_STYLE)) {
                        cacheStyleName += "_" + this.session.getSession();
                    }

                    // save to cache
                    setImageCache(bufferedImage, cacheStyleName, bbox, !isboundaryTile);
                }

                String url = createImageURL(this.session.getLayers().get(this.layerId).getStyleName(), bbox);
                this.sendWFSImage(url, bufferedImage, bbox, true, isboundaryTile);
            } else {
                log.debug("Tile not needed?", bounds);
            }

            if (first) {
                first = false;
                // keep the next tiles
                this.session.setKeepPrevious(true);
            }
            index++;
        }
        return true;
    }

    public boolean runHighlightJob() {
        if(!this.sendHighlight) {
            // highlight job with a flag for not sending images, what is going on in here?
            return true;
        }
        if(!this.requestHandler(null)) {
            return false;
        }
        this.featuresHandler();
        if (!goNext()) {
            return false;
        }
        // Send geometries, if requested as well
        if(this.session.isGeomRequest()){
            this.sendWFSFeatureGeometries(this.geomValuesList, ResultProcessor.CHANNEL_FEATURE_GEOMETRIES);
        }
        log.debug("highlight image handling", this.features.size());
        // IMAGE HANDLING
        log.debug("sending");
        Location location = this.session.getLocation();
        if(this.image == null) {
            this.image = new WFSImage(this.layer,
                    this.session.getClient(),
                    this.session.getLayers().get(this.layerId).getStyleName(),
                    JobType.HIGHLIGHT.toString());
        }
        // Style check
        if(this.image.getStyle() == null) {
            Map<String, Object> output = this.createCommonWarningResponse(
                    "Highlight SDL style parsing failed (custon or default)",
                    WFSExceptionHelper.WARNING_SLDSTYLE_PARSING_FAILED);
            this.sendCommonErrorResponse(output, true);
        }
        BufferedImage bufferedImage = this.image.draw(this.session.getMapSize(),
                location,
                this.features);
        if(bufferedImage == null) {
            throw new TransportJobException("Image parsing failed for feature highlight",
                    WFSExceptionHelper.ERROR_WFS_IMAGE_PARSING_FAILED);
        }

        Double[] bbox = location.getBboxArray();

        // cache (non-persistant)
        setImageCache(bufferedImage, JobType.HIGHLIGHT.toString() + "_" + this.session.getSession(), bbox, false);

        String url = createImageURL(JobType.HIGHLIGHT.toString(), bbox);
        this.sendWFSImage(url, bufferedImage, bbox, false, false);
        return true;
    }

    public boolean runMapClickJob() {

        if (!this.requestHandler(null)) {
            // success, just no hits
            this.sendWFSFeatures(EMPTY_LIST, ResultProcessor.CHANNEL_MAP_CLICK);
            return true;
        }
        this.featuresHandler();
        if (!goNext()) {
            return false;
        }

        // features
        if (this.sendFeatures) {
            log.debug("Feature values list", this.featureValuesList);
            this.sendWFSFeatures(this.featureValuesList,
                    ResultProcessor.CHANNEL_MAP_CLICK);
        } else {
            log.debug("No feature data!");
            this.sendWFSFeatures(EMPTY_LIST, ResultProcessor.CHANNEL_MAP_CLICK);
        }
        
        // geometries
        if(this.session.isGeomRequest()){
            this.sendWFSFeatureGeometries(this.geomValuesList, ResultProcessor.CHANNEL_FEATURE_GEOMETRIES);
        }
        return true;
    }

    public boolean runGeoJSONJob() {
        if (!this.requestHandler(null)) {
            return false;
        }
        this.featuresHandler();
        if (!goNext()) {
            return false;
        }
        if (this.sendFeatures) {
            this.sendWFSFeatures(this.featureValuesList,
                    ResultProcessor.CHANNEL_FILTER);
        }
        return true;
    }

    public boolean runPropertyFilterJob() {
        // should be same functionality
        return runGeoJSONJob();
    }

    public boolean runUnknownJob() {
        log.debug("Type is not handled " + this.type);
        return true;
    }


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
        this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_ERROR,
                output);
    }

    /**
     * Sets which resources will be sent (features, image)
     */
    protected void setResourceSending() {
        // layer configuration is the default
        this.sendFeatures = layer.isGetFeatureInfo();
        this.sendImage = layer.isGetMapTiles() && sessionLayer.hasVisibleStyle();
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
        return JobHelper.isRequestScalesInRange(
                this.session.getMapScales(),
                (int) this.session.getLocation().getZoom(),
                layer,
                session.getLocation().getSrs());
    }

    /**
     * Checks if enough information for running the task type
     *
     * @return <code>true</code> if enough information for type;
     *         <code>false</code> otherwise.
     */
    public boolean hasValidParams() {
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

        if(!fields.contains("__fid")) fields.add(0, "__fid");
        if(!fields.contains("__centerX"))fields.add("__centerX");
        if(!fields.contains("__centerY"))fields.add("__centerY");

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
        if (channel.equals(ResultProcessor.CHANNEL_MAP_CLICK) || channel.equals(ResultProcessor.CHANNEL_FILTER)) {
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

        Tile tileSize = this.session.getMapSize();
        if(isTiled) {
            tileSize = this.session.getTileSize();
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
        output.put(OUTPUT_IMAGE_DATA, base64Image);

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

    public void sendCommonErrorResponse(final String message, final boolean once) {
        Map<String, Object> output = createCommonResponse(message);
        if(once) {
            output.put(OUTPUT_ONCE, once);
        }
        this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_ERROR, output);
    }
    public void sendCommonErrorResponse(final Map<String, Object> output, final boolean once) {
        if(once) {
            output.put(OUTPUT_ONCE, once);
        }
        this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_ERROR, output);
    }
    public Map<String, Object> createCommonResponse(final String message, final String key) {
        Map<String, Object> output = createCommonResponse();
        output.put(OUTPUT_MESSAGE, message);
        output.put(OUTPUT_KEY, key);
        return output;
    }
    public Map<String, Object> createCommonWarningResponse(final String message, final String key) {
        Map<String, Object> output = createCommonResponse();
        output.put(OUTPUT_MESSAGE, message);
        output.put(OUTPUT_KEY, key);
        output.put(OUTPUT_LEVEL, "warning");
        return output;
    }
    public Map<String, Object> createCommonResponse(final String message) {
        Map<String, Object> output = createCommonResponse();
        output.put(OUTPUT_MESSAGE, message);
        return output;
    }

    public Map<String, Object> createCommonResponse() {
        Map<String, Object> output = new HashMap<String, Object>();
        output.put(OUTPUT_LAYER_ID, this.layerId);
        output.put(OUTPUT_JOBTYPE, this.type.toString());
        return output;
    }


    public void notifyError() {
        notifyError("Something went wrong");
    }

    public void notifyError(String error) {
        if (error == null) {
            error = "Layer could not be loaded - reason unknown";
        }
        log.error("On Error - layer:", layerId, "type:", type, "msg:", error);
        Map<String, Object> output = createCommonResponse(error, WFSExceptionHelper.ERROR_COMMON_JOB_FAILURE);
        this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_ERROR, output);
    }

    public void notifyError(Exception e) {
        if (e == null) {
            notifyError("Something went wrong");
            return;
        }
        Map<String, Object> output = createCommonResponse(e.getMessage());
        output.put(OUTPUT_ONCE, false);

        if (e instanceof TransportJobException) {
            output.put(OUTPUT_KEY, ((TransportJobException) e).getMessageKey());
            output.put(OUTPUT_LEVEL, ((TransportJobException) e).getLevel());
        } else if (e instanceof ServiceRuntimeException) {
            output.put(OUTPUT_KEY, ((ServiceRuntimeException) e).getMessageKey());
            output.put(OUTPUT_LEVEL, ((ServiceRuntimeException) e).getLevel());
        } else {
            output.put(OUTPUT_KEY, WFSExceptionHelper.ERROR_COMMON_JOB_FAILURE);
            output.put(OUTPUT_LEVEL, WFSExceptionHelper.ERROR_LEVEL);
        }
        if (e.getCause() != null) {
            output.put(OUTPUT_CAUSE, e.getCause().getMessage());
        }
        this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_ERROR, output);
    }


    public void notifyStart() {
        log.info("On start - layer:", layerId, "type:", type);
        this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_STATUS, createCommonResponse("started"));
    }

    public void notifyCompleted(final boolean success, final boolean success_nop) {
        log.info("Completed - layer:", layerId, "type:", type, "success:", success, " Success no operation:", success_nop);
        Map<String, Object> output = createCommonResponse("completed");
        output.put("success", success);
        output.put("success_nop", success_nop);
        this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_STATUS, output);
    }


}