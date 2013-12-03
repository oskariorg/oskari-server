package fi.nls.oskari.domain.map.wfs;

import java.util.List;

import javax.xml.namespace.QName;

public class FeatureParameter {
	private int id;
	private FeatureType featureType;
	private QName qname;
	private FeatureParameterType featureParameterType;
	private String xmlType;
	private String rawValue;
	private int converterType;
	private Converter converter;
	private List<FeatureParameter> childFeatureParameters;
	private String description;
	private int parentId;
	
	/**
	 * Is this feature paremeter of abstract type,
	 * that will need special handling.
	 * 
	 */
	private boolean abstractSchemaType;
	
	/**
	 * Is this feature parameter special one,
	 * that contains actual location infomation.
	 */
	private boolean bboxQueryParameter;
	
	
	public boolean isAbstractSchemaType() {
		return abstractSchemaType;
	}

	public void setAbstractSchemaType(boolean abstractSchemaType) {
		this.abstractSchemaType = abstractSchemaType;
	}

	public boolean isBboxQueryParameter() {
		return bboxQueryParameter;
	}

	public void setBboxQueryParameter(boolean bboxQueryParameter) {
		this.bboxQueryParameter = bboxQueryParameter;
	}
	
	@Override
	public String toString() {
		return "FeatureParameter [abstractSchemaType=" + abstractSchemaType
				+ ", bboxQueryParameter=" + bboxQueryParameter + ", converter="
				+ converter + ", converterType=" + converterType
				+ ", description=" + description + ", featureParameterType="
				+ featureParameterType + ", featureType=" + featureType
				+ ", id=" + id + ", parentId=" + parentId + ", qname=" + qname
				+ ", rawValue=" + rawValue + ", xmlType=" + xmlType + "]";
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public FeatureType getFeatureType() {
		return featureType;
	}
	public void setFeatureType(FeatureType featureType) {
		this.featureType = featureType;
	}
	public QName getQname() {
		if (qname == null) {
			qname = new QName("", "");
		}
		return qname;
	}
	public void setQname(QName qname) {
		this.qname = qname;
	}
	public FeatureParameterType getFeatureParameterType() {
		return featureParameterType;
	}
	public void setFeatureParameterType(FeatureParameterType featureParameterType) {
		this.featureParameterType = featureParameterType;
	}	
	public String getXmlType() {
		if (xmlType == null) {
			return "";
		}
		return xmlType;
	}
	public void setXmlType(String xmlType) {
		this.xmlType = xmlType;
	}
	public String getRawValue() {
		if (rawValue == null) {
			return "";
		}
		return rawValue;
	}
	public void setRawValue(String rawValue) {
		this.rawValue = rawValue;
	}
	public String getConvertedValue() {
		if (converter == null) {
			converter = Converter.getConverter(converterType);
		}
		
		return converter.getConvertedValue();
	}
	public int getConverterType() {
		return converterType;
	}
	public void setConverterType(int converterType) {
		this.converterType = converterType;
	}
	public Converter getConverter() {
		if (converter == null) {
			converter = Converter.getConverter(converterType);
		}
		return converter;
	}
	public List<FeatureParameter> getChildFeatureParameters() {
		return childFeatureParameters;
	}
	public void setChildFeatureParameters(
			List<FeatureParameter> childFeatureParameters) {
		this.childFeatureParameters = childFeatureParameters;
	}
	public String getDescription() {
		if (description == null) {
			return "";
		}
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
}
