package fi.nls.oskari.domain.map.wfs;

import java.util.HashMap;
import java.util.Map;

public enum FeatureParameterType {
	XSD_INTEGER("xsd:integer"), XSD_STRING("xsd:string"), XSD_DATETIME("xsd:dateTime"), 
	XSD_CODE(""), GML_GEOMETRY_PROPERTY_TYPE("gml:GeometryPropertyType"), 
	GML_MULTI_SURFACE_PROPERTY_TYPE("gml:MultiSurfacePropertyType"), 
	GML_POINT_PROPERTY_TYPE("gml:PointPropertyType");
	
	private String xmlAttributeValue;
	
	// KEY: xml attribute value; VALUE: XsdElementType
	private static final Map<String, FeatureParameterType> featureParameterTypeMap = 
		new HashMap<String, FeatureParameterType>();
	
	private FeatureParameterType(String xmlAttributeValue) {
		this.xmlAttributeValue = xmlAttributeValue;
	}
	
	public String getXmlAttributeValue() {
		return xmlAttributeValue;
	}
	
	public static FeatureParameterType getFeatureParameterType(String xmlAttributeValue) {
		FeatureParameterType featureParameterType = featureParameterTypeMap.get(xmlAttributeValue);
		
		if (featureParameterType == null) {
			featureParameterType = XSD_CODE;
		}
		
		return featureParameterType;
	}
	
	static {
		featureParameterTypeMap.put("xsd:integer", XSD_INTEGER);
		featureParameterTypeMap.put("xsd:string", XSD_STRING);
		featureParameterTypeMap.put("xsd:dateTime", XSD_DATETIME);
		featureParameterTypeMap.put("gml:GeometryPropertyType", GML_GEOMETRY_PROPERTY_TYPE);
		featureParameterTypeMap.put("gml:MultiSurfacePropertyType", GML_MULTI_SURFACE_PROPERTY_TYPE);
		featureParameterTypeMap.put("gml:PointPropertyType", GML_POINT_PROPERTY_TYPE);
	}
}
