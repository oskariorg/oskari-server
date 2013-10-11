package fi.nls.oskari.domain.map.wfs;

public class OwsOperation {
	private int id;
	private WFSService wfsService;
	private String name;
	private String url;
	
	@Override
	public String toString() {
		return "OwsOperation [id=" + id + ", name=" + name + ", url=" + url + "]";
	}
	
	public OwsOperation() {
	}
	
	public OwsOperation(String name, String url) {
		setName(name);
		setUrl(url);
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public WFSService getWfsService() {
		return wfsService;
	}
	public void setWfsService(WFSService wfsService) {
		this.wfsService = wfsService;
	}
	public String getName() {
		if (name == null) {
			return "";
		}
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		if (url == null) {
			return "";
		}
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
