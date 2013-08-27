package fi.nls.oskari.domain.map.wfs;

import java.util.ArrayList;
import java.util.List;

public abstract class OGCService {
	private int id;
	private String titleFi = "";
	private String titleSv = "";
	private String titleEn = "";
	private String owsAbstractFi = "";
	private String owsAbstractSv = "";
	private String owsAbstractEn = "";
	private String url ="";
	private String username = "";
	private String password = "";
	private String gmlVersion = "3.1.1";
	private String geonetworkFileIdentifier = "";
	private boolean gml2typeSeparator = false;
	
	private List<OwsOperation> owsOperations = new ArrayList<OwsOperation>();
	
	/** Does this service use proxy whi*/
	private boolean useProxy;
	
	@Override
	public String toString() {
		return "OGCService [geonetworkFileIdentifier="
				+ geonetworkFileIdentifier + ", gmlVersion=" + gmlVersion
				+ ", id=" + id + ", owsAbstractEn=" + owsAbstractEn
				+ ", owsAbstractFi=" + owsAbstractFi + ", owsAbstractSv="
				+ owsAbstractSv + ", password=" + password + ", titleEn="
				+ titleEn + ", titleFi=" + titleFi + ", titleSv=" + titleSv
				+ ", url=" + url + ", useProxy=" + useProxy + ", username="
				+ username + "]";
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public String getUsername() {
		if (username == null) {
			return "";
		}
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		if (password == null) {
			return "";
		}
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public List<OwsOperation> getOwsOperations() {
		return owsOperations;
	}
	public void setOwsOperations(List<OwsOperation> owsOperations) {
		this.owsOperations = owsOperations;
	}
	public String getTitleFi() {
		if (titleFi == null) {
			return "";
		}
		return titleFi;
	}
	public void setTitleFi(String titleFi) {
		this.titleFi = titleFi;
	}
	public String getTitleSv() {
		if (titleSv == null) {
			return "";
		}
		return titleSv;
	}
	public void setTitleSv(String titleSv) {
		this.titleSv = titleSv;
	}
	public String getTitleEn() {
		if (titleEn == null) {
			return "";
		}
		return titleEn;
	}
	public void setTitleEn(String titleEn) {
		this.titleEn = titleEn;
	}
	public String getOwsAbstractFi() {
		if (owsAbstractFi == null) {
			return "";
		}
		return owsAbstractFi;
	}

	public void setOwsAbstractFi(String owsAbstractFi) {
		this.owsAbstractFi = owsAbstractFi;
	}
	public String getOwsAbstractSv() {
		if (owsAbstractSv == null) {
			return "";
		}
		return owsAbstractSv;
	}
	public void setOwsAbstractSv(String owsAbstractSv) {
		this.owsAbstractSv = owsAbstractSv;
	}
	public String getOwsAbstractEn() {
		if (owsAbstractEn == null) {
			return "";
		}
		return owsAbstractEn;
	}
	public void setOwsAbstractEn(String owsAbstractEn) {
		this.owsAbstractEn = owsAbstractEn;
	}
	
	public boolean isUseProxy() {
		return useProxy;
	}

	public void setUseProxy(boolean useProxy) {
		this.useProxy = useProxy;
	}

	public String getGmlVersion() {
		return gmlVersion;
	}

	public void setGmlVersion(String gmlVersion) {
		this.gmlVersion = gmlVersion;
	}

	public String getGeonetworkFileIdentifier() {
		return geonetworkFileIdentifier;
	}

	public void setGeonetworkFileIdentifier(String geonetworkFileIdentifier) {
		this.geonetworkFileIdentifier = geonetworkFileIdentifier;
	}

	public boolean isGml2typeSeparator() {
		return gml2typeSeparator;
	}

	public void setGml2typeSeparator(boolean gml2typeSeparator) {
		this.gml2typeSeparator = gml2typeSeparator;
	}
	
	
}
