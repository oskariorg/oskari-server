package fi.nls.oskari.work.arcgis;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import fi.nls.oskari.arcgis.ArcGisCommunicator;
import fi.nls.oskari.arcgis.ArcGisFilter;
import fi.nls.oskari.arcgis.ArcGisImage;
import fi.nls.oskari.arcgis.ArcGisTokenService;
import fi.nls.oskari.arcgis.pojo.ArcGisFeature;
import fi.nls.oskari.arcgis.pojo.ArcGisLayerStore;
import fi.nls.oskari.arcgis.pojo.ArcGisProperty;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.Location;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.wfs.WFSParser;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.wfs.util.HttpHelper;
import fi.nls.oskari.work.*;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.operation.MathTransform;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Job for Arcgis REST Map Layer
 */
public class ArcGisMapLayerJob extends OWSMapLayerJob {

    private static final Logger log = LogFactory.getLogger(ArcGisMapLayerJob.class);
    private static final List<List<Object>> EMPTY_LIST = new ArrayList();

    public static final String OUTPUT_LAYER_ID = "layerId";
    public static final String OUTPUT_ONCE = "once";
    public static final String OUTPUT_MESSAGE = "message";
    public static final String OUTPUT_FEATURES = "features";
    public static final String OUTPUT_FEATURE = "feature";
    public static final String OUTPUT_KEEP_PREVIOUS = "keepPrevious";
    public static final String PROCESS_ENDED = "Ended";

    public static String ERROR_REST_IMAGE_PARSING = "arcgis_image_parsing_failed";
    public static String ERROR_REST_CONFIGURATION_FAILED = "arcgis_configuring_layer_failed";
    public static String ERROR_REST_REQUEST_FAILED = "arcgis_request_failed";

    // process information
    ResultProcessor service;
    private ArcGisLayerStore arcGisLayer;
    private ArcGisLayerStore arcGisLayerScaled;
    private List<ArcGisLayerStore> arcGisLayers = new ArrayList<ArcGisLayerStore>();

    private MathTransform transformService;
    private MathTransform transformClient;
    private ArrayList<ArcGisFeature> features;
    private List<List<Object>> featureValuesList;
    private List<List<Object>> geomValuesList;
    private List<String> processedFIDs = new ArrayList<String>();
    private ArcGisImage image = null;
    //private Job parentJob = null;
    private String token = null;

    public ArcGisMapLayerJob(ResultProcessor service, JobType type, SessionStore store, WFSLayerStore layer) {
        super(service, type, store, layer, true, true, true);
    }

    public ArcGisMapLayerJob(ResultProcessor service, JobType type, SessionStore store, WFSLayerStore layer,
                             boolean reqSendFeatures, boolean reqSendImage, boolean reqSendHighlight) {
        super(service, type, store, layer, reqSendFeatures, reqSendImage, reqSendHighlight);

    }

    /**
     * Makes request
     *
     * @param type
     * @param layer
     * @param session
     * @param bounds
     * @return response
     */
    public static Reader sendQueryRequest(JobType type,
                                                  WFSLayerStore layer, ArcGisLayerStore arcGisLayer,
                                                  SessionStore session, List<Double> bounds,
                                                  String token) {
        Reader response = null;
        if(layer.getTemplateType() == null) { // default
            String payload = ArcGisCommunicator.createQueryRequestPayload(type, layer, session, bounds, token);
            String url = layer.getURL() + "/" + arcGisLayer.getId() + "/query?";
            log.debug("Request data\n", url, "\n", payload);
            //response = HttpHelper.postRequestReader(url, "", payload, layer.getUsername(), layer.getPassword());

            //TODO: POST
            response = HttpHelper.getRequestReader(url + payload, "", layer.getUsername(), layer.getPassword());
        } else {
            log.warn("Failed to make a request because of undefined layer type", layer.getTemplateType());
        }

        return response;
    }

    private Reader sendIdentifyRequest(WFSLayerStore layer, List<ArcGisLayerStore> layers,
                                               SessionStore session, List<Double> bounds,
                                               String token) {
        Reader response = null;

        String payload = ArcGisCommunicator.createIdentifyRequestPayload(layers, session, bounds, token);
        String url = layer.getURL() + "/identify?";
        log.debug("Request data\n", url, "\n", payload);
        response = HttpHelper.getRequestReader(url + payload, "", layer.getUsername(), layer.getPassword());

        return response;
    }

    public static ArcGisLayerStore getArcGisLayerConfiguration(String layerId, WFSLayerStore layer, String token) {
        log.info("Getting configuration for layer", layerId);
        ArcGisLayerStore result = null;
        String json = ArcGisLayerStore.getCache(layerId);
        boolean fromCache = (json != null);
        if (!fromCache) {
            log.info("Getting configuration from server for layer", layerId);
            result = loadArcGisLayerConfigurationFromServer(layerId, layer, token);

            if (result == null) {
                return null;
            } else {
                result.save();
                return result;
            }
        } else {
            try {
                return ArcGisLayerStore.setJSON(layerId, json);
            } catch (Exception e) {
                log.error(e, "JSON parsing failed for WFSLayerStore \n" + json);
            }
        }

        return null;
    }

    private static ArcGisLayerStore loadArcGisLayerConfigurationFromServer(String layerId, WFSLayerStore layer, String token) {
        ArcGisLayerStore result = null;
        String json = loadLayerConfig(layer.getURL(), layer.getLayerName(), token);

        try {
            result = ArcGisLayerStore.setJSON(layerId, json);
        } catch (Exception e) {
            log.error(e, "JSON parsing failed for WFSLayerStore \n" + json);
        }

        if (result != null && result.getType().equals("Group Layer")) {
            ArrayList<ArcGisLayerStore> subLayers = new ArrayList<ArcGisLayerStore>();
            for (String subLayerId : result.getSubLayerIds()) {
                String subLayerJson = loadLayerConfig(layer.getURL(), subLayerId, token);
                ArcGisLayerStore subLayerStore = null;
                try {
                    subLayerStore = ArcGisLayerStore.setJSON(layerId, subLayerJson);
                } catch (IOException e) {
                    log.error(e, "JSON parsing failed for Sub ArcGisLayerStore \n" + json);
                }
                subLayers.add(subLayerStore);
            }

            result.setSubLayers(subLayers);
        }

        return result;
    }

    private static String loadLayerConfig(String layerUrl, String layerId, String token) {
        String url = layerUrl + "/" + layerId + "?f=json&token=" + token;
        return HttpHelper.getRequest(url, null);
    }

    public void loadLayerConfig() {
        this.token = ArcGisTokenService.getInstance().getTokenForLayer(this.layer.getURL());
        this.arcGisLayer = getArcGisLayerConfiguration(this.layerId, this.layer, this.token);
    }
    /**
     * Process of the job
     * <p/>
     * Worker calls this when starts the job.
     */
    public String run() {
        final boolean thisMethodShouldBeRefactoredForNewJobModel = true;
        if(thisMethodShouldBeRefactoredForNewJobModel) {
            throw new RuntimeException("Needs refactoring");
        }

        loadLayerConfig();
        setResourceSending();

        log.debug("type:", this.type, "features:", this.sendFeatures, "image:", this.sendImage, "highlight:", this.sendHighlight);

        // if different SRS, create transforms for geometries
        if (!this.session.getLocation().getSrs().equals(this.layer.getSRSName())) {
            log.info("Different SRS. Map:", this.session.getLocation().getSrs(), "Layer:", this.layer.getSRSName());
            this.transformService = this.session.getLocation().getTransformForService(this.layer.getCrs(), true);
            this.transformClient = this.session.getLocation().getTransformForClient(this.layer.getCrs(), true);
        }

        String cacheStyleName = this.session.getLayers().get(this.layerId).getStyleName();
        if (cacheStyleName.startsWith(ArcGisImage.PREFIX_CUSTOM_STYLE)) {
            cacheStyleName += "_" + this.session.getSession();
        }

        // init enlarged envelope
        List<List<Double>> grid = this.session.getGrid().getBounds();
        if (grid.size() > 0) {
            this.session.getLocation().setEnlargedEnvelope(grid.get(0));
        }

        if (!goNext()) return STATUS_CANCELED;


        this.arcGisLayer = getArcGisLayerConfiguration(this.layerId, this.layer, this.token);
        if (this.arcGisLayer == null) {
            log.warn("Layer (" + this.layerId + ") arcgis configurations couldn't be fetched");
            Map<String, Object> output = new HashMap<String, Object>();
            output.put(OUTPUT_LAYER_ID, this.layerId);
            output.put(OUTPUT_ONCE, true);
            output.put(OUTPUT_MESSAGE, ERROR_REST_CONFIGURATION_FAILED);
            this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_ERROR, output);
            return "error";
        }

        this.arcGisLayers = getArcGisLayersDependingOnScale();
        this.arcGisLayerScaled = this.arcGisLayers.get(0);

        if (this.type == JobType.NORMAL) { // tiles for grid

            if (!this.layer.isTileRequest()) { // make single request
                if (!this.normalHandlers(null, true)) {
                    log.warn("Canceling single request", layer.getLayerId());
                    return STATUS_CANCELED;
                }
            }

            log.info("Normal images handling for layer", layer.getLayerId());

            boolean first = true;
            int index = 0;
            for (List<Double> bounds : grid) {
                if (this.layer.isTileRequest()) { // make a request per tile
                    if (!this.normalHandlers(bounds, first)) {
                        log.warn("Canceling tile request", layer.getLayerId());
                        return STATUS_CANCELED;
                    }
                }

                if (!goNext()) return STATUS_CANCELED;

                if (this.sendImage && this.sessionLayer.isTile(bounds)) { // check if needed tile
                    Double[] bbox = new Double[4];
                    for (int i = 0; i < bbox.length; i++) {
                        bbox[i] = bounds.get(i);
                    }

                    // get from cache
                    BufferedImage bufferedImage = getImageCache(bbox);
                    boolean fromCache = (bufferedImage != null);
                    boolean isboundaryTile = this.session.getGrid().isBoundsOnBoundary(index);

                    if (!fromCache) {
                        if (this.image == null) {
                            this.image = new ArcGisImage(this.layer,
                                    this.arcGisLayer,
                                    this.arcGisLayers,
                                    this.session.getClient(),
                                    this.session.getLayers().get(this.layerId).getStyleName(),
                                    null,
                                    this.token);
                        }

                        bufferedImage = this.image.draw(this.session.getTileSize(),
                                this.session.getLocation(),
                                bounds,
                                this.features);

                        if (bufferedImage == null) {
                            this.imageParsingFailed();
                            return "error";
                        }

                        // set to cache
                        if (!isboundaryTile) {
                            setImageCache(bufferedImage, cacheStyleName, bbox, true);
                        } else { // non-persistent cache - for ie
                            setImageCache(bufferedImage, cacheStyleName, bbox, false);
                        }
                    }

                    String url = createImageURL(this.session.getLayers().get(this.layerId).getStyleName(), bbox);
                    this.sendWFSImage(url, bufferedImage, bbox, true, isboundaryTile);
                }

                if (first) {
                    first = false;
                    this.session.setKeepPrevious(true); // keep the next tiles
                }
                index++;
            }
        } else if (this.type == JobType.HIGHLIGHT) {
            if (this.sendHighlight) {
                if (!this.requestHandler(null)) {
                    return STATUS_CANCELED;
                }
                this.featuresHandler();
                if (!goNext()) return STATUS_CANCELED;

                // Send geometries, if requested as well
                if (this.session.isGeomRequest()) {
                    this.sendWFSFeatureGeometries(this.geomValuesList, ResultProcessor.CHANNEL_FEATURE_GEOMETRIES);
                }

                log.debug("highlight image handling", this.features.size());

                Location location = this.session.getLocation();
                if (this.image == null) {
                    this.image = new ArcGisImage(this.layer,
                            this.arcGisLayer,
                            this.arcGisLayers,
                            this.session.getClient(),
                            this.session.getLayers().get(this.layerId).getStyleName(),
                            JobType.HIGHLIGHT.toString(),
                            this.token);
                }
                BufferedImage bufferedImage = null;
                bufferedImage = this.image.draw(this.session.getMapSize(),
                        location,
                        this.features);
                if (bufferedImage == null) {
                    this.imageParsingFailed();
                    return "error";
                }

                Double[] bbox = location.getBboxArray();

                // cache (non-persistant)
                setImageCache(bufferedImage, JobType.HIGHLIGHT.toString() + "_" + this.session.getSession(), bbox, false);

                String url = createImageURL(JobType.HIGHLIGHT.toString(), bbox);
                this.sendWFSImage(url, bufferedImage, bbox, false, false);
            }
        } else if (this.type == JobType.MAP_CLICK) {
            if (!this.requestHandler(null)) {
                this.sendWFSFeatures(EMPTY_LIST, ResultProcessor.CHANNEL_MAP_CLICK);
                return "success";
            }
            this.featuresHandler();
            if (!goNext()) return STATUS_CANCELED;
            if (this.sendFeatures) {
                log.debug("Feature values list", this.featureValuesList);
                this.sendWFSFeatures(this.featureValuesList, ResultProcessor.CHANNEL_MAP_CLICK);
            } else {
                log.debug("No feature data!");
                this.sendWFSFeatures(EMPTY_LIST, ResultProcessor.CHANNEL_MAP_CLICK);
            }
        } else if (this.type == JobType.GEOJSON) {
            if (!this.requestHandler(null)) {
                return STATUS_CANCELED;
            }
            this.featuresHandler();
            if (!goNext()) return STATUS_CANCELED;
            if (this.sendFeatures) {
                this.sendWFSFeatures(this.featureValuesList, ResultProcessor.CHANNEL_FILTER);
            }
        } else {
            log.error("Type is not handled", this.type);
        }
        return "success";
    }

    private List<ArcGisLayerStore> getArcGisLayersDependingOnScale() {
        List<ArcGisLayerStore> result = new ArrayList<ArcGisLayerStore>();
        List<ArcGisLayerStore> subLayers = this.arcGisLayer.getSubLayers();

        if (subLayers != null && subLayers.size() > 0) {
            double scale = this.session.getMapScales().get((int) this.session.getLocation().getZoom());

            for (ArcGisLayerStore subLayer : subLayers) {
                double minScale = subLayer.getMinScale();
                double maxScale = subLayer.getMaxScale();

                if (maxScale <= scale && scale <= minScale) {
                    result.add(subLayer);
                    log.info("Adding other arcgis layer id", this.arcGisLayer.getIdStr(), "=>", this.arcGisLayer.getIdStr());
                }
            }
        }

        if (result.size() == 0)
            result.add(this.arcGisLayer);

        return result;
    }

    /**
     * Parses response to features
     *
     * @param layer
     * @return features
     */
    public FeatureCollection<SimpleFeatureType, SimpleFeature> response(
            WFSLayerStore layer, RequestResponse requestResponse) {
        Reader response = ((WFSRequestResponse) requestResponse).getResponse();
        features = ArcGisCommunicator.parseFeatures(response, layer);
        //FeatureCollection<SimpleFeatureType, SimpleFeature> features = WFSCommunicator.parseSimpleFeatures(response, layer);
        IOHelper.close(response);
        // TODO: wrap features into featurecollection or make an alternative api
        return null; //features;
    }

    /**
     * Makes request and parses response to features
     *
     * @param bounds
     * @return <code>true</code> if thread should continue; <code>false</code>
     * otherwise.
     */
    protected boolean requestHandler(List<Double> bounds) {

        log.debug("Starting request handler");
        Map<String, Object> output = new HashMap<String, Object>();
        List<Reader> responses = new ArrayList<>();
        for (ArcGisLayerStore subLayer : this.arcGisLayers) {
            Reader response = sendQueryRequest(this.type, this.layer, subLayer, this.session, bounds, this.token);
            // request failed
            if(response == null) {
                log.warn("Request failed for layer", layer.getLayerId());
                output.put(OUTPUT_LAYER_ID, layer.getLayerId());
                output.put(OUTPUT_ONCE, true);
                output.put(OUTPUT_MESSAGE, ERROR_REST_REQUEST_FAILED);
                this.service.addResults(
                        session.getClient(), ResultProcessor.CHANNEL_ERROR, output);
                log.debug(PROCESS_ENDED, getKey());
                return false;
            }
            responses.add(response);
        }


        try {
            // parse response
            this.features = ArcGisCommunicator.parseFeatures(responses, this.layer);

            // parsing failed
            if (this.features == null) {
                log.warn("Parsing failed for layer", this.layerId);
                output.put(OUTPUT_LAYER_ID, this.layerId);
                output.put(OUTPUT_ONCE, true);
                output.put(OUTPUT_MESSAGE, ResultProcessor.ERROR_FEATURE_PARSING);
                this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_ERROR, output);
                log.debug(PROCESS_ENDED, getKey());
                return false;
            }

            log.info("Got", this.features.size(), "features for layer", layer.getLayerId());

            // 0 features found - send size
            if (this.type == JobType.MAP_CLICK && this.features.size() == 0) {
                log.debug("Empty result for map click", this.layerId);
                output.put(OUTPUT_LAYER_ID, this.layerId);
                output.put(OUTPUT_FEATURES, "empty");
                output.put(OUTPUT_KEEP_PREVIOUS, this.session.isKeepPrevious());
                this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_MAP_CLICK, output);
                log.debug(PROCESS_ENDED, getKey());
                return false;
            } else if (this.type == JobType.GEOJSON && this.features.size() == 0) {
                log.debug("Empty result for filter", this.layerId);
                output.put(OUTPUT_LAYER_ID, this.layerId);
                output.put(OUTPUT_FEATURES, "empty");
                this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_FILTER, output);
                log.debug(PROCESS_ENDED, getKey());
                return false;
            } else {
                if (this.features.size() == 0) {
                    log.debug("Empty result", this.layerId);
                    output.put(OUTPUT_LAYER_ID, this.layerId);
                    output.put(OUTPUT_FEATURE, "empty");
                    this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_FEATURE, output);
                    log.debug(PROCESS_ENDED, getKey());
                    return false;
                } else if (this.features.size() == layer.getMaxFeatures()) {
                    log.debug("Max feature result", this.layerId);
                    output.put(OUTPUT_LAYER_ID, this.layerId);
                    output.put(OUTPUT_FEATURE, "max");
                    this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_FEATURE, output);
                }
            }

            log.debug("Features count", this.features.size());
        } catch (Exception ee) {
            log.error("Unhandled exception during request: ", ee);
            output.put(OUTPUT_LAYER_ID, this.layerId);
            output.put(OUTPUT_ONCE, true);
            output.put(OUTPUT_MESSAGE, ERROR_REST_REQUEST_FAILED);
            this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_ERROR, output);
            log.debug(PROCESS_ENDED, getKey());
            return false;
        }

        return true;
    }

    /**
     * Parses features properties and sends to appropriate channels
     */
    protected void propertiesHandler() {
        if (!this.sendFeatures) {
            return;
        }

        log.debug("Starting properties handler");

        List<String> selectedProperties = new ArrayList<String>();
        List<String> layerSelectedProperties = layer.getSelectedFeatureParams(session.getLanguage());

        // selected props
        if (layerSelectedProperties != null && layerSelectedProperties.size() != 0) {
            selectedProperties.addAll(this.layer.getSelectedFeatureParams(this.session.getLanguage()));
        } else { // all properties
            for (ArcGisFeature feat : this.features) {
                for (ArcGisProperty prop : feat.getProperties()) {
                    String field = prop.getName().toString();
                    if (!this.layer.getGMLGeometryProperty().equals(field)) { // don't add geometry
                        selectedProperties.add(field);
                    }
                }
                break;
            }
        }

        log.info("Got", selectedProperties.size(), "properties for layer", layer.getLayerId());

        this.sendWFSProperties(selectedProperties, this.layer.getFeatureParamsLocales(this.session.getLanguage()));
    }

    /**
     * Parses features values
     */
    protected void featuresHandler() {
        log.debug("Starting features handler");

        Geometry screenGeometry = ArcGisFilter.initBBOXFilter(this.session.getLocation(), this.transformService);

        this.featureValuesList = new ArrayList<List<Object>>();
        this.geomValuesList = new ArrayList<List<Object>>();
        for (ArcGisFeature feature : this.features) {
            // if is not in shown area -> skip
            if (!screenGeometry.intersects(feature.getGeometry())) {
                //log.debug("Not selected feature ", feature.GetId());
                continue;
            }

            List<Object> values = new ArrayList<Object>();

            String fid = feature.GetId();

            if (fid == null) {
                log.warn("Feature has no id. Skipping");
                continue;
            }

            if (!this.processedFIDs.contains(fid)) {
                // __fid value
                values.add(fid);
                this.processedFIDs.add(fid);

                // get feature geometry (transform if needed) and get geometry center
                //Geometry geometry = WFSParser.getFeatureGeometry(feature, this.layer.getGMLGeometryProperty(), this.transformClient);
                Geometry geometry = feature.getGeometry();

                if (this.session.isGeomRequest()) {
                    List<Object> gvalues = new ArrayList<Object>();
                    gvalues.add(fid);
                    gvalues.add(geometry);
                    this.geomValuesList.add(gvalues);
                }

                // send values
                if (this.sendFeatures) {
                    Point centerPoint = WFSParser.getGeometryCenter(geometry);

                    // selected values
                    List<String> selectedProperties = layer.getSelectedFeatureParams(session.getLanguage());
                    if (selectedProperties != null && selectedProperties.size() != 0) {
                        for (String attr : selectedProperties) {
                            values.add(feature.getPropertyValue(attr));
                        }
                    } else { // all values


                        for (ArcGisProperty prop : feature.getProperties()) {
                            String field = prop.getName().toString();
                            if (!this.layer.getGMLGeometryProperty().equals(field)) { // don't add geometry
                                values.add(feature.getPropertyValue(field));
                            }
                        }

                    }

                    // center position (must be in properties also)
                    if (centerPoint != null) {
                        values.add(centerPoint.getX());
                        values.add(centerPoint.getY());
                    } else {
                        values.add(null);
                        values.add(null);
                    }

                    WFSParser.parseValuesForJSON(values);

                    if (this.type == JobType.NORMAL) {
                        this.sendWFSFeature(values);
                    } else {
                        this.featureValuesList.add(values);
                    }
                }
            } else {
                log.warn("Found duplicate feature ID", fid);
            }
        }
    }

    /**
     * Send image parsing error
     */
    protected void imageParsingFailed() {
        imageParsingFailed(ERROR_REST_IMAGE_PARSING);
    }

    @Override
    public RequestResponse request(JobType type, WFSLayerStore layer, SessionStore session, List<Double> bounds, MathTransform transformService) {
        throw new RuntimeException("Not implemented!");
    }
}