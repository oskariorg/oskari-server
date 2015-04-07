package fi.nls.oskari.wfs;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.work.JobType;
import fi.nls.oskari.work.ResultProcessor;
import fi.nls.oskari.work.WFSMapLayerJob;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.wfs.util.XMLHelper;
import org.opengis.referencing.operation.MathTransform;

/**
 * WFS request creators and response parsers
 */
public class WFSCommunicator {
    
	private static final Logger log = LogFactory.getLogger(WFSCommunicator.class);
			
	private static final String VERSION_1_0_0 = "1.0.0";
	private static final String VERSION_1_1_0 = "1.1.0";

    private static final String PROPERTY_PREFIX_EXT = "wfs.extension.";

	/**
	 * Creates request payload for WFS 1.1.0 (default request type)
	 * 
	 * @param layer
	 * @param session
     * @param bounds
	 * @return xml payload
	 */
	@SuppressWarnings("unchecked")
	public static String createRequestPayload(final JobType type, final WFSLayerStore layer, final SessionStore session, final List<Double> bounds, final MathTransform transform) {


		OMFactory factory = OMAbstractFactory.getOMFactory();

		// namespaces
		OMNamespace xsi = factory.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
		OMNamespace wfs = factory.createOMNamespace("http://www.opengis.net/wfs", "wfs");
		// root element
		OMElement root = factory.createOMElement("GetFeature", wfs);
        try {
		OMAttribute schemaLocation = factory.createOMAttribute("schemaLocation", 
				xsi,
				"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/" 
				+ layer.getWFSVersion() + "/wfs.xsd");
        // quite ugly, but only way to handle if namespace is just used in TEXT
        OMAttribute layerNs = factory.createOMAttribute("xmlns:" + layer.getFeatureNamespace(), null, layer.getFeatureNamespaceURI());
		OMAttribute version = factory.createOMAttribute("version", null, layer.getWFSVersion());
		OMAttribute service = factory.createOMAttribute("service", null, "WFS");
		OMAttribute maxFeatures = factory.createOMAttribute("maxFeatures", null, String.valueOf(layer.getMaxFeatures()));

        // if geometry property has different namespace than the featureNamespace
        String[] split = layer.getGMLGeometryProperty().split(":");
        if(split.length >= 2 && !split[0].equals(layer.getFeatureNamespace())) {
            if(layer.getGeometryNamespaceURI() == null) {
                log.error("No geometry namespace URI defined");
                return null;
            }
            OMAttribute geomNs = factory.createOMAttribute("xmlns:" + split[0], null, layer.getGeometryNamespaceURI());
            root.addAttribute(geomNs);
        }

        if (layer.getOutputFormat() != null) {
            OMAttribute outputFormat = factory.createOMAttribute("outputFormat", null, layer.getOutputFormat());
            root.addAttribute(outputFormat);
        }

        root.addAttribute(layerNs);
		root.addAttribute(schemaLocation);
		root.addAttribute(version);
		root.addAttribute(service);
		root.addAttribute(maxFeatures);

		// query
		//try {
            OMElement query = factory.createOMElement("Query", wfs);
            OMAttribute typeName = factory.createOMAttribute("typeName", null,
                    layer.getFeatureNamespace() + ":" + layer.getFeatureElement());
            OMAttribute srsName = factory.createOMAttribute("srsName", null, layer.getSRSName());
            query.addAttribute(typeName);
            query.addAttribute(srsName);
            root.addChild(query);


            List<String> selectedProperties = layer.getSelectedFeatureParams(session.getLanguage());
            if(selectedProperties != null && !selectedProperties.isEmpty()) {
                // FIXME: Nooooooo... we should just return arraylist if we need it, instead of casting it from list here...
                selectedProperties = (ArrayList<String>) ((ArrayList<String>) selectedProperties).clone();
            }
            if(!layer.isGetFeatureInfo()) {
                if(layer.isGetMapTiles()) { // only geometry
                    OMElement property = factory.createOMElement("PropertyName", wfs);
                    property.setText(layer.getGMLGeometryProperty());
                    query.addChild(property);
                }
            } else if(selectedProperties == null || selectedProperties.isEmpty()) {
                // empty selection, and features wanted - give all (also map tiles
            } else {
                // Commented out since we always want the geometry field in queries
                //if(layer.isGetMapTiles()) {
                selectedProperties.add(layer.getGMLGeometryProperty());
                //}
            }
            // loop for all properties
      /*      if(selectedProperties != null) {
                for(String prop : selectedProperties) {
                    OMElement property = factory.createOMElement("PropertyName", wfs);
                    if(!prop.contains(":")) {
                        property.setText(layer.getFeatureNamespace() + ":" + prop);
                    } else {
                        property.setText(prop);
                    }
                    query.addChild(property);
                }
            }  -- eliminated because of GetFeature fails when property name is not valid or not for all features */

            // load filter
            WFSFilter wfsFilter = constructFilter(layer.getLayerId());
            String filterStr = wfsFilter.create(type, layer, session, bounds, transform);
            log.debug(" ++++++++++++++++++++++++++++++ filter xml: ", filterStr);
            if(filterStr != null) {
                StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(filterStr);
                OMElement filter = staxOMBuilder.getDocumentElement();
                query.addChild(filter);
            }
		}
		catch (Exception e){
		    log.error(e, "Failed to create payload - root: ", root);
		}

		return root.toString();
	}
	
	/**
	 * Parses simple features from default and FMI request's responses with geotools
	 * 
	 * Parser configurations for GML 3.0, 3.1.1 and GML 3.2.
	 * 
	 * @param response
	 * @param layer
	 * @return simple features
	 */
	@SuppressWarnings("unchecked")
	public static FeatureCollection<SimpleFeatureType, SimpleFeature> parseSimpleFeatures(BufferedReader response, final WFSLayerStore layer) {
		Parser parser = null;
		if(Character.getNumericValue(layer.getGMLVersion().charAt(2)) == 2) { // 3.2
			log.debug("Using GML Parser 3.2");
			parser = GMLParser32.getParser(layer);
		} else { // 3.1.1, 3.0, 3.1 ...
			log.debug("Using GML Parser 3");
			parser = GMLParser3.getParser(layer);
		}

		Object obj = null;
		try {
			obj = parser.parse(response);
            if(obj instanceof FeatureCollection) {
                return (FeatureCollection<SimpleFeatureType, SimpleFeature>) obj;
            }
            throw new RuntimeException(ResultProcessor.ERROR_FEATURE_PARSING);
		} catch (Exception e) {
            if(!parseErrors(obj)) {
                log.error(e, "Features couldn't be parsed: - response: ", response, " obj: ", obj);
            }
            throw new RuntimeException(ResultProcessor.ERROR_FEATURE_PARSING);
		}
	}

	/**
	 * Parses WFS 1.0.0 and 1.1.0 XML errors
	 * 
	 * @param param
	 * @return <code>true</code> if error was handled; <code>false</code>
	 *         otherwise.
	 */
	@SuppressWarnings("unchecked")
	private static boolean parseErrors(Object param) {
        if(!(param instanceof Map)) {
            return false;
        }
        // can't handle these cases
        final Map<Object, Object> error = (Map<Object, Object>) param;
		if(error == null
                || !error.containsKey("Exception")
                || !error.containsKey("version")
                || !(error.get("Exception") instanceof Map)) {
            log.error("Layer configuration problem", error);
            return false;
        }
        // check that we are processing a known version
        final String version = (String) error.get("version");
        final boolean knownVersion = version.equals(VERSION_1_0_0) || version.equals(VERSION_1_1_0);
        if(!knownVersion) {
            log.error("UNHANDLED Version:", version);
            return false;
        }

        final Map<Object, Object> exception = (Map<Object, Object>) error.get("Exception");
        if(exception.containsKey("ExceptionText") && exception.containsKey("exceptionCode")) {
            log.error("Layer configuration problem [",
                    exception.get("exceptionCode"), "] ",
                    exception.get("ExceptionText"), error);
            return true;
        }
        return false;
	}

    /**
     * Constructs a filter for specific layer type
     *
     * Layer type is checked from layer's prefix before the first '_'
     *
     * @param layerId
     *
     * @return filter instance
     */
    public static WFSFilter constructFilter(String layerId) {
        String[] layer = layerId.split("_");

        if(layer.length > 1) {
            String filterClassName = PropertyUtil.getOptional(PROPERTY_PREFIX_EXT + layer[0]);
            try {
                final Class filterClass = Class.forName(filterClassName);
                return (WFSFilter) filterClass.newInstance();
            } catch (Exception e) {
                log.error(e, "Error constructing a filter for layer:", layerId, filterClassName);
                return null;
            }
        }

        // if not found or no prefix
        return new WFSFilter();
    }
}

