package fi.nls.oskari.work;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.transport.TransportJobException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.wfs.WFSCommunicator;
import fi.nls.oskari.wfs.WFSExceptionHelper;
import fi.nls.oskari.wfs.WFSFilter;
import fi.nls.oskari.wfs.WFSParser;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.wfs.util.HttpHelper;
import fi.nls.oskari.wfs.extension.UserLayerProcessor;
import fi.nls.oskari.wfs.LayerProcessor;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.operation.MathTransform;

import java.io.Reader;
import java.util.*;
/**
 * Job for WFS Map Layer
 */
public class WFSMapLayerJob extends OWSMapLayerJob {
	LayerProcessor layerProcessor = new UserLayerProcessor ();

	/**
	 * Creates a new runnable job with own Jedis instance
	 *
	 * Parameters define client's service (communication channel), session and layer's id.
	 * Sends all resources that the layer configuration allows.
	 *
	 * @param service
	 * @param store
	 * @param layer
	 */
	public WFSMapLayerJob(ResultProcessor service, JobType type, SessionStore store, WFSLayerStore layer) {
		this(service, type, store, layer, true, true, true);
    }

	/**
	 * Creates a new runnable job with own Jedis instance
	 *
	 * Parameters define client's service (communication channel), session and layer's id.
	 * Also sets resources that will be sent if the layer configuration allows.
	 *
	 * @param service
	 * @param store
	 * @param layer
	 * @param reqSendFeatures
	 * @param reqSendImage
     * @param reqSendHighlight
	 */
	public WFSMapLayerJob(ResultProcessor service, JobType type, SessionStore store, WFSLayerStore layer,
			boolean reqSendFeatures, boolean reqSendImage, boolean reqSendHighlight) {
	    super(service,type,store,layer,reqSendFeatures,reqSendImage,reqSendHighlight);
    }


    /**
     * Makes request
     * Throws TransportJobException, if payload fails or post request response fails
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
        Reader response = null;
        if (layer.getTemplateType() == null) { // default
            String payload = WFSCommunicator.createRequestPayload(type, layer,
                    session, bounds, transformService);
            log.debug("...WFS / Request data "+ layer.getURL() + "\n" + payload + "\n");
            try {
                response = HttpHelper.postRequestReader(layer.getURL(), "",
                        payload, layer.getUsername(), layer.getPassword(), true);
            }
            catch (ServiceRuntimeException e){
                throw new TransportJobException(e.getMessage(),
                        e.getCause(),
                        WFSExceptionHelper.ERROR_GETFEATURE_POSTREQUEST_FAILED);
            }
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
        Reader response = ((WFSRequestResponse) requestResponse).getResponse();
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = WFSCommunicator.parseSimpleFeatures(response, layer);

        if (layerProcessor.isProcessable(layer)) {
			features = layerProcessor.process(features,layer);
        }

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
                log.warn("Request failed for layer: ",layer.getLayerName(), "id: ", layer.getLayerId());
                throw new TransportJobException("Request failed for layer: " + layer.getLayerName() + "id: " + layer.getLayerId(),
                        WFSExceptionHelper.ERROR_GETFEATURE_POSTREQUEST_FAILED);

            }

            // parse response, throws an exception on failure
            this.features = response(layer, response);
            final Map<String, Object> output = createCommonResponse();
            if(features == null || features.isEmpty()) {
                log.debug("Empty result for", this.layerId, "type:", type);
                output.put(OUTPUT_FEATURE, "empty");
                log.debug(PROCESS_ENDED, getKey());
                if(this.type == JobType.MAP_CLICK) {
                    output.put(OUTPUT_KEEP_PREVIOUS, this.session.isKeepPrevious());
                    this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_MAP_CLICK, output);
                    return false;
                } else if(this.type == JobType.GEOJSON  || this.type == JobType.PROPERTY_FILTER) {
                    output.put(OUTPUT_KEEP_PREVIOUS, this.session.isKeepPrevious());
                    this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_FILTER, output);
                    return false;
                }
                this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_FEATURE, output);
                return false;
            }
            // Swap XY in feature geometry, if reverseXY setup in layer attributes
            if(layer.isReverseXY(session.getLocation().getSrs())){
                ProjectionHelper.swapGeometryXY(this.features);
            }

            if(this.features.size() == layer.getMaxFeatures()) {
                log.debug("Max feature result", this.layerId);
                output.put(OUTPUT_FEATURE, "max");
                this.service.addResults(session.getClient(), ResultProcessor.CHANNEL_FEATURE, output);
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
        Filter screenBBOXFilter = WFSFilter.initBBOXFilter(this.session.getLocation(), this.layer, false);
        if(screenBBOXFilter == null) {
            throw new TransportJobException("Failed to create BBOX filter (location or layer is unset)",
                    WFSExceptionHelper.ERROR_FEATURE_PARSING);

        }

        // send feature info
        FeatureIterator<SimpleFeature> featuresIter =  this.features.features();

        this.featureValuesList = new ArrayList<List<Object>>();
        this.geomValuesList = new ArrayList<List<Object>>();

        final List<String> selectedProperties = getPropertiesToInclude();

        boolean geometryParingFailures = false;

        while(goNext(featuresIter.hasNext())) {
            SimpleFeature feature = featuresIter.next();
            String fid = feature.getIdentifier().getID();
            log.debug("Processing properties of feature:", fid);

            // if is not in shown area -> skip
            if(!screenBBOXFilter.evaluate(feature)) {
                log.debug("Feature not on screen, skipping", fid);
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
                    log.debug("Feature geometry parsing failed", fid);
                    gvalues.add(null);
                    geometryParingFailures = true;
                }
                this.geomValuesList.add(gvalues);
            }

            // send values
            if(!this.sendFeatures) {
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

        if(geometryParingFailures){
            Map<String, Object> output = this.createCommonWarningResponse(
                    "Geometry parsing of some features failed (unknown geometry property or transformation error",
                    WFSExceptionHelper.WARNING_GEOMETRY_PARSING_FAILED);
            this.sendCommonErrorResponse(output, true);
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