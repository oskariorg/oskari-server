package fi.nls.oskari.domain.map.wfs;

import java.util.Map;

public class PropertyMapConverter extends Converter {
	private Map<String, String> propertyMap;
	
	protected PropertyMapConverter() {
	}
	
	@Override
	public String toString() {
		return "PROPERTYMAP_CONVERTER";
	}
	
	@Override
	public String getConvertedValue() {
		return propertyMap.get(rawValue);
	}
	
	public Map<String, String> getPropertyMap() {
		return propertyMap;
	}
	public void setPropertyMap(Map<String, String> propertyMap) {
		this.propertyMap = propertyMap;
	}
}
