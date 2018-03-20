package fi.nls.oskari.arcgis;

import com.vividsolutions.jts.geom.Coordinate;
import fi.nls.oskari.arcgis.pojo.ArcGisFeature;
import fi.nls.oskari.arcgis.pojo.ArcGisLayerStore;
import fi.nls.oskari.arcgis.pojo.ArcGisProperty;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.Location;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.pojo.WFSCustomStyleStore;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.work.JobType;
import org.apache.commons.lang.StringUtils;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * WFS request creators and response parsers
 */
public class ArcGisCommunicator {
    
	private static final Logger log = LogFactory.getLogger(ArcGisCommunicator.class);			

	/**
	 * Creates request payload for WFS 1.1.0 (default request type)
	 * 
	 * @param layer
	 * @param session
     * @param bounds
	 * @return xml payload
	 */
	@SuppressWarnings("unchecked")
	public static String createQueryRequestPayload(final JobType type,
			final WFSLayerStore layer, final SessionStore session,
			final List<Double> bounds,
			final String token) {
		
		HashMap<String, String> data = new HashMap<String, String>();
		
		ArcGisFilter arcGisFilter = new ArcGisFilter();
        HashMap<String, String> filter = arcGisFilter.create(type, layer, session, bounds);
        
        if (filter != null) {
        	data.putAll(filter);
        }
        else {
        	data.put("geometry", "107250,6638500,701500,7616750");
        	data.put("inSR", session.getLocation().getSrs());
    		data.put("geometryType", "esriGeometryEnvelope");
    		data.put("spatialRel", "esriSpatialRelIntersects");
        }
				
		data.put("returnGeometry", "true");
		data.put("returnIdsOnly", "false");
		data.put("returnCountOnly", "false");
		data.put("returnZ", "false");
		data.put("returnM", "false");
		data.put("returnDistinctValues", "false");
		data.put("outFields", "*");
		data.put("outSR", layer.getSRSName());    
		data.put("f", "json");
		data.put("token", token);			
		
		return mapToString(data);		        
		
//		OMFactory factory = OMAbstractFactory.getOMFactory();
//
//		// namespaces
//		OMNamespace xsi = factory.createOMNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
//		OMNamespace wfs = factory.createOMNamespace("http://www.opengis.net/wfs", "wfs");
//		// root element
//		OMElement root = factory.createOMElement("GetFeature", wfs);
//		OMAttribute schemaLocation = factory.createOMAttribute("schemaLocation", 
//				xsi,
//				"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/" 
//				+ layer.getWFSVersion() + "/wfs.xsd");
//        // quite ugly, but only way to handle if namespace is just used in TEXT
//        OMAttribute layerNs = factory.createOMAttribute("xmlns:" + layer.getFeatureNamespace(), null, layer.getFeatureNamespaceURI());
//		OMAttribute version = factory.createOMAttribute("version", null, layer.getWFSVersion());
//		OMAttribute service = factory.createOMAttribute("service", null, "WFS");
//		OMAttribute maxFeatures = factory.createOMAttribute("maxFeatures", null, String.valueOf(layer.getMaxFeatures()));
//
//        // if geometry property has different namespace than the featureNamespace
//        String[] split = layer.getGMLGeometryProperty().split(":");
//        if(split.length >= 2 && !split[0].equals(layer.getFeatureNamespace())) {
//            if(layer.getGeometryNamespaceURI() == null) {
//                log.error("No geometry namespace URI defined");
//                return null;
//            }
//            OMAttribute geomNs = factory.createOMAttribute("xmlns:" + split[0], null, layer.getGeometryNamespaceURI());
//            root.addAttribute(geomNs);
//        }
//
//        if (layer.getOutputFormat() != null) {
//            OMAttribute outputFormat = factory.createOMAttribute("outputFormat", null, layer.getOutputFormat());
//            root.addAttribute(outputFormat);
//        }
//
//        root.addAttribute(layerNs);
//		root.addAttribute(schemaLocation);
//		root.addAttribute(version);
//		root.addAttribute(service);
//		root.addAttribute(maxFeatures);
//		// query
//		try {
//            OMElement query = factory.createOMElement("Query", wfs);
//            OMAttribute typeName = factory.createOMAttribute("typeName", null,
//                    layer.getFeatureNamespace() + ":" + layer.getFeatureElement());
//            OMAttribute srsName = factory.createOMAttribute("srsName", null, layer.getSRSName());
//            query.addAttribute(typeName);
//            query.addAttribute(srsName);
//            root.addChild(query);
//
//
//            List<String> selectedProperties = layer.getSelectedFeatureParams(session.getLanguage());
//            if(selectedProperties != null) {
//                selectedProperties = (ArrayList<String>) ((ArrayList<String>) selectedProperties).clone();
//            }
//            if(!layer.isGetFeatureInfo()) {
//                if(layer.isGetMapTiles()) { // only geometry
//                    OMElement property = factory.createOMElement("PropertyName", wfs);
//                    property.setText(layer.getGMLGeometryProperty());
//                    query.addChild(property);
//                }
//            } else if(selectedProperties == null || selectedProperties.isEmpty()) {
//                // empty selection, and features wanted - give all (also map tiles
//            } else {
//                // Commented out since we always want the geometry field in queries
//                //if(layer.isGetMapTiles()) {
//                selectedProperties.add(layer.getGMLGeometryProperty());
//                //}
//            }
//            // loop for all properties
//      /*      if(selectedProperties != null) {
//                for(String prop : selectedProperties) {
//                    OMElement property = factory.createOMElement("PropertyName", wfs);
//                    if(!prop.contains(":")) {
//                        property.setText(layer.getFeatureNamespace() + ":" + prop);
//                    } else {
//                        property.setText(prop);
//                    }
//                    query.addChild(property);
//                }
//            }  -- eliminated because of GetFeature fails when property name is not valid or not for all features */
//
//            // load filter
//            WFSFilter wfsFilter = constructFilter(layer.getLayerId());
//            String filterStr = wfsFilter.create(type, layer, session, bounds, transform);
//            if(filterStr != null) {
//                StAXOMBuilder staxOMBuilder = XMLHelper.createBuilder(filterStr);
//                OMElement filter = staxOMBuilder.getDocumentElement();
//                query.addChild(filter);
//            }
//		}
//		catch (Exception e){
//		    log.error(e, "Failed to create payload");
//		}
//
//		return root.toString();
	}
	
	public static String createIdentifyRequestPayload(
			final List<ArcGisLayerStore> layers,
			final SessionStore session,
			final List<Double> bounds,
			final String token) 
	{	
		HashMap<String, String> data = new HashMap<String, String>();
		
		//create point filter
		Coordinate coordinate = session.getMapClick();
    	data.put("geometry", coordinate.x + "," + coordinate.y);
    	data.put("sr", session.getLocation().getSrs());
		data.put("geometryType", "esriGeometryPoint");
		
		Location location;
		if (bounds != null)
		{
			location = new Location(session.getLocation().getSrs());
			location.setBbox(bounds);
		}
		else {
			location = session.getLocation();
		}		
		ReferencedEnvelope envelope = location.getEnvelope();
		
		data.put("tolerance", "2");
		data.put("mapExtent", envelope.getMinX() + "," + envelope.getMinY() + "," + envelope.getMaxX() + "," + envelope.getMaxY());
		data.put("imageDisplay", "800,600,96");
		
		List<String> layerIds = new ArrayList<String>();
		for (ArcGisLayerStore layer : layers) {
			layerIds.add(layer.getIdStr());
		}
		
		data.put("layers", "all:" + StringUtils.join(layerIds, ","));
		
		data.put("returnGeometry", "true");
		data.put("returnZ", "false");
		data.put("returnM", "false");

		data.put("f", "json");
		data.put("token", token);			
		
		return mapToString(data);		        		
	}
	
	public static ArrayList<ArcGisFeature> parseFeatures(List<Reader> responses, final WFSLayerStore layer) {
		ArrayList<ArcGisFeature> result = new ArrayList<ArcGisFeature>();
		int count = responses.size();
		
		for (Reader response : responses) {
			ArrayList<ArcGisFeature> features = parseFeatures(response, layer);
			if (features == null) {
				if (count == 1) {
					return null;
				}					
			} else {
				result.addAll(features);
			}				
		}
		
		return result;
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
	public static ArrayList<ArcGisFeature> parseFeatures(Reader response, final WFSLayerStore layer) {
		ArrayList<ArcGisFeature> result = new ArrayList<ArcGisFeature>();
		
		JSONParser parser = new JSONParser();
		JSONObject json;
		try {
			json = (JSONObject) parser.parse(response);
			
			if (json.containsKey("error")) {
				log.error("Error during parsing features. Server returned error.", json.get("error"));
				return null;
			}
			
			JSONArray featuresArray;
			if (json.containsKey("features")) {
				featuresArray = (JSONArray) json.get("features");
			}
			else if (json.containsKey("results")){
				featuresArray = (JSONArray) json.get("results");
			}
			else {
				log.error("Error during parsing features. Unexpected json.", json);
				return null;
			}
								
			for (Object featureItem : featuresArray) {
				ArcGisFeature item = ArcGisFeature.setJSON((JSONObject) featureItem);
				
				if (item.getGeometry() == null) {
					log.warn("There is no geometry. Skipping feature");
				}
				else {
					result.add(item);	
				}				
			}		
		} catch (IOException e) {
			log.error(e, "Error during parsing features");
			return null;
		} catch (ParseException e) {
			log.error(e, "Error during parsing features");
			return null;
		}	
		
		return result;
		
//		
//		Parser parser = null;
//		if(Character.getNumericValue(layer.getGMLVersion().charAt(2)) == 2) { // 3.2
//			log.debug("Using GML Parser 3.2");
//			parser = GMLParser32.getParser(layer);
//		} else { // 3.1.1, 3.0, 3.1 ...
//			log.debug("Using GML Parser 3");
//			parser = GMLParser3.getParser(layer);
//		}		
//				
//		boolean errorHandled = false;
//		Object obj = null;
//		FeatureCollection<SimpleFeatureType, SimpleFeature> features = null;
//		
//		
//		
//		try {
//			obj = parser.parse(response);
//			if(obj instanceof Map) {
//				if(features == null) {
//					errorHandled = parseErrors((Map<Object, Object>) obj);	
//				}
//			}
//			else {
//				features = (FeatureCollection<SimpleFeatureType, SimpleFeature>) obj;
//			}
//		} catch (Exception e) {
//            e.printStackTrace();
//			if(!errorHandled)
//				log.error(e, "Features couldn't be parsed");
//		}
//		
//		return features;
	}

//	/**
//	 * Parses WFS 1.0.0 and 1.1.0 XML errors
//	 * 
//	 * @param error
//	 * @return <code>true</code> if error was handled; <code>false</code>
//	 *         otherwise.
//	 */
//	@SuppressWarnings("unchecked")
//	private static boolean parseErrors(Map<Object, Object> error) {
//		Map<Object, Object> exception = null;
//		String version = null;
//		boolean handled = false;
//		
//		
//		if(error != null && error.containsKey("Exception") && error.get("Exception") instanceof Map) {
//			exception = (Map<Object, Object>) error.get("Exception");
//			//log.debug("Exception:", exception);
//			
//			if(error.containsKey("version")) {
//				version = (String) error.get("version");
//
//				if(version.equals(VERSION_1_0_0) || version.equals(VERSION_1_1_0)) {
//					if(exception.containsKey("ExceptionText") && exception.containsKey("exceptionCode")) {
//						log.error("Layer configuration problem [",
//								exception.get("exceptionCode"), "] ", 
//								exception.get("ExceptionText"), error); 
//						handled = true;	
//					}
//				} else {
//                    log.error("UNHANDLED Version:", version);
//                }
//				
//			}
//		}
//		
//		if(!handled)
//			log.error("Layer configuration problem", error);
//		
//		return handled;
//	}
//
//    /**
//     * Constructs a filter for specific layer type
//     *
//     * Layer type is checked from layer's prefix before the first '_'
//     *
//     * @param layerId
//     *
//     * @return filter instance
//     */
//    public static WFSFilter constructFilter(String layerId) {
//        String[] layer = layerId.split("_");
//
//        if(layer.length > 1) {
//            String filterClassName = PropertyUtil.getOptional(PROPERTY_PREFIX_EXT + layer[0]);
//            try {
//                final Class filterClass = Class.forName(filterClassName);
//                return (WFSFilter) filterClass.newInstance();
//            } catch (Exception e) {
//                log.error(e, "Error constructing a filter for layer:", layerId, filterClassName);
//                return null;
//            }
//        }
//
//        // if not found or no prefix
//        return new WFSFilter();
//    }

	public static String createImageRequestPayload(WFSLayerStore layer,
			ArcGisLayerStore arcgisLayer,
			List<ArcGisLayerStore> arcgisLayers,
			Rectangle screenArea, ReferencedEnvelope bounds,
			String mapSrs,
			String token) {
		
		List<String> ids = new ArrayList<String>();
		for (ArcGisLayerStore arcGisLayerStore : arcgisLayers) {
			ids.add(arcGisLayerStore.getIdStr());
		}

		HashMap<String, String> data = new HashMap<String, String>();
		data.put("layers", "show:" + StringUtils.join(ids, ","));
		data.put("TRANSPARENT", "true");
		data.put("FORMAT", "png");
		data.put("BBOX", bounds.getMinX()+ "," + bounds.getMinY() + "," + bounds.getMaxX() + "," + bounds.getMaxY());
		data.put("SIZE", screenArea.width + "," + screenArea.height);
		data.put("IMAGESR", mapSrs);
		data.put("BBOXSR", mapSrs);
		data.put("f", "image");
		data.put("token", token);
		
		return mapToString(data);
	}
	
	@SuppressWarnings("unchecked")
	public static String createHighlightStyleRequestPayload(WFSLayerStore layer, 
			ArcGisLayerStore arcgisLayer,
			List<ArcGisLayerStore> arcgisLayers,
			ArrayList<ArcGisFeature> features, 
			WFSCustomStyleStore highlightStyle) {
		HashMap<String, String> data = new HashMap<String, String>();				
		
		JSONArray array = new JSONArray();
		
		int i = 0;
		for (ArcGisLayerStore layerItem : arcgisLayers) {
			JSONObject json = new JSONObject();
			json.put("id", 555 + i);
			JSONObject source = new JSONObject();
			source.put("type", "mapLayer");
			source.put("mapLayerId", layerItem.getIdStr());
			json.put("source", source);
			
			JSONObject drawingInfo = new JSONObject();
			JSONObject renderer = new JSONObject();		
			renderer.put("type", "uniqueValue");
			renderer.put("field1", ArcGisProperty.ID_PROPERTY);
			renderer.put("field2", null);
			renderer.put("field3", null);
			renderer.put("fieldDelimiter", ",");
			renderer.put("defaultLabel", "yncepynce");
			
			JSONObject highlightSymbol = ArcGisStyleMapper.mapStyleToSymbol(highlightStyle, layerItem.getGeometryType());
			JSONArray uniqueValuesInfos = new JSONArray();
			
			for (ArcGisFeature feature : features) 
			{
				JSONObject uniqueValueItem = new JSONObject();
				try {
					uniqueValueItem.put("value", feature.GetId());
				} catch (Exception e) {
					log.warn(e, "Cannot get id");
					continue;
				}
				uniqueValueItem.put("label", "");
				uniqueValueItem.put("description", "");
				uniqueValueItem.put("symbol", highlightSymbol);
				
				uniqueValuesInfos.add(uniqueValueItem);
			}
			
			renderer.put("uniqueValueInfos", uniqueValuesInfos);		
			drawingInfo.put("renderer", renderer);
			json.put("drawingInfo", drawingInfo);
			
			array.add(json);
			i++;
		}				
		
		data.put("dynamicLayers", array.toJSONString());
		
		return mapToString(data);
	}

	@SuppressWarnings("unchecked")
	public static String createStyleRequestPayload(WFSLayerStore layer,
			ArcGisLayerStore arcgisLayer,
			List<ArcGisLayerStore> arcgisLayers,
			WFSCustomStyleStore customStyle) {
		HashMap<String, String> data = new HashMap<String, String>();		
		
		JSONArray array = new JSONArray();
		int i = 0;
		for (ArcGisLayerStore layerItem : arcgisLayers) {
			JSONObject json = new JSONObject();
			json.put("id", 555 + i);
			JSONObject source = new JSONObject();
			source.put("type", "mapLayer");
			source.put("mapLayerId", layerItem.getIdStr());
			json.put("source", source);
			
			JSONObject drawingInfo = new JSONObject();
			JSONObject renderer = new JSONObject();		
			renderer.put("type", "simple");
			renderer.put("label", "yncepynce");
			renderer.put("description", "");
			JSONObject symbol = ArcGisStyleMapper.mapStyleToSymbol(customStyle, layerItem.getGeometryType());		

			renderer.put("symbol", symbol);
			drawingInfo.put("renderer", renderer);
			json.put("drawingInfo", drawingInfo);
			array.add(json);
			i++;
		}	
		
		data.put("dynamicLayers", array.toJSONString());
		
		return mapToString(data);
	}
	

	
	private static String mapToString(HashMap<String, String> data) {
		StringWriter buffer = new StringWriter();
		
		//TODO: Url encoder
		
		for (String key : data.keySet()) {
			String value = data.get(key);
			buffer.write("&" + key + "=" + value);
		}
		
		return buffer.toString();
	}
}

