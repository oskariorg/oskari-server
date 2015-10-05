package fi.nls.oskari.arcgis.pojo;

import com.esri.core.geometry.Geometry.Type;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.OperatorExportToWkb;
import com.esri.core.geometry.OperatorImportFromJson;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;

public class ArcGisFeature {
	private static final Logger log = LogFactory.getLogger(ArcGisFeature.class);
	
	private JSONObject json;
	
	private LinkedHashMap<String, ArcGisProperty> properties;
	private Geometry geometry;
	
	public ArcGisFeature() {
		properties = new LinkedHashMap<String, ArcGisProperty>();
	}	
	
	public Collection<ArcGisProperty> getProperties() {
		return properties.values();
	}
	
	public void setProperties(Collection<ArcGisProperty> properties) {
		this.properties.clear();
		
		if (properties != null) {
			for (ArcGisProperty property : properties) {
				this.properties.put(property.getName(), property);
			}
		}								
	}		
	
	public Set<String> getPropertiesNames() {
		return properties.keySet();
	}
	
	public String GetId() {
		Object value = getPropertyValue(ArcGisProperty.ID_PROPERTY);
		return value != null ? value.toString() : null;
	}
	
	public Geometry getGeometry() {
		return geometry;
	}
	
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
	
//	@JsonIgnore
//	public JSONObject getJSON() {
//		return json;
//	}
	
	@JsonIgnore
	public Object getPropertyValue(String key) {
		ArcGisProperty prop = this.properties.get(key);
		
		return prop != null ? prop.getValue() : null;
	}

	public static ArcGisFeature setJSON(JSONObject json) {
		OperatorImportFromJson Importer = OperatorImportFromJson.local();
		OperatorExportToWkb Exporter = OperatorExportToWkb.local();
		WKBReader Reader = new WKBReader();
		
		ArcGisFeature result = new ArcGisFeature();
		
		JSONObject attributes = (JSONObject) json.get("attributes");
		if (attributes != null) {
			ArrayList<ArcGisProperty> properties = new ArrayList<ArcGisProperty>();
			
			for (Object name : attributes.keySet()) {
				ArcGisProperty prop = new ArcGisProperty();
				prop.setName(name.toString());
				prop.setValue(attributes.get(name));
				
				properties.add(prop);
			}
			
			result.setProperties(properties);
		}
		
		JSONObject geometry = (JSONObject) json.get("geometry");
		if (geometry != null) {
			MapGeometry geometryObj = null;
			try {
				geometryObj = Importer.execute(Type.Unknown, geometry.toJSONString());
			} catch (JsonParseException e) {
				log.error(e, "Cannot import geometry from ESRIJSON");
			} catch (IOException e) {
				log.error(e, "Cannot import geometry from ESRIJSON");
			}
						
			if (geometryObj != null) {
				//String wkt = Exporter.execute(0, geometryObj.getGeometry(), null);				
				ByteBuffer buffer = Exporter.execute(0, geometryObj.getGeometry(), null);
				//it's layer srs
				Geometry geom = null;
				try {
					//geom = Reader.read(wkt);
					geom = Reader.read(buffer.array());
				} catch (ParseException e) {
					//log.error(e, "Cannot read from WKT");
					//log.debug(wkt);
					log.error(e, "Cannot read from WKB");
				}
				result.setGeometry(geom);
			}			
		}		
		
		return result;
	}
}
