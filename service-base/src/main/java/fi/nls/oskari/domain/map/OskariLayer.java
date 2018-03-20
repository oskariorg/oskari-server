package fi.nls.oskari.domain.map;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;

import java.util.*;

public class OskariLayer extends JSONLocalizedNameAndTitle implements Comparable<OskariLayer> {

    private static Logger log = LogFactory.getLogger(OskariLayer.class);
    public static final String PROPERTY_AJAXURL = "oskari.ajax.url.prefix";

    private static final String TYPE_COLLECTION = "collection";
    public static final String TYPE_WMS = "wmslayer";
    public static final String TYPE_WFS = "wfslayer";
    public static final String TYPE_WMTS = "wmtslayer";
    public static final String TYPE_STATS = "statslayer";
    public static final String TYPE_ANALYSIS = "analysislayer";
    public static final String TYPE_USERLAYER = "userlayer";
    public static final String TYPE_ARCGIS93 = "arcgis93layer";

    private int id = -1;
    private int parentId = -1;
    private String externalId;
	private String type;

    private boolean isBaseMap = false;
    private int dataproviderId;

    private String name;
    private String url;

    // simplied url is just for caching so we don't need to create it but once
    private String secureBaseUrl = PropertyUtil.get("maplayer.wmsurl.secure", "");
    private String simplifiedUrl;

    // defaults
	private Integer opacity = 100;
    private String style;
	private Double minScale = -1d;
	private Double maxScale = -1d;

    private String legendImage;
    private String metadataId;

    private JSONObject params = new JSONObject();
    private JSONObject options = new JSONObject();
    private JSONObject attributes = new JSONObject();
    private JSONObject capabilities = new JSONObject();

    private boolean realtime = false;
    private int refreshRate;

    private String gfiXslt;
    private String gfiType;
    private String gfiContent;
    private String geometry;

    private String username;
    private String password;

    private String version = "";
    private String srs_name;

    private Date created = null;
    private Date updated = null;
    private Integer orderNumber;

    private Set<MaplayerGroup> maplayerGroups = new HashSet<MaplayerGroup>();
    private Set<DataProvider> dataProviders = new HashSet<DataProvider>();
    private List<OskariLayer> sublayers = new ArrayList<OskariLayer>();

    private Date capabilitiesLastUpdated;
    private int capabilitiesUpdateRateSec;

    public boolean isCollection() {
        return TYPE_COLLECTION.equals(type);
    }

    // we only link one theme at the moment so get the first one
	public MaplayerGroup getMaplayerGroup() {
        if(maplayerGroups == null || maplayerGroups.isEmpty()) {
            return null;
        }
        if(maplayerGroups.size() > 1) {
            // TODO: remove this when we support more than one theme
            log.warn("More than one maplayer group, this shouldn't happen!! layerId:", getId(), "- Maplayer groupsN:" , maplayerGroups);
        }
		return maplayerGroups.iterator().next();
	}
    public Set<MaplayerGroup> getMaplayerGroups() {
        return maplayerGroups;
    }
    public void addGroups(final List<MaplayerGroup> groups) {
        if(groups != null && !groups.isEmpty()) {
            maplayerGroups.addAll(groups);
        }
    }
    public void addGroup(final MaplayerGroup group) {
        if(group != null) {
            maplayerGroups.add(group);
        }
    }

    public void emptyMaplayerGroups() {
        maplayerGroups.clear();
    }

    // we only link one group at the moment so get the first one
    public DataProvider getGroup() {
        if(dataProviders == null || dataProviders.isEmpty()) {
            return null;
        }
        return dataProviders.iterator().next();
    }

    public void addDataprovider(final DataProvider dataProvider) {
        if(dataProvider != null) {
            dataProviders.add(dataProvider);
            setDataproviderId(dataProvider.getId());
        }
    }

    public int compareTo(OskariLayer l) {
        String defaultLang = PropertyUtil.getDefaultLanguage();
        return this.getName(defaultLang).compareTo(l.getName(defaultLang));
    }
    /**
     * Returns a simplified version of the wms url. Splits it with comma-character, takes the first one and
     * returns it after removing possible protocol.
     * @return simplified version of wms url or an empty string if there is any problems creating it.
     */
    public String getSimplifiedUrl() {
        return getSimplifiedUrl(false);
    }

    /**
     * Returns a simplified version of the wms url. Splits it with comma-character, takes the first one.
     * @param keepProtocol true to include protocol of the url
     * @return simplified version of wms url or an empty string if there is any problems creating it.
     */
    public String getSimplifiedUrl(final boolean keepProtocol) {
        if(simplifiedUrl != null) {
            return simplifiedUrl;
        }
        if(url == null) {
            return "";
        }
        final String[] splitted = url.split("\\s*,\\s*");
        if(splitted == null || splitted.length == 0 ) {
            return "";
        }

        final String protocolSeparator = "://";
        final int protocolIndex = splitted[0].indexOf(protocolSeparator);
        simplifiedUrl = splitted[0].trim();
        if(protocolIndex != -1 && !keepProtocol) {
            // strip protocol if one was found and keepProtocol is false
            simplifiedUrl = splitted[0].substring(protocolIndex + protocolSeparator.length()).trim();
        }
        return simplifiedUrl;
    }

    public void addSublayer(final OskariLayer layer) {
        if(layer != null) {
            sublayers.add(layer);
        }
    }

    public void addSublayers(List<OskariLayer> layers) {
        sublayers.addAll(layers);
    }

    public List<OskariLayer> getSublayers() {
        return sublayers;
    }

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public Integer getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(Integer orderNumber) {
		this.orderNumber = orderNumber;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getOpacity() {
		return opacity;
	}
	public void setOpacity(Integer opacity) {
		this.opacity = opacity;
	}
	public Double getMinScale() {
		return minScale;
	}
	public void setMinScale(Double minScale) {
        if(minScale != null && !minScale.isNaN()) {
		    this.minScale = minScale;
        }
	}
	public Double getMaxScale() {
		return maxScale;
	}
	public void setMaxScale(Double maxScale) {
        if(maxScale != null && !maxScale.isNaN()) {
            this.maxScale = maxScale;
        }
	}

	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	public String getLegendImage() {
		return legendImage;
	}
	public void setLegendImage(String legendImage) {
		this.legendImage = legendImage;
	}

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public boolean isBaseMap() {
        return isBaseMap;
    }

    public void setBaseMap(boolean baseMap) {
        isBaseMap = baseMap;
    }

    public int getDataproviderId() {
        return dataproviderId;
    }

    public void setDataproviderId(int dataproviderId) {
        this.dataproviderId = dataproviderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return getUrl(false);
    }

    public String getUrl(final boolean isSecure) {
        if(url == null) {
            return "";
        }
        if(url.toLowerCase().startsWith("https://") || url.startsWith("/")) {
            // don't use prefix for urls that:
            // - already use secure protocol
            // - are like /action?, /wms or //domain.com/path
            return url;
        }
        if(isSecure) {
            if(!secureBaseUrl.isEmpty()) {
                return secureBaseUrl + getSimplifiedUrl();
            }
            // proxy layer url
            Map<String, String> urlParams = new LinkedHashMap<String, String>();
            urlParams.put("action_route", "GetLayerTile");
            urlParams.put("id", Integer.toString(getId()));
            return IOHelper.constructUrl(PropertyUtil.get(PROPERTY_AJAXURL), urlParams);
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        // reset cached simplified url
        this.simplifiedUrl = null;
    }

    public String getMetadataId() {
        return metadataId;
    }

    public void setMetadataId(String metadataId) {
        this.metadataId = metadataId;
    }

    public JSONObject getParams() {
        return params;
    }

    public void setParams(JSONObject params) {
        this.params = params;
    }

    public JSONObject getOptions() {
        return options;
    }

    public void setOptions(JSONObject options) {
        this.options = options;
    }

    public JSONObject getAttributes() {
        return attributes;
    }

    public void setAttributes(JSONObject attributes) {
        this.attributes = attributes;
    }

    public JSONObject getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(JSONObject capabilities) {
        this.capabilities = capabilities;
    }

    public String getGfiType() {
        return gfiType;
    }

    public void setGfiType(String gfiType) {
        this.gfiType = gfiType;
    }

    public String getGfiXslt() {
        return gfiXslt;
    }

    public void setGfiXslt(String gfiXslt) {
        this.gfiXslt = gfiXslt;
    }

    public String getGfiContent() {
        return gfiContent;
    }

    public void setGfiContent(String gfiContent) {
        this.gfiContent = gfiContent;
    }

    public String getGeometry() {
        if(geometry == null) {
            // geometry is from a CSW service. Capabilities "geom" is the coverage from layer capabilities
            return getCapabilities().optString("geom");
        }
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public boolean getRealtime() { return realtime; }

    public void setRealtime(boolean realtime) { this.realtime = realtime; }

    public int getRefreshRate() { return refreshRate; }

    public void setRefreshRate(int refreshRate) {
        if (refreshRate < 0) {
            this.refreshRate = 0;
        } else {
            this.refreshRate = refreshRate;
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSrs_name() {
        return srs_name;
    }

    public void setSrs_name(String srs_name) {
        this.srs_name = srs_name;
    }

    public Date getCapabilitiesLastUpdated() {
        return capabilitiesLastUpdated;
    }

    public void setCapabilitiesLastUpdated(Date capabilitiesLastUpdated) {
        this.capabilitiesLastUpdated = capabilitiesLastUpdated;
    }

    public int getCapabilitiesUpdateRateSec() {
        return capabilitiesUpdateRateSec;
    }

    public void setCapabilitiesUpdateRateSec(int capabilitiesUpdateRateSec) {
        this.capabilitiesUpdateRateSec = capabilitiesUpdateRateSec;
    }

}
