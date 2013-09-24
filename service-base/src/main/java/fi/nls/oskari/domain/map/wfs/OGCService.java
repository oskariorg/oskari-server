package fi.nls.oskari.domain.map.wfs;

import fi.nls.oskari.domain.map.JSONLocalizedTitleAndAbstract;
import fi.nls.oskari.util.PropertyUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class OGCService extends JSONLocalizedTitleAndAbstract {
	private int id;
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
		String ret =  "OGCService [geonetworkFileIdentifier="
				+ geonetworkFileIdentifier + ", gmlVersion=" + gmlVersion
				+ ", id=" + id;
        for (String locale : PropertyUtil.getSupportedLocales()) {
            String lang = locale.split("_")[0];
            String ucLang = Character.toUpperCase(lang.charAt(0)) + lang.substring(1);
            ret += ", owsAbstract" + ucLang + "=" + getAbstract(lang);
            ret += ", title" + ucLang + "=" + getTitle(lang);
        }
		ret += ", password=" + password;
		ret += ", url=" + url + ", useProxy=" + useProxy + ", username=";
        ret += username + "]";
        return ret;
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
