package fi.nls.oskari.work;

import com.netflix.hystrix.exception.HystrixBadRequestException;
import fi.nls.oskari.pojo.*;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.wfs.util.HttpHelper;
import fi.nls.oskari.wfs.*;

import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.work.hystrix.HystrixMapLayerJob;
import org.codehaus.jackson.map.ObjectMapper;
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
import java.util.*;
/**
 * Job for WFS Map Layer
 */
public class WFSMapLayerJob extends HystrixMapLayerJob {

    public static final String STATUS_CANCELED = "canceled";

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
	public WFSMapLayerJob(ResultProcessor service, JobType type, SessionStore store, String layerId) {
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
	public WFSMapLayerJob(ResultProcessor service, JobType type, SessionStore store, String layerId,
			boolean reqSendFeatures, boolean reqSendImage, boolean reqSendHighlight) {
	    super(service,type,store,layerId,reqSendFeatures,reqSendImage,reqSendHighlight);

    }






    /**
     * Makes request
     *
     * @param type
     * @param layer
     * @param session
     * @param bounds
     * @param transformService
     * @return response
     */
    public RequestResponse request(JobType type, WFSLayerStore layer,
            SessionStore session, List<Double> bounds,
            MathTransform transformService) {
        BufferedReader response = null;
        if (layer.getTemplateType() == null) { // default
            String payload = WFSCommunicator.createRequestPayload(type, layer,
                    session, bounds, transformService);
            log.debug("...WFS / Request data "+ layer.getURL() + "\n" + payload + "\n");
            response = HttpHelper.postRequestReader(layer.getURL(), "",
                    payload, layer.getUsername(), layer.getPassword());
        } else {
            log.debug(
                    "Failed to make a request because of undefined layer type "+
                    layer.getTemplateType());
        }

        WFSRequestResponse requestResponse = new WFSRequestResponse();
        requestResponse.setResponse(response);

        return requestResponse;
    }

    /**
     * Parses response to features
     *
     * @param layer
     * @return features
     */
    public FeatureCollection<SimpleFeatureType, SimpleFeature> response(
            WFSLayerStore layer, RequestResponse requestResponse) {
        BufferedReader response = ((WFSRequestResponse) requestResponse).getResponse();
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = WFSCommunicator.parseSimpleFeatures(response, layer);

        IOHelper.close(response);

        return features;
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
	 * Process of the job
	 *
	 * Worker calls this when starts the job.
	 *
	 */
	@Override
	public final String run() {
        log.debug(PROCESS_STARTED, getKey());

        if(!this.validateType()) {
            log.warn("Not enough information to continue the task (" +  this.type + ")");
            throw new HystrixBadRequestException("Not enough information to continue the task (" +  this.type + ")");
            //return "error";
        }

    	if(!goNext()) return STATUS_CANCELED;

        this.layerPermission = getPermissions(layerId, this.session.getSession(), this.session.getRoute());
        if(!this.layerPermission) {
            log.warn("Session (" +  this.session.getSession() + ") has no permissions for getting the layer (" + this.layerId + ")");
            Map<String, Object> output = new HashMap<String, Object>();
            output.put(OUTPUT_LAYER_ID, this.layerId);
            output.put(OUTPUT_ONCE, true);
            output.put(OUTPUT_MESSAGE, ResultProcessor.ERROR_NO_PERMISSIONS);
            this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_ERROR, output);
            throw new HystrixBadRequestException("Session (" +  this.session.getSession() + ") has no permissions for getting the layer (" + this.layerId + ")");
        }

    	if(!goNext()) return null;
    	this.layer = getLayerConfiguration(this.layerId, this.session.getSession(), this.session.getRoute());
        if(this.layer == null) {
            log.warn("Layer (" +  this.layerId + ") configurations couldn't be fetched");
            Map<String, Object> output = new HashMap<String, Object>();
            output.put(OUTPUT_LAYER_ID, this.layerId);
            output.put(OUTPUT_ONCE, true);
            output.put(OUTPUT_MESSAGE, ResultProcessor.ERROR_CONFIGURATION_FAILED);
            this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_ERROR, output);
            throw new RuntimeException("Layer (" +  this.layerId + ") configurations couldn't be fetched");
        }

		setResourceSending();

		if(!validateMapScales()) {
            log.debug("Map scale was not valid for layer",  this.layerId);
            throw new HystrixBadRequestException("Map scale was not valid for layer (" + this.layerId + ")");
		}

        // if different SRS, create transforms for geometries
        if(!this.session.getLocation().getSrs().equals(this.layer.getSRSName())) {
            this.transformService = this.session.getLocation().getTransformForService(this.layer.getCrs(), true);
            this.transformClient = this.session.getLocation().getTransformForClient(this.layer.getCrs(), true);
        }

        String cacheStyleName = this.session.getLayers().get(this.layerId).getStyleName();
        if(cacheStyleName.startsWith(WFSImage.PREFIX_CUSTOM_STYLE)) {
            cacheStyleName += "_" + this.session.getSession();
        }

        // init enlarged envelope
        List<List<Double>> grid = this.session.getGrid().getBounds();
        if(grid.size() > 0) {
            this.session.getLocation().setEnlargedEnvelope(grid.get(0));
        }

        if(!goNext()) return STATUS_CANCELED;

        log.debug(this.type);

        if(this.type == JobType.NORMAL) { // tiles for grid
            if(!this.layer.isTileRequest()) { // make single request
                if(!this.normalHandlers(null, true)) {
                    return STATUS_CANCELED;
                }
            }

            log.debug("normal tile images handling");

            boolean first = true;
			int index = 0;
			for(List<Double> bounds : grid) {
                if(this.layer.isTileRequest()) { // make a request per tile
                    if(!this.normalHandlers(bounds, first)) {
                        return STATUS_CANCELED;
                    }
                }

				if(!goNext()) return STATUS_CANCELED;

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
                            throw new RuntimeException("Image parsing failed!");
                        }

					    // set to cache
						if(!isboundaryTile) {
                            setImageCache(bufferedImage, cacheStyleName, bbox, true);
						} else { // non-persistent cache - for ie
                            setImageCache(bufferedImage, cacheStyleName, bbox, false);
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
		} else if(this.type == JobType.HIGHLIGHT) {
            if(this.sendHighlight) {
                if(!this.requestHandler(null)) {
                    return STATUS_CANCELED;
                }
                this.featuresHandler();
                if(!goNext()) return STATUS_CANCELED;

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
                BufferedImage bufferedImage = this.image.draw(this.session.getMapSize(),
                        location,
                        this.features);
                if(bufferedImage == null) {
                    this.imageParsingFailed();
                    throw new RuntimeException("Image parsing failed!");
                }

                Double[] bbox = location.getBboxArray();

                // cache (non-persistant)
                setImageCache(bufferedImage, JobType.HIGHLIGHT.toString() + "_" + this.session.getSession(), bbox, false);

                String url = createImageURL(JobType.HIGHLIGHT.toString(), bbox);
                this.sendWFSImage(url, bufferedImage, bbox, false, false);
            }
        } else if(this.type == JobType.MAP_CLICK) {
            if(!this.requestHandler(null)) {
                this.sendWFSFeatures(EMPTY_LIST, ResultProcessor.CHANNEL_MAP_CLICK);
                // success, just no hits
                return null;
            }
            this.featuresHandler();
            if(!goNext()) return STATUS_CANCELED;
            if(this.sendFeatures) {
                log.debug("Feature values list", this.featureValuesList);
                this.sendWFSFeatures(this.featureValuesList, ResultProcessor.CHANNEL_MAP_CLICK);
            } else {
                log.debug("No feature data!");
                this.sendWFSFeatures(EMPTY_LIST, ResultProcessor.CHANNEL_MAP_CLICK);
            }
        } else if(this.type == JobType.GEOJSON || this.type == JobType.PROPERTY_FILTER) {
            if(!this.requestHandler(null)) {
                return STATUS_CANCELED;
            }
            this.featuresHandler();
            if(!goNext()) return STATUS_CANCELED;
            if(this.sendFeatures) {
                this.sendWFSFeatures(this.featureValuesList, ResultProcessor.CHANNEL_FILTER);
            }
        } else {
            log.error("Type is not handled", this.type);
        }

        log.debug(PROCESS_ENDED, getKey());
        return "success";
	}


	/**
	 * Makes request and parses response to features
	 *
     * @param bounds
	 * @return <code>true</code> if thread should continue; <code>false</code>
	 *         otherwise.
	 */
    protected boolean requestHandler(List<Double> bounds) {

        // make a request
        RequestResponse response = request(type, layer, session, bounds, transformService);
        boolean success = false;

        try {
            // request failed
            if(response == null) {
                log.warn("Request failed for layer", layer.getLayerId());
                throw new RuntimeException(ResultProcessor.ERROR_WFS_REQUEST_FAILED);
            }

            // parse response
            this.features = response(layer, response);
            // parsing failed
            if(this.features == null) {
                log.warn("Parsing failed for layer", this.layerId);
                throw new RuntimeException(ResultProcessor.ERROR_FEATURE_PARSING);
            }

            final Map<String, Object> output = createCommonResponse();
            // 0 features found - send size
            if(this.type == JobType.MAP_CLICK && this.features.size() == 0) {
                log.debug("Empty result for map click",  this.layerId);
                output.put(OUTPUT_LAYER_ID, this.layerId);
                output.put(OUTPUT_FEATURES, "empty");
                output.put(OUTPUT_KEEP_PREVIOUS, this.session.isKeepPrevious());
                this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_MAP_CLICK, output);
                log.debug(PROCESS_ENDED, getKey());
                return false;
            } else if((this.type == JobType.GEOJSON && this.features.size() == 0) || (this.type == JobType.PROPERTY_FILTER && this.features.size() == 0)) {
                log.debug("Empty result for filter", this.layerId);
                output.put(OUTPUT_FEATURES, "empty");
                this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_FILTER, output);
                log.debug(PROCESS_ENDED, getKey());
                return false;
            } else {
                if(this.features.size() == 0) {
                    log.debug("Empty result",  this.layerId);
                    output.put(OUTPUT_FEATURE, "empty");
                    this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_FEATURE, output);
                    log.debug(PROCESS_ENDED, getKey());
                    return false;
                } else if(this.features.size() == layer.getMaxFeatures()) {
                    log.debug("Max feature result", this.layerId);
                    output.put(OUTPUT_FEATURE, "max");
                    this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_FEATURE, output);
                }
            }

            success = true;
            log.debug("Features count", this.features.size());
        } finally {
            if( response != null ) {
                try {
                    response.flush();
                } catch( java.io.IOException e) {
                    success = false;
                }
            }
        }

        return success;
    }

    /**
     * Parses features properties and sends to appropriate channels
     */
    protected void propertiesHandler() {
        if(!this.sendFeatures) {
            return;
        }

        log.debug("properties handler");
        final List<String> selectedProperties = getPropertiesToInclude();
        this.sendWFSProperties(selectedProperties, this.layer.getFeatureParamsLocales(this.session.getLanguage()));
    }

    /**
     * Returns array of property names that will be sent in response
     * @return
     */
    private List<String> getPropertiesToInclude() {

        final List<String> selectedProperties = layer.getSelectedFeatureParams(session.getLanguage());
        if(selectedProperties != null && selectedProperties.size() != 0) {
            log.debug("Using selected properties:", selectedProperties);
            return selectedProperties;
        }

        if(this.features.features().hasNext()) {
            final List<String> propertyNames = new ArrayList<String>();
            final Collection<Property> featureProperties = this.features.features().next().getProperties();
            for (Property prop : featureProperties) {
                final String field = prop.getName().toString();
                if (this.excludedProperties.contains(field)) {
                    continue;
                }
                // don't add geometry
                if (!this.layer.getGMLGeometryProperty().equals(field)) {
                    propertyNames.add(field);
                }
            }
            log.debug("Using all non-geometry feature properties:", propertyNames);
            return propertyNames;
        }
        else {
            log.warn("Tried to determine properties by there's no features!");
        }
        return Collections.EMPTY_LIST;
    }
    /**
     * Parses features values
     */
    protected void featuresHandler() {
        log.debug("features handler - layer:", this.layer.getLayerId());

        // create filter of screen area
        Filter screenBBOXFilter = WFSFilter.initBBOXFilter(this.session.getLocation(), this.layer);

        // send feature info
        FeatureIterator<SimpleFeature> featuresIter =  this.features.features();

        this.featureValuesList = new ArrayList<List<Object>>();
        this.geomValuesList = new ArrayList<List<Object>>();

        final List<String> selectedProperties = getPropertiesToInclude();

        while(goNext(featuresIter.hasNext())) {
            SimpleFeature feature = featuresIter.next();
            String fid = feature.getIdentifier().getID();
            log.debug("Processing properties of feature:", fid);

            // if is not in shown area -> skip
            if(!screenBBOXFilter.evaluate(feature)) {
                log.debug("Feature not on screen, skipping", fid);
                log.debug("Filter ", screenBBOXFilter);
                log.debug("Feature ", feature);
                continue;
            }

            List<Object> values = new ArrayList<Object>();

            if (this.processedFIDs.contains(fid)) {
                log.warn("Found duplicate feature ID", fid);
                continue;
            }
            // __fid value
            values.add(fid);
            this.processedFIDs.add(fid);

            // get feature geometry (transform if needed) and get geometry center
            Geometry geometry = WFSParser.getFeatureGeometry(feature, this.layer.getGMLGeometryProperty(), this.transformClient);

            // Add geometry property, if requested  in hili
            if (this.session.isGeomRequest())
            {
                log.debug("Requested geometry", fid);
                List<Object> gvalues = new ArrayList<Object>();
                gvalues.add(fid);
                if( geometry != null ) {
                gvalues.add(geometry.toText()); //feature.getAttribute(this.layer.getGMLGeometryProperty()));
                } else {
                    gvalues.add(null);
                }
                this.geomValuesList.add(gvalues);
            }

            // send values
            if(!this.sendFeatures) {
                log.warn("Didn't request properties - skipping", fid);
                continue;
            }
            Point centerPoint = WFSParser.getGeometryCenter(geometry);

            for (String attr : selectedProperties) {
                values.add(getFeaturePropertyValueForResponse(feature.getAttribute(attr)));
            }

            // center position (must be in properties also)
            if(centerPoint != null) {
                values.add(centerPoint.getX());
                values.add(centerPoint.getY());
            } else {
                values.add(null);
                values.add(null);
            }

            log.debug("Got property values:", values);
            WFSParser.parseValuesForJSON(values);
            log.debug("Transformed property values:", values);

            if(this.type == JobType.NORMAL) {
                this.sendWFSFeature(values);
            } else {
                this.featureValuesList.add(values);
            }
        }
	}

    /**
     * Normalize value for response
     * @param input
     * @return
     */
    private Object getFeaturePropertyValueForResponse(final Object input) {
        if(input == null) {
            return null;
        }
        final String value = input.toString();
        if(value == null) {
            return null;
        }
        if(value.isEmpty()) {
            log.debug("Value is empty");
            return "";
        }
        try {
            HashMap<String, Object> propMap = new ObjectMapper().readValue(value, HashMap.class);
            if(propMap.isEmpty()) {
                log.debug("Got empty map from value: '" + value + "' - Returning null. Input was", input.getClass().getName());
                return null;
            }
            return propMap;
        } catch (Exception e) {
            return value;
        }

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

}