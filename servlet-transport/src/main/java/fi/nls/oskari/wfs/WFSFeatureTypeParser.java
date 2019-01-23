package fi.nls.oskari.wfs;

import com.vividsolutions.jts.geom.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.geotools.feature.NameImpl;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.AttributeDescriptorImpl;
import org.geotools.feature.type.AttributeTypeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.json.JSONObject;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.math.BigInteger;
import java.util.*;

public class WFSFeatureTypeParser {
    private static final Logger log = LogFactory.getLogger(WFSFeatureTypeParser.class);
    
    @SuppressWarnings("rawtypes")
	private static Map<String, Class> typeMap = new HashMap<String, Class>();
    static {
        typeMap.put("Object", Object.class);
        
        typeMap.put("String", String.class);
        typeMap.put("string", String.class);
        typeMap.put("\"\"", String.class);

        typeMap.put("Integer", Integer.class);
        typeMap.put("int", Integer.class);
        typeMap.put("0", Integer.class);

        typeMap.put("BigInteger", BigInteger.class);

        typeMap.put("Double", Double.class);
        typeMap.put("double", Double.class);
        typeMap.put("0.0", Double.class);

        typeMap.put("Float", Float.class);
        typeMap.put("float", Float.class);
        typeMap.put("0.0f", Float.class);
        
        typeMap.put("Boolean", Boolean.class);
        typeMap.put("true", Boolean.class);
        typeMap.put("false", Boolean.class);
        
        typeMap.put("UUID", UUID.class);
        
        typeMap.put("Geometry", Geometry.class);
        typeMap.put("Point", Point.class);
        typeMap.put("LineString", LineString.class);
        typeMap.put("Polygon", Polygon.class);
        typeMap.put("MultiPoint", MultiPoint.class);
        typeMap.put("MultiLineString", MultiLineString.class);
        typeMap.put("MultiPolygon", MultiPolygon.class);
        typeMap.put("Envelope", Envelope.class);
        
        typeMap.put("GeometryCollection", GeometryCollection.class);
        
        typeMap.put("Date", Date.class);
        typeMap.put("Timestamp", Date.class);
        
        typeMap.put("Feature", Feature.class);
        
    }
    
	private Map<String, String> typeSpecs = null;
	private Map<String, List<String>> properties = null;

    /**
     * Constructor for custom WFS FeatureType parser that creates a set of SimpleFeatureTypes
     * 
     * @param typeSpecs
     */
	public WFSFeatureTypeParser(Map<String, String> typeSpecs) {
		this.typeSpecs = typeSpecs;
	}

    public WFSFeatureTypeParser(JSONObject typeSpecs) {
        this.typeSpecs = JSONHelper.getObjectAsMap(typeSpecs);
    }

	/**
	 * Gets properties
	 * 
	 * @return properties
	 */
	public Map<String, List<String>> getProperties() {
		if(this.properties == null) {
			this.parse();
		}
		return this.properties;
	}

    /**
     * Configurable parsing
     * 
     * @return feature types set
     */
	public Map<String, SimpleFeatureType> parse() {
		if(this.typeSpecs == null) {
            log.debug("Parsing failed because type specifications were not defined");
            return null;
		}
		
    	Map<String, SimpleFeatureType> types = new HashMap<String, SimpleFeatureType>();
    	this.properties = new HashMap<String, List<String>>();
		
		for (Map.Entry<String, String> entry : typeSpecs.entrySet())
		{
			try {
				SimpleFeatureType type = createType(entry.getKey(), entry.getValue());
				types.put(entry.getKey(), type);
				parseProperties(entry.getKey(), entry.getValue());
			} catch (SchemaException e) {
				log.error(e, "Should always be valid schema");
			}
		}
		
		return types;
	}
	
	/**
	 * Creates feature type from the type specification
	 * 
	 * @param typeName
	 * @param typeSpec
	 * @return feature type
	 * @throws SchemaException
	 */
	public SimpleFeatureType createType(String typeName, String typeSpec) throws SchemaException {
		// modified copy of geotools' DataUtilities createType
		int split = typeName.lastIndexOf('.');
		String namespace = (split == -1) ? null : typeName.substring(0, split);
		String name = (split == -1) ? typeName : typeName.substring(split + 1);

		return createType(namespace, name, typeSpec);
    }
	
	/**
	 * Creates feature type from the type specification
	 * 
	 * @param namespace
	 * @param name
	 * @param typeSpec
	 * @return feature type
	 * @throws SchemaException
	 */
    public SimpleFeatureType createType(String namespace, String name, String typeSpec) throws SchemaException {
        // modified copy of geotools' DataUtilities createType
        SimpleFeatureType featype = null;
        try {
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.setName(name);
            builder.setNamespaceURI(namespace);

            String[] types = typeSpec.split(",");

            AttributeDescriptor attributeType;
            builder.setCRS(null); // not interested in warnings from this simple method

            for (int i = 0; i < types.length; i++) {
                boolean defaultGeometry = types[i].startsWith("*");
                if (types[i].startsWith("*")) {
                    types[i] = types[i].substring(1);
                }

                attributeType = createAttribute(types[i]);
                builder.add(attributeType);

                if (defaultGeometry) {
                    builder.setDefaultGeometry(attributeType.getLocalName());
                }
            }

            featype = builder.buildFeatureType();
        } catch (Exception ee) {
            log.error(ee, "Parsing failed for Simple feature type: ", name);
        }
        return featype;
    }

    /**
	 * Creates attribute descriptor from the type specification
	 * 
	 * @param typeSpec
	 * @return attribute descriptor
	 * @throws SchemaException
	 */
    public AttributeDescriptor createAttribute(String typeSpec) throws SchemaException {
		// modified copy of geotools' DataUtilities createAttribute (removed hint)
    	int split = typeSpec.indexOf(":");

    	String name;
    	String type;

    	// parse name, type & hint
    	if (split == -1) {
    		name = typeSpec;
    		type = "String";
    	} else {
    		name = typeSpec.substring(0, split);
    		type = typeSpec.substring(split + 1);
    	}

        try {
        	boolean nillable = true;
        	CoordinateReferenceSystem crs = null;

            Class<?> clazz = type(type);
            
            if (Geometry.class.isAssignableFrom(clazz)) {
                @SuppressWarnings("unchecked")
                GeometryType at = new GeometryTypeImpl(new NameImpl(name), clazz, crs, false,
                        false, Collections.EMPTY_LIST, null, null);
                return new GeometryDescriptorImpl(at, new NameImpl(name), 0, 1, nillable, null);
            } else {
                @SuppressWarnings("unchecked")
				AttributeType at = new AttributeTypeImpl(new NameImpl(name), clazz, false, false,
                        Collections.EMPTY_LIST, null, null);
                return new AttributeDescriptorImpl(at, new NameImpl(name), 0, 1, nillable, null);
            }
        } catch (ClassNotFoundException e) {
            throw new SchemaException("Could not type " + name + " as:" + type, e);
        }
    }

    /**
     * Gets the class of the type name
     * 
     * @param typeName
     * @return class
     * @throws ClassNotFoundException
     */
    public static Class<?> type(String typeName) throws ClassNotFoundException {
        if (typeMap.containsKey(typeName)) {
            return typeMap.get(typeName);
        } else if(typeName.contains(".")) {
        	return Class.forName(typeName);
        }
        return String.class;
    }
    
    /**
     * Parses properties from type specification
     * 
     * @param typeSpec
     */
    private void parseProperties(String typeName, String typeSpec) {
		String[] types = typeSpec.split(",");
		List<String> props = new ArrayList<String>();
		for (int i = 0; i < types.length; i++) {
			if (types[i].startsWith("*")) {
				types[i] = types[i].substring(1);
			}
			
	    	int split = types[i].indexOf(":");
	    	props.add(types[i].substring(0, split));
		}
		this.properties.put(typeName, props);
    }
}
