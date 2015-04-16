package fi.nls.oskari.arcgis.pojo;

public class ArcGisProperty {
	public final static String ID_PROPERTY = "OBJECTID";
	
	private String name;
	private Object value;
	
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}	
}
