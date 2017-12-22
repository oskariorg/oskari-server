package fi.nls.oskari.wfs;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.wfs.util.XMLHelper;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.geotools.data.DataUtilities;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.resources.Classes;
import org.geotools.xml.Parser;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.referencing.operation.MathTransform;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
    
public class WFSParser {
    private static final Logger log = LogFactory.getLogger(WFSParser.class);

    private static final String NAMESPACE_URI_GML = "http://www.opengis.net/gml";
    private static final String ELEMENT_FEATURE_MEMBER = "featureMember";
    private static final String ATTRIBUTE_ID = "id";
    
    private static final String TYPE_STRING = "String";
    private static final String TYPE_INT = "Integer";
    private static final String TYPE_DOUBLE = "Double";
    private static final String TYPE_FEATURE = "Feature";
    private static final String TYPE_GEOMETRY = "Geometry";
    private static final String TYPE_ENVELOPE = "Envelope";
    private static final String TYPE_DATE = "Date";

    private static final String DEFAULT = "default";
    
    private Reader response = null;
    private WFSLayerStore layer = null;
    private String geomProperty = null;
    private Map<String, SimpleFeatureType> featureTypes = null;
    private Map<String, List<String>> propertyNames = null;
    
    /**
     * Constructor for custom WFS parser that creates FeatureCollection
     * 
     * @param response
     * @param layer
     */
    public WFSParser(Reader response, WFSLayerStore layer) {
    	this.response = response;
    	this.layer = layer;
    	this.geomProperty = layer.getGMLGeometryProperty().replaceAll("^[^_]*:", "");
    }
    
    /**
     * Configurable parsing
     * 
     * @return feature collection
     */
	public FeatureCollection<SimpleFeatureType, SimpleFeature> parse() {
        if(this.response == null || this.layer == null) {
        	return null;
        }
        
        // get feature types (for binding)
        WFSFeatureTypeParser featureTypeParser = new WFSFeatureTypeParser(this.layer.getFeatureType());
		this.featureTypes = featureTypeParser.parse();
		this.propertyNames = featureTypeParser.getProperties();

		// init
        List<SimpleFeature> list = new ArrayList<SimpleFeature>();
        SimpleFeature feature = null;

        // init xml parser
        StAXOMBuilder builder = XMLHelper.createBuilder(this.response);
        OMElement root = builder.getDocumentElement();

        // empty result - just root found
        if(root.getFirstElement() == null) {
            return DataUtilities.collection(list);
        }

        Iterator<?> features = root.getFirstElement().getChildrenWithName(new QName(this.layer.getFeatureNamespaceURI(), this.layer.getFeatureElement()));
        // if doesn't have collection of featureMembers - create it
        if(!features.hasNext()) {
        	List<OMElement> elementList = new LinkedList<OMElement>();
        	Iterator<?> featureMembers = root.getChildrenWithName(new QName(NAMESPACE_URI_GML, ELEMENT_FEATURE_MEMBER));
        	while(featureMembers.hasNext()) {
            	elementList.add(((OMElement) featureMembers.next()).getFirstElement());
        	}
        	features = elementList.iterator();
        }

        // go through feature elements
        while (features.hasNext()) {
            OMElement featureElement = (OMElement) features.next();
            feature = parseFeature(featureElement, DEFAULT);
            list.add(feature);
        }
		return DataUtilities.collection(list); 
	}
    
    /**
     * Parses feature from XML data
     * 
     * @param featureElement
     * @param typeName
     * @return feature
     */
    public SimpleFeature parseFeature(OMElement featureElement, String typeName) {
    	Map<String, Object> feature = new LinkedHashMap<String, Object>(); // should preserve order of addition
    	List<String> propNames = this.propertyNames.get(typeName);
        if(propNames == null) {
            log.warn("No property names");
            return null;
        }
    	String fid = parseFid(featureElement);
    	
    	// get the inner element if it has featureId - always?
    	if(fid == null) {
        	OMElement tmpFeatureElement = featureElement.getFirstElement();
	        if(tmpFeatureElement != null) {
	        	String tmpFid = parseFid(tmpFeatureElement);

	        	if(tmpFid != null) {
	        		featureElement = tmpFeatureElement;
	        		fid = tmpFid;
		        	//log.debug("using child element");
	        	}
        	}
        }
        //log.debug("feature id:", fid);
        
        boolean multiple = false;
        Geometry geom = null;
        AttributeType attr = null;
        
        for(String name : propNames) {
        	Iterator<?> properties = featureElement.getChildrenWithLocalName(name);
        	if(!properties.hasNext()) { // if empty - null
				feature.put(name, null);
        	}
        	while (properties.hasNext()) {
                //log.debug("property name:", name);
        		OMElement property = (OMElement) properties.next();
        		
        		// add to list
        		if(properties.hasNext()) {
        			multiple = true;
        		}

        		// get type
        		attr = featureTypes.get(typeName).getType(name);
        		if(attr == null) {
        			log.error("Type not found for '", name, "'");
        			return null;
        		}
        		String type = Classes.getShortName(attr.getBinding());
                
        		// parse geometry - only one for now - support for List?
        		if(geomProperty.equals(name) || 
        				type.contains(TYPE_GEOMETRY)) { // TODO: add all geometry catches here (POINT etc)
        			//log.debug("PARSING GEOMETRY", name);
        			geom = parseGeometry(property.toString());
        			if(geom != null) {
        				feature.put(name, geom);
        			} else {
       					return null;
       				}
        		} else {
        			setBinding(feature, featureTypes, type, name, property, multiple);
        		}
        	}
        }
        
        List<Object> values = new ArrayList<Object>(feature.values());
		return SimpleFeatureBuilder.build(featureTypes.get(typeName), values, fid);
    }
    
    /**
     * Parses fid from feature element
     * 
     * @param featureElement
     * @return fid
     */
    private String parseFid(OMElement featureElement) {
        String fid = null;
        OMAttribute fidAttribute = featureElement.getAttribute(new QName(NAMESPACE_URI_GML, ATTRIBUTE_ID));
        if(fidAttribute != null) {
        	fid = fidAttribute.getAttributeValue();
        }
        return fid;
    }
    
    /**
     * Parses geometry using geotools' GML parser
     *
     * @param xml
     * @return geometry
     */
    public Geometry parseGeometry(String xml) {
    	Parser parser = null;
		if(Character.getNumericValue(this.layer.getGMLVersion().charAt(2)) == 2) { // 3.2
			parser = GMLParser32.getParserWithoutSchemaLocator(); //GMLParser32.get();
		} else { // 3.1.1, 3.0, 3.1 ...
			parser = GMLParser3.getParserWithoutSchemaLocator(); //GMLParser3.get();
		}

        Object object = null;
		Geometry geometry = null;
		try {
            object = parser.parse(new InputSource(new StringReader(xml.toString())));
            if(object instanceof Geometry) {
                geometry = (Geometry) object;
            } else {
                log.error("Geometry parser failed", object, xml.toString());
            }
		} catch (IOException e) {
			log.error(e, "Geometry parser failed (IO)", xml.toString());
		} catch (SAXException e) {
			log.error(e, "Geometry parser failed (SAX)", xml.toString());
		} catch (ParserConfigurationException e) {
			log.error(e, "Geometry parser failed (configuration)", xml.toString());
		} catch (Exception e) {
			log.error(e, "Geometry parser failed", xml.toString());
		}
		return geometry;
    }
    
    /**
     * Binds type to properties
     * 
     * @param map
     * @param types
     * @param type
     * @param name
     * @param property
     */
    @SuppressWarnings("unchecked")
	public void setBinding(
			Map<String, Object> map, 
			Map<String, SimpleFeatureType> types, 
			String type, 
			String name,
			OMElement property, 
			boolean multiple) {
    	if(multiple) {
    		List<Object> tmpList = null;
    		if(map.containsKey(name)) {
    			tmpList = (List<Object>) map.get(name);
    		} else {
    			tmpList = new ArrayList<Object>();
    			map.put(name, tmpList);
    		}
    		tmpList.add(setType(type, name, property));
    	} else {
			map.put(name, setType(type, name, property));
    	}
    }

    /**
     * Set type to object
     * 
     * @param type
     * @param name
     * @param property
     * @return typed property object
     */
    public Object setType(String type, String name, OMElement property) {
    	if(type.contains(TYPE_STRING)) {
			return property.getText();
    	} else if(type.contains(TYPE_INT)) {
    		return Integer.parseInt(property.getText());
    	} else if(type.contains(TYPE_DOUBLE)) {
    		return Double.parseDouble(property.getText());
    	} else if(type.contains(TYPE_FEATURE)) {
    		return parseFeature(property, name);
    	} else if(type.contains(TYPE_ENVELOPE)) {
            // TODO: Envelope [ReferencedEnvelope] (131)
    		return property.getText();
    	} else if(type.contains(TYPE_DATE)) {
        	// TODO: Date (131)
    		return property.getText();
    	} else {
    		log.error("Class not defined in parser:", type);
    	}
    	return property.getText();
    }
    
    /**
     * Parses values to simpler format for JSON
     * 
     * Modifies given parameter.
     * 
     * @param values
     */
    public static void parseValuesForJSON(List<Object> values) {
		Object value = null;
		ReferencedEnvelope envelope = null;
		Point point = null;
		SimpleFeature feature = null;
		List<Object> list = null;
		for(int i = 0; i < values.size(); i++) {
			value = values.get(i);
			if(value != null) {
				//log.debug(value.getClass());
				if(value instanceof ReferencedEnvelope) {
					envelope = (ReferencedEnvelope) value;
					values.set(i, 
							envelope.getMinX() + ":" +
							envelope.getMaxX() + ", " +
							envelope.getMinY() + ":" +
							envelope.getMaxY()
					);
				} else if(value instanceof Point) {
					point = (Point) value;
					values.set(i, 
							point.getX() + ", " +
							point.getY()
					);
				} else if(value instanceof SimpleFeature) {
					feature = (SimpleFeature) value;
					values.set(i, parseInnerFeature(feature) );
				} else if(value instanceof List) {
					list = (List<Object>) value;
					parseValuesForJSON(list);
					values.set(i, list);
				}
			}
		}
	}
	
    /**
     * Parses SimpleFeature typed object
     * 
     * @param feature
     * @return feature as a HashMap
     */
	private static Map<String, Object> parseInnerFeature(SimpleFeature feature) {
		Map<String, Object> result = new HashMap<String, Object>();

		for(Property prop : feature.getProperties()) {
			String field = prop.getName().toString();
			Object value = feature.getAttribute(field);
			// TODO: parse value again [recursive] (multi values possible)
			if(value != null) { // hide null properties
				result.put(field, value);
			}
		}
		
		return result;
	}

    /**
     * Gets feature's geometry and transforms it if needed
     *
     * @param feature
     * @param geometryProperty
     * @param transform
     * @return geometry
     */
    public static Geometry getFeatureGeometry(SimpleFeature feature, String geometryProperty, MathTransform transform) {
        if(geometryProperty == null || geometryProperty == "")  {
            return null;
        }

        if(geometryProperty.contains(":")) {
            geometryProperty = geometryProperty.replaceAll("^[^_]*:", "");
        }

        Object obj = feature.getAttribute(geometryProperty);
        if(obj instanceof Geometry) {
            Geometry geometry = (Geometry) feature.getAttribute(geometryProperty);
            if(transform != null) {
                try {
                    geometry = JTS.transform(geometry, transform);
                    feature.setAttribute(geometryProperty, geometry); // set transformed in feature
                } catch (Exception e) {
                    log.error(e, "Transform feature's geometry to user's CRS failed");
                }
            }
            return geometry;
        }
        log.error("feature has no geometry - id: ", feature.getID());
        return null;
    }

    /**
     * Gets geometry's center point
     *
     * @param geometry
     * @return point
     */
    public static Point getGeometryCenter(Geometry geometry) {
        if(geometry == null || geometry.getCoordinates().length == 0) {
            return null;
        }

        return geometry.getCentroid();
    }

    /**
     * Creates Geotools' feature collection from given parameters (types and features)
     *
     * @param types
     * @param features
     * @return simple features
     */
    public static FeatureCollection<SimpleFeatureType, SimpleFeature> dataToSimpleFeatures(String types, List<List<Object>> features) {
        DefaultFeatureCollection featureCollection = null;
        SimpleFeatureType type = null;
        try {
            // properties
            type = DataUtilities.createType("custom", types);
        } catch (SchemaException e) {
            log.error(e, "Custom FeatureType create failed");
        }

        featureCollection = new DefaultFeatureCollection();
        // values
        for(List<Object> values : features) {
            featureCollection.add( SimpleFeatureBuilder.build(
                    type,
                    values.toArray(),
                    null
            )
            );
        }
        return (FeatureCollection<SimpleFeatureType, SimpleFeature>) featureCollection;
    }
}
