package fi.nls.oskari.work;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.*;
import fi.nls.oskari.transport.TransportService;
import fi.nls.oskari.utils.HttpHelper;
import fi.nls.oskari.wfs.WFSCommunicator;
import fi.nls.oskari.wfs.WFSFilter;
import fi.nls.oskari.wfs.WFSImage;
import fi.nls.oskari.wfs.WFSParser;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import org.opengis.filter.Filter;
import org.opengis.referencing.operation.MathTransform;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Job for WFS Map Layer
 */
public class WFSMapLayerJob extends Job {
	
	private static final Logger log = LogFactory.getLogger(WFSMapLayerJob.class);

    public static enum Type {
        NORMAL ("normal"),
        HIGHLIGHT ("highlight"),
        MAP_CLICK ("mapClick"),
        GEOJSON("geoJSON");

        private final String name;

        private Type(String name) {
            this.name = name;
        }

        @Override
        public String toString(){
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
    public static final String OUTPUT_IMAGE_HEIGHT= "height";
    public static final String OUTPUT_IMAGE_URL = "url";
    public static final String OUTPUT_IMAGE_DATA = "data";
    public static final String OUTPUT_BOUNDARY_TILE = "boundaryTile";

    public static final String BROWSER_MSIE = "msie";
    
    public static final String PROCESS_STARTED = "Started";
    public static final String PROCESS_ENDED = "Ended";

	// process information
	TransportService service;
	private SessionStore session;
    private Layer sessionLayer;
	private WFSLayerStore layer;
	private WFSLayerPermissionsStore permissions;
	private String layerId;
	private boolean layerPermission;
	private boolean reqSendFeatures;
	private boolean reqSendImage;
    private boolean reqSendHighlight;
	private boolean sendFeatures;
	private boolean sendImage;
    private boolean sendHighlight;
    private MathTransform transformService;
    private MathTransform transformClient;
	private Type type;
	private FeatureCollection<SimpleFeatureType, SimpleFeature> features;
    private List<List<Object>> featureValuesList;
    private List<String> processedFIDs = new ArrayList<String>();
    private WFSImage image = null;
    private Units units = new Units();
	
	// API
	private static final String PERMISSIONS_API = "GetLayerIds";
	private static final String LAYER_CONFIGURATION_API = "GetWFSLayerConfiguration&id=";

    // COOKIE
    private static final String ROUTE_COOKIE_NAME = "ROUTEID=";

	/**
	 * Creates a new runnable job with own Jedis instance
	 * 
	 * Parameters define client's service (communication channel), session and layer's id.
	 * Sends all resources that the layer configuration allows.
	 * 
	 * @param service
	 * @param store
	 * @param layerId
	 */
	public WFSMapLayerJob(TransportService service, Type type, SessionStore store, String layerId) {
		this(service, type, store, layerId, true, true, true);
    }
	
	/**
	 * Creates a new runnable job with own Jedis instance
	 * 
	 * Parameters define client's service (communication channel), session and layer's id.
	 * Also sets resources that will be sent if the layer configuration allows.
	 * 
	 * @param service
	 * @param store
	 * @param layerId
	 * @param reqSendFeatures
	 * @param reqSendImage
     * @param reqSendHighlight
	 */
	public WFSMapLayerJob(TransportService service, Type type, SessionStore store, String layerId,
			boolean reqSendFeatures, boolean reqSendImage, boolean reqSendHighlight) {
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
		return this.getClass().getSimpleName() + "_" + this.session.getClient() + "_" + this.layerId + "_" + this.type;
	}

	/**
	 * Gets service path for local API
	 * 
	 * Path for Layer configuration and permissions request
	 * 
	 * @return URL
	 */
	public String getAPIUrl() {
        String session = "";
        if(TransportService.SERVICE_URL_SESSION_PARAM != null) {
            session = ";" + TransportService.SERVICE_URL_SESSION_PARAM + "=" + this.session.getSession();
        }
		return TransportService.SERVICE_URL + TransportService.SERVICE_URL_PATH + session + TransportService.SERVICE_URL_LIFERAY_PATH;
	}
	
	/**
	 * Process of the job
	 * 
	 * Worker calls this when starts the job.
	 *
	 */
	@Override
	public final void run() {
        log.debug(PROCESS_STARTED, getKey());

        if(!validateType()) {
            log.warn("Not enough information to continue the task (" +  this.type + ")");
            return;
        }

    	if(!goNext()) return;
    	this.getPermissions();
		if(!this.layerPermission) {
			return;
		}

    	if(!goNext()) return;
    	this.getLayerConfiguration();
		if(this.layer == null) {
            log.error("Getting layer configuration failed");
			return;
		}

		setResourceSending();

		if(!validateMapScales()) {
            log.debug("Map scale was not valid for layer",  this.layerId);
			return;
		}

        // if different SRS, create transforms for geometries
        if(!this.session.getLocation().getSrs().equals(this.layer.getSRSName())) {
            this.transformService = this.session.getLocation().getTransformForService(this.layer.getCrs(), true);
            this.transformClient = this.session.getLocation().getTransformForClient(this.layer.getCrs(), true);
        }
        // init enlarged envelope
        List<List<Double>> grid = this.session.getGrid().getBounds();
        if(grid.size() > 0) {
            this.session.getLocation().setEnlargedEnvelope(grid.get(0));
        }

        if(!goNext()) return;

        log.debug(this.type);

        if(this.type == Type.NORMAL) { // tiles for grid
            if(!this.layer.isTileRequest()) { // make single request
                if(!this.normalHandlers(null, true)) {
                    return;
                }
            }

            log.debug("normal tile images handling");

            boolean first = true;
			int index = 0;
			for(List<Double> bounds : grid) {
                if(this.layer.isTileRequest()) { // make a request per tile
                    if(!this.normalHandlers(bounds, first)) {
                        return;
                    }
                }

				if(!goNext()) return;
				
				if(this.sendImage && this.sessionLayer.isTile(bounds)) { // check if needed tile
		   	 		Double[] bbox = new Double[4];
		   	 		for (int i = 0; i < bbox.length; i++) {
			   	 		bbox[i] = bounds.get(i);
		   	 		}
		   	 		
					// get from cache
				    BufferedImage bufferedImage = getImageCache(bbox);
			    	boolean fromCache = (bufferedImage != null);
                    boolean isboundaryTile = this.session.getGrid().isBoundsOnBoundary(index);

			    	if(!fromCache) {
                        if(this.image == null) {
                            this.image = new WFSImage(this.layer,
                                    this.session.getClient(),
                                    this.session.getLayers().get(this.layerId).getStyleName(),
                                    null);
                        }
					    bufferedImage = this.image.draw(this.session.getTileSize(),
                                this.session.getLocation(),
                                bounds,
                                this.features);
                        if(bufferedImage == null) {
                            this.imageParsingFailed();
                            return;
                        }

					    // set to cache
						if(!isboundaryTile) {
                            setImageCache(bufferedImage, this.session.getLayers().get(this.layerId).getStyleName(), bbox, true);
						} else { // non-persistent cache - for ie
                            setImageCache(bufferedImage, this.session.getLayers().get(this.layerId).getStyleName(), bbox, false);
						}
					}

		   	 		String url = createImageURL(this.session.getLayers().get(this.layerId).getStyleName(), bbox);
					this.sendWFSImage(url, bufferedImage, bbox, true, isboundaryTile);
				}

				if(first) {
					first = false;
					this.session.setKeepPrevious(true); // keep the next tiles
				}
				index++;
			}
		} else if(this.type == Type.HIGHLIGHT) {
            if(this.sendHighlight) {
                if(!this.requestHandler(null)) {
                    return;
                }
                this.featuresHandler();
                if(!goNext()) return;

                log.debug("highlight image handling", this.features.size());

                // IMAGE HANDLING
                log.debug("sending");
                Location location = this.session.getLocation();
                if(this.image == null) {
                    this.image = new WFSImage(this.layer,
                            this.session.getClient(),
                            this.session.getLayers().get(this.layerId).getStyleName(),
                            Type.HIGHLIGHT.toString());
                }
                BufferedImage bufferedImage = this.image.draw(this.session.getMapSize(),
                        this.session.getLocation(),
                        this.features);
                if(bufferedImage == null) {
                    this.imageParsingFailed();
                    return;
                }

                Double[] bbox = location.getBboxArray();

                // cache (non-persistant)
                setImageCache(bufferedImage, Type.HIGHLIGHT.toString(), bbox, false);

                String url = createImageURL(Type.HIGHLIGHT.toString(), bbox);
                log.debug("url");
                this.sendWFSImage(url, bufferedImage, bbox, false, false);
            }
        } else if(this.type == Type.MAP_CLICK) {
            if(!this.requestHandler(null)) {
                return;
            }
            this.featuresHandler();
            if(!goNext()) return;
            if(this.sendFeatures) {
                this.sendWFSFeatures(this.featureValuesList, TransportService.CHANNEL_MAP_CLICK);
            }
        } else if(this.type == Type.GEOJSON) {
            if(!this.requestHandler(null)) {
                return;
            }
            this.featuresHandler();
            if(!goNext()) return;
            if(this.sendFeatures) {
                this.sendWFSFeatures(this.featureValuesList, TransportService.CHANNEL_FILTER);
            }
        } else {
            log.error("Type is not handled", this.type);
        }

        log.debug(PROCESS_ENDED, getKey());
	}

    /**
     * Wrapper for normal type job's handlers
     */
    private boolean normalHandlers(List<Double> bounds, boolean first) {
        if(!this.requestHandler(bounds)) {
            log.debug("Cancelled by request handler");
            return false;
        }
        if(first) {
            propertiesHandler();
            if(!goNext()) return false;
        }
        if(!goNext()) return false;
        this.featuresHandler();
        if(!goNext()) return false;
        return true;
    }

	/**
	 * Makes request and parses response to features
	 *
     * @param bounds
	 * @return <code>true</code> if thread should continue; <code>false</code>
	 *         otherwise.
	 */
	private boolean requestHandler(List<Double> bounds) {
        BufferedReader response = null;
        if(layer.getTemplateType() == null) { // default
            String payload = WFSCommunicator.createRequestPayload(this.type, this.layer, this.session, bounds, this.transformService);
            log.debug("Request data\n", this.layer.getURL(), "\n", payload);
	    	if(!goNext()) return false;
			response = HttpHelper.postRequestReader(this.layer.getURL(), "", payload, this.layer.getUsername(), this.layer.getPassword());
        } else {
        	log.warn("Failed to make a request because of undefined layer type", layer.getTemplateType());
        }

        Map<String, Object> output = new HashMap<String, Object>();

        // request failed
		if(response == null) {
            log.warn("Request failed for layer",  this.layerId);
	   	 	output.put(OUTPUT_LAYER_ID, this.layerId);
	   	 	output.put(OUTPUT_ONCE, true);
	   	 	output.put(OUTPUT_MESSAGE, "wfs_request_failed");
	    	this.service.send(session.getClient(), TransportService.CHANNEL_ERROR, output);
	        log.debug(PROCESS_ENDED, getKey());
			return false;
		}
		
    	if(!goNext()) return false;
    	
		// parse response
        if(this.layer.getFeatureType().size() > 0) { // custom type => custom parsing
            WFSParser parser = new WFSParser(response, this.layer);
        	this.features = parser.parse();
        } else {
        	this.features = WFSCommunicator.parseSimpleFeatures(response, this.layer);        	
        }

		// parsing failed
		if(this.features == null) {
            log.warn("Parsing failed for layer",  this.layerId);
	   	 	output.put(OUTPUT_LAYER_ID, this.layerId);
	   	 	output.put(OUTPUT_ONCE, true);
	   	 	output.put(OUTPUT_MESSAGE, "features_parsing_failed");
	    	this.service.send(session.getClient(), TransportService.CHANNEL_ERROR, output);
	        log.debug(PROCESS_ENDED, getKey());
			return false;
		}

        // 0 features found - send size
        if(this.type == Type.MAP_CLICK && this.features.size() == 0) {
            log.debug("Empty result for map click",  this.layerId);
            output.put(OUTPUT_LAYER_ID, this.layerId);
            output.put(OUTPUT_FEATURES, "empty");
            output.put(OUTPUT_KEEP_PREVIOUS, this.session.isKeepPrevious());
            this.service.send(session.getClient(), TransportService.CHANNEL_MAP_CLICK, output);
            log.debug(PROCESS_ENDED, getKey());
            return false;
        } else if(this.type == Type.GEOJSON && this.features.size() == 0) {
            log.debug("Empty result for filter",  this.layerId);
            output.put(OUTPUT_LAYER_ID, this.layerId);
            output.put(OUTPUT_FEATURES, "empty");
            this.service.send(session.getClient(), TransportService.CHANNEL_FILTER, output);
            log.debug(PROCESS_ENDED, getKey());
            return false;
        } else {
            if(this.features.size() == 0) {
                log.debug("Empty result",  this.layerId);
                output.put(OUTPUT_LAYER_ID, this.layerId);
                output.put(OUTPUT_FEATURE, "empty");
                this.service.send(session.getClient(), TransportService.CHANNEL_FEATURE, output);
                log.debug(PROCESS_ENDED, getKey());
                return false;
            } else if(this.features.size() == layer.getMaxFeatures()) {
                log.debug("Max feature result",  this.layerId);
                output.put(OUTPUT_LAYER_ID, this.layerId);
                output.put(OUTPUT_FEATURE, "max");
                this.service.send(session.getClient(), TransportService.CHANNEL_FEATURE, output);
            }
        }

        log.debug("Features count", this.features.size());

        return true;
    }

    /**
     * Parses features properties and sends to appropriate channels
     */
    private void propertiesHandler() {
        if(!this.sendFeatures) {
            return;
        }

        log.debug("properties handler");

        List<String> selectedProperties = new ArrayList<String>();
        List<String> layerSelectedProperties = layer.getSelectedFeatureParams(session.getLanguage());

        // selected props
        if(layerSelectedProperties != null && layerSelectedProperties.size() != 0) {
            selectedProperties.addAll(this.layer.getSelectedFeatureParams(this.session.getLanguage()));
        } else { // all properties
            for(Property prop : this.features.features().next().getProperties()) {
                String field = prop.getName().toString();
                if(!this.layer.getGMLGeometryProperty().equals(field)) { // don't add geometry
                    selectedProperties.add(field);
                }
            }
        }

        this.sendWFSProperties(selectedProperties, this.layer.getFeatureParamsLocales(this.session.getLanguage()));
    }

    /**
     * Parses features values
     */
    private void featuresHandler() {
        log.debug("features handler");

        // create filter of screen area
        Filter screenBBOXFilter = WFSFilter.initBBOXFilter(this.session.getLocation(), this.layer);

        // send feature info
        FeatureIterator<SimpleFeature> featuresIter =  this.features.features();

        this.featureValuesList = new ArrayList<List<Object>>();
        while(goNext(featuresIter.hasNext())) {
            SimpleFeature feature = featuresIter.next();

            // if is not in shown area -> skip
            if(!screenBBOXFilter.evaluate(feature)) {
                //log.debug("Not selected");
                continue;
            }

            List<Object> values = new ArrayList<Object>();

            String fid = feature.getIdentifier().getID();
            if (!this.processedFIDs.contains(fid)) {
                // __fid value
                values.add(fid);
                this.processedFIDs.add(fid);

                // get feature geometry (transform if needed) and get geometry center
                Geometry geometry = WFSParser.getFeatureGeometry(feature, this.layer.getGMLGeometryProperty(), this.transformClient);

                // send values
                if(this.sendFeatures) {
                    Point centerPoint = WFSParser.getGeometryCenter(geometry);

                    // selected values
                    List<String> selectedProperties = layer.getSelectedFeatureParams(session.getLanguage());
                    if(selectedProperties != null && selectedProperties.size() != 0) {
                        for(String attr : selectedProperties) {
                            values.add(feature.getAttribute(attr));
                        }
                    } else { // all values
                        for(Property prop : this.features.features().next().getProperties()) {
                            String field = prop.getName().toString();
                            if(!this.layer.getGMLGeometryProperty().equals(field)) { // don't add geometry
                                values.add(feature.getAttribute(field));
                            }
                        }
                    }

                    // center position (must be in properties also)
                    if(centerPoint != null) {
                        values.add(centerPoint.getX());
                        values.add(centerPoint.getY());
                    } else {
                        values.add(null);
                        values.add(null);
                    }

                    WFSParser.parseValuesForJSON(values);

                    if(this.type == Type.NORMAL) {
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
     * Gets image from cache
     *
     * @param bbox
     */
    private BufferedImage getImageCache(Double[] bbox) {
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
     * @param bbox
     * @param persistent
     */
    private void setImageCache(BufferedImage bufferedImage, final String style, Double[] bbox, boolean persistent) {
        WFSImage.setCache(
                bufferedImage,
                this.layerId,
                style,
                this.session.getLocation().getSrs(),
                bbox,
                this.session.getLocation().getZoom(),
                persistent
        );
    }

    /**
     * Send image parsing error
     */
    private void imageParsingFailed() {
        log.error("Image parsing failed");
        Map<String, Object> output = new HashMap<String, Object>();
        output.put(OUTPUT_LAYER_ID, this.layerId);
        output.put(OUTPUT_ONCE, true);
        output.put(OUTPUT_MESSAGE, "wfs_image_parsing_failed");
        this.service.send(session.getClient(), TransportService.CHANNEL_ERROR, output);
    }

    /**
     * Checks if enough information for running the task type
     *
     * @return <code>true</code> if enough information for type; <code>false</code>
     *         otherwise.
     */
    private boolean validateType() {
        if(this.type == Type.HIGHLIGHT) {
            if(this.sessionLayer.getHighlightedFeatureIds() != null &&
                    this.sessionLayer.getHighlightedFeatureIds().size() > 0) {
                return true;
            }
        } else if(this.type == Type.MAP_CLICK) {
            if(session.getMapClick() != null) {
                return true;
            }
        } else if(this.type == Type.GEOJSON) {
            if(session.getFilter() != null) {
                return true;
            }
        } else if(this.type == Type.NORMAL) {
            return true;
        }
        return false;
    }


	/**
	 * Gets layer permissions (uses cache) 
	 */
	private void getPermissions() {
    	String json = WFSLayerPermissionsStore.getCache(this.session.getSession());
    	boolean fromCache = (json != null);
    	if(!fromCache) {
    		log.warn(getAPIUrl() + PERMISSIONS_API);
            String cookies = null;
            if(this.session.getRoute() != null && !this.session.getRoute().equals("")) {
                cookies = ROUTE_COOKIE_NAME + this.session.getRoute();
            }
			json = HttpHelper.getRequest(getAPIUrl() + PERMISSIONS_API, cookies);
			if(json == null)
				return;
		}
    	try {
    		this.permissions = WFSLayerPermissionsStore.setJSON(json);
    		this.layerPermission = this.permissions.isPermission(this.layerId);
    	} catch (IOException e) {
            log.error(e, "JSON parsing failed for WFSLayerPermissionsStore \n" + json);
    	}

        // no permissions
        if(!this.layerPermission) {
            log.warn("Session (" +  this.session.getSession() + ") has no permissions for getting the layer (" + this.layerId + ")");
            Map<String, Object> output = new HashMap<String, Object>();
            output.put(OUTPUT_LAYER_ID, this.layerId);
            output.put(OUTPUT_ONCE, true);
            output.put(OUTPUT_MESSAGE, "wfs_no_permissions");
            this.service.send(session.getClient(), TransportService.CHANNEL_ERROR, output);
        }
	}

	/**
	 * Gets layer configuration (uses cache) 
	 */
	private void getLayerConfiguration() {
    	String json = WFSLayerStore.getCache(this.layerId);
    	boolean fromCache = (json != null);
    	if(!fromCache) {
    		log.warn(getAPIUrl() + LAYER_CONFIGURATION_API + this.layerId);
            String cookies = null;
            if(this.session.getRoute() != null && !this.session.getRoute().equals("")) {
                cookies = ROUTE_COOKIE_NAME + this.session.getRoute();
            }
            // NOTE: result is not handled
			String result = HttpHelper.getRequest(getAPIUrl() + LAYER_CONFIGURATION_API + this.layerId, cookies);
            json = WFSLayerStore.getCache(this.layerId);
			if(json == null)
				return;
		}
    	try {
    		this.layer = WFSLayerStore.setJSON(json);
    	} catch (Exception e) {
            log.error(e, "JSON parsing failed for WFSLayerStore \n" + json);
    	}

        // no layer
        if(this.layer == null) {
            log.warn("Layer (" +  this.layerId + ") configurations couldn't be fetched");
            Map<String, Object> output = new HashMap<String, Object>();
            output.put(OUTPUT_LAYER_ID, this.layerId);
            output.put(OUTPUT_ONCE, true);
            output.put(OUTPUT_MESSAGE, "wfs_configuring_layer_failed");
            this.service.send(session.getClient(), TransportService.CHANNEL_ERROR, output);
        }
	}
	
	/**
	 * Sets which resources will be sent (features, image)
	 */
	private void setResourceSending() {
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
	 * @return <code>true</code> if map scale is valid; <code>false</code>
	 *         otherwise.
	 */
	private boolean validateMapScales() {
		double scale = this.session.getMapScales().get((int)this.session.getLocation().getZoom());
        double minScaleInMapSrs = units.getScaleInSrs(layer.getMinScale(), layer.getSRSName(), session.getLocation().getSrs());
        double maxScaleInMapSrs = units.getScaleInSrs(layer.getMaxScale(), layer.getSRSName(), session.getLocation().getSrs());

		log.debug("Scale in:", layer.getSRSName(), scale, "[", layer.getMaxScale(), ",", layer.getMinScale(), "]");
        log.debug("Scale in:", session.getLocation().getSrs(), scale, "[", maxScaleInMapSrs, ",", minScaleInMapSrs, "]");
		if(minScaleInMapSrs >= scale && maxScaleInMapSrs <= scale) // min == biggest value
			return true;
		return false;
	}

    /**
     * Creates image url
     *
     * @param style
     * @param bbox
     */
    private String createImageURL(final String style, Double[] bbox) {
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
    private void sendWFSProperties(List<String> fields, List<String> locales) {    
    	if(fields == null || fields.size() == 0) {
            log.warn("Failed to send properties");
    		return;
    	}

    	fields.add(0, "__fid");
    	fields.add("__centerX");
    	fields.add("__centerY");

    	if(locales != null) {
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

    	this.service.send(this.session.getClient(), TransportService.CHANNEL_PROPERTIES, output);
    }
    
    /**
     * Sends one feature
     * 
     * @param values
     */
    private void sendWFSFeature(List<Object> values) {    	
    	if(values == null || values.size() == 0) {
            log.warn("Failed to send feature");
    		return;   	
    	}
    	
    	Map<String, Object> output = new HashMap<String, Object>();
   	 	output.put(OUTPUT_LAYER_ID, this.layerId);
   	 	output.put(OUTPUT_FEATURE, values);

    	this.service.send(this.session.getClient(), TransportService.CHANNEL_FEATURE, output);
    }

    /**
     * Sends list of features
     * 
     * @param features
     * @param channel
     */
    private void sendWFSFeatures(List<List<Object>> features, String channel) {
    	if(features == null || features.size() == 0) {
            log.warn("Failed to send features");
    		return;   	
    	}
    	
    	Map<String, Object> output = new HashMap<String, Object>();
   	 	output.put(OUTPUT_LAYER_ID, this.layerId);
   	 	output.put(OUTPUT_FEATURES, features);
   	 	if(channel.equals(TransportService.CHANNEL_MAP_CLICK)) {
   	 		output.put(OUTPUT_KEEP_PREVIOUS, this.session.isKeepPrevious());
   	 	}

    	this.service.send(this.session.getClient(), channel, output);
    }

    /**
     * Sends image as an URL to IE 8 & 9, base64 data for others
     *
     * @param url
     * @param bufferedImage
     * @param bbox
     * @param isTiled
     */
    private void sendWFSImage(String url, BufferedImage bufferedImage, Double[] bbox, boolean isTiled, boolean isboundaryTile) {
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
   	 	output.put(OUTPUT_IMAGE_TYPE, this.type); // "normal" | "highlight"
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

    	this.service.send(this.session.getClient(), TransportService.CHANNEL_IMAGE, output);
    }
}