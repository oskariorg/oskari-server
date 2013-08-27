package fi.nls.oskari.domain.map;

public class CapabilitiesCache {
	
	private int layerId;
	private String data;
	private String version;
	
	public int getLayerId() {
		return layerId;
	}
	public void setLayerId(int layerId) {
		this.layerId = layerId;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String toString(){
		return "layerId:"+ layerId + " version:"+version;
	}

}
