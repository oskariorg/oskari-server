package fi.mml.portti.service.ogc.executor;

import org.geotools.feature.FeatureCollection;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class WFSResponseCapsule {	
	FeatureCollection<SimpleFeatureType, SimpleFeature> features;
	JSONObject jsonObject;
	String xmlData;
	
	public FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatures() {
		return features;
	}
	public void setFeatures(
			FeatureCollection<SimpleFeatureType, SimpleFeature> features) {
		this.features = features;
	}
	public JSONObject getJsonObject() {
		return jsonObject;
	}
	public void setJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}
	
	public String getXmlData() {
		return xmlData;
	}
	public void setXmlData(String xmlData) {
		this.xmlData = xmlData;
	}
	
	
}
