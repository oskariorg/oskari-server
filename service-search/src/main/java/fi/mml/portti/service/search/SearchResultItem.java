package fi.mml.portti.service.search;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.io.Serializable;
import java.util.*;

/**
 * Search result item.
 */
public class SearchResultItem implements Comparable<SearchResultItem>, Serializable {

    private static final Logger LOG = LogFactory.getLogger(SearchResultItem.class);
    // JSON keys (see toJSON())
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_UUID = "uuid";
    public static final String KEY_TYPE = "type";
	public static final String KEY_LANG = "lang";
    public static final String KEY_RANK = "rank";
    public static final String KEY_LON = "lon";
    public static final String KEY_LAT = "lat";
    public static final String KEY_ZOOMLEVEL = "zoomLevel";
    public static final String KEY_ZOOMSCALE = "zoomScale";
	public static final String KEY_REGION = "region";
	public static final String KEY_CHANNELID = "channelId";

    public static final String KEY_BBOX = "bbox";
    public static final String KEY_LEFT = "left";
    public static final String KEY_TOP = "top";
    public static final String KEY_RIGHT = "right";
    public static final String KEY_BOTTOM = "bottom";

	private static final long serialVersionUID = -1272738593795856016L;
	private static final int TRUNCATE_DESCRIPTION_LENGTH = 350;
	private String title;
	private String resourceNameSpace;
	private String resourceId;
	private String channelId;
    private String natureOfTarget;
	private String description;
	private String contentURL;
	private String actionURL;
	private String gmdURL;
	private String region;
	private String locationTypeCode;
    private String type;
	private String lang;
	private String locationName;
	private Double lon;
	private Double lat;
	private Double westBoundLongitude;
	private Double southBoundLatitude;
	private Double eastBoundLongitude;
	private Double northBoundLatitude;
	private String mapURL;
	private String zoomLevel;
    private double zoomScale = -1;
	private String trunkateDescription;
	private List<String> uuid;
	private boolean downloadable = false;
	private boolean downloadAllowed = false;
    private Map<String, Object> properties = new HashMap<String, Object>();
	
	private int rank = -1;

    /**
     * Add custom result field value for result
     * @param key
     * @param value
     */
    public void addValue(final String key, final Object value) {
        if(key != null && value != null) {
            properties.put(key, value);
        }
    }

    /**
     * Get value for a custom result field
     * @param key
     * @return
     */
    public Object getValue(final String key) {
        return properties.get(key);
    }

    /**
     * Returns custom result field entries
     * @return
     */
    public Set<String> getCustomFieldLabels() {
        return properties.keySet();
    }

	public String toString() {
		return "resourceId=" + resourceId + ", resourceNameSpace=" + resourceNameSpace 
		+ ", title=" + title + ", actionURL=" + actionURL + ", gmdURL=" + gmdURL;
	}

	public int compareTo(SearchResultItem sri) {
		if (this.rank != sri.getRank()) {
			// TODO: rank should be normalized throughout different channels, currently it's not
			return this.rank - sri.getRank();
		}
		// TODO: should make a function to compare if the other one has value and return 1/-1
		if(this.title == null || sri.getTitle() == null) {
			return 0;
		}

		if (this.title.equals(sri.getTitle())) {
			// Same title, order is determined by region
			// Should we use type instead of region here?
			if(this.region == null || sri.getRegion() == null) {
				return 0;
			}
			return this.region.compareTo(sri.getRegion());
		}

		// TODO: streetname ranking should be done internally in the search channel impl which knows about the title content
		String[] streetName1 = this.getTitle().split("\\s");
		String[] streetName2 = sri.getTitle().split("\\s");

		// without street number
		if (streetName1.length != streetName2.length) {
			return streetName1.length - streetName2.length;
		}

		// Same street names
		if (streetName1[0].equals(streetName2[0])) {
			if (streetName1[1] == null || streetName2[1] != null) {
				return 0;
			}
			if (streetName1[1].length() == streetName2[1].length()) {
				return streetName1[1].compareTo(streetName2[1]);
			}
			return streetName1[1].length() - streetName2[1].length();
		}
		return title.compareTo(sri.getTitle());
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @deprecated Use getZoomScale() instead
     * @return zoomLevel
     */
    @Deprecated
    public String getZoomLevel() {
		return zoomLevel;
	}

    /**
     *
     * @deprecated Use setZoomScale() instead
     * @param zoomLevel
     */
    @Deprecated
	public void setZoomLevel(String zoomLevel) {
		this.zoomLevel = zoomLevel;
	}

    /**
     * The scale in which the item should be shown.
     * Map should show it in the closest zoom level available zooming out
     * @param scale
     */
    public void setZoomScale(double scale) {
        this.zoomScale = scale;
    }

    /**
     * The scale in which the item should be shown.
     * Map should show it in the closest zoom level available zooming out
     * @return scale
     */
    public double getZoomScale() {
        return this.zoomScale;
    }

    @Deprecated
	public String getMapURL() {
		return mapURL;
	}

	@Deprecated
	public void setMapURL(String mapURL) {
		this.mapURL = mapURL;
	}

	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public String getTitle() {
		return title;
	}
	public String getEscapedTitle() {
		return title.replaceAll("'", "&#39;");
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		
		if (description.length() > TRUNCATE_DESCRIPTION_LENGTH) {
			this.trunkateDescription = description.substring(0,TRUNCATE_DESCRIPTION_LENGTH-3) + "...";
		} else {
			this.trunkateDescription = description;
		}
			
		this.description = description;
	}

	public String getChannelId() { return channelId;	}
	public void setChannelId(String channelId) { this.channelId = channelId; }
	public String getContentURL() {
		return contentURL;
	}
	public void setContentURL(String contentURL) {
		this.contentURL = contentURL;
	}
	public String getActionURL() {
		return actionURL;
	}
	public void setActionURL(String actionURL) {
		this.actionURL = actionURL;
	}
	public String getGmdURL() {
		return gmdURL;
	}
	public void setGmdURL(String gmdURL) {
		this.gmdURL = gmdURL;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}

	public String getLocationTypeCode() {
		return locationTypeCode;
	}
	public void setLocationTypeCode(String locationTypeCode) {
		this.locationTypeCode = locationTypeCode;
	}

	public Double getLon() {
		return lon;
	}

	/**
	 * @deprecated Use setLong(double) instead
	 * @param lon
	 */
	@Deprecated
	public void setLon(String lon) {
		try {
			this.lon = Double.parseDouble(lon);
		} catch (Exception ignored) {
			this.lon = null;
		}
	}
	public void setLon(double lon) {
		this.lon = lon;
	}

	public Double getWestBoundLongitude() {
		return westBoundLongitude;
	}
	/**
	 * @deprecated Use setWestBoundLongitude(double) instead
	 */
	@Deprecated
	public void setWestBoundLongitude(String westBoundLongitude) {
		try {
			this.westBoundLongitude = Double.parseDouble(westBoundLongitude);
		} catch (Exception ignored) {
			this.westBoundLongitude = null;
		}
	}
	public void setWestBoundLongitude(double westBoundLongitude) {
		this.westBoundLongitude = westBoundLongitude;
	}
	public Double getSouthBoundLatitude() {
		return southBoundLatitude;
	}
	/**
	 * @deprecated Use setSouthBoundLatitude(double) instead
	 */
	@Deprecated
	public void setSouthBoundLatitude(String southBoundLatitude) {
		try {
			this.southBoundLatitude = Double.parseDouble(southBoundLatitude);
		} catch (Exception ignored) {
			this.southBoundLatitude = null;
		}
	}
	public void setSouthBoundLatitude(double southBoundLatitude) {
		this.southBoundLatitude = southBoundLatitude;
	}
	public Double getEastBoundLongitude() {
		return eastBoundLongitude;
	}
	/**
	 * @deprecated Use setEastBoundLongitude(double) instead
	 */
	@Deprecated
	public void setEastBoundLongitude(String eastBoundLongitude) {
		try {
			this.eastBoundLongitude = Double.parseDouble(eastBoundLongitude);
		} catch (Exception ignored) {
			this.eastBoundLongitude = null;
		}
	}
	public void setEastBoundLongitude(double eastBoundLongitude) {
		this.eastBoundLongitude = eastBoundLongitude;
	}
	public Double getNorthBoundLatitude() {
		return northBoundLatitude;
	}
	/**
	 * @deprecated Use setNorthBoundLatitude(double) instead
	 */
	@Deprecated
	public void setNorthBoundLatitude(String northBoundLatitude) {
		try {
			this.northBoundLatitude = Double.parseDouble(northBoundLatitude);
		} catch (Exception ignored) {
			this.northBoundLatitude = null;
		}
	}
	public void setNorthBoundLatitude(double northBoundLatitude) {
		this.northBoundLatitude = northBoundLatitude;
	}

	public Double getLat() {
		return lat;
	}
	/**
	 * @deprecated Use setLat(double) instead
	 */
	@Deprecated
	public void setLat(String lat) {
		try {
			this.lat = Double.parseDouble(lat);
		} catch (Exception ignored) {
			this.lat = null;
		}
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public String getLocationName() {
		return locationName;
	}
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
	public String getTrunkateDescription() {
		return trunkateDescription;
	}
	public void setTrunkateDescription(String trunkateDescription) {
		this.trunkateDescription = trunkateDescription;
	}
	public String getResourceNameSpace() {
		return resourceNameSpace;
	}
	public void setResourceNameSpace(String resourceNameSpace) {
		this.resourceNameSpace = resourceNameSpace;
	}
	public String getResourceId() {
		return resourceId;
	}
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
    public String getNatureOfTarget() {
        return natureOfTarget;
    }
    public void setNatureOfTarget(String natureOfTarget) {
        this.natureOfTarget = natureOfTarget;
    }
	public List<String> getUuId() {
		return uuid;
	}

    // TODO: what is uuid used for?
	public void setUuId(List uuid) {
		this.uuid = uuid;
	}
	public void addUuId(String uuid) {
		if(this.uuid == null){
			this.uuid = new ArrayList();
		}
		this.uuid.add(uuid);
	}
	
	/**
	 * Get boolean that indicates if this search result item has downloadable material.
	 * @return
	 */
	public boolean isDownloadable() {
		return downloadable;
	}
	
	/**
	 * Set boolean that indicates if this search result item has downloadable material.
	 * @return
	 */
	
	public void setDownloadable(boolean downloadable) {
		this.downloadable = downloadable;
	}
	
	/**
	 * Get boolean that indicates if the user has permissions to download this material.
	 * @return
	 */
	public boolean isDownloadAllowed() {
		return downloadAllowed;
	}
	
	/**
	 * Set boolean that indicates if the user has permissions to download this material.
	 * @return
	 */
	public void setDownloadAllowed(boolean downloadAllowed) {
		this.downloadAllowed = downloadAllowed;
	}

    public boolean hasNameAndLocation() {
        return hasValue(getTitle()) && getLat() != null && getLon() != null;
    }

    private boolean hasValue(final String param) {
        return param != null && !param.isEmpty();
    }

    /**
     * Calls toJSON with resourceId
     * @return
     */
    public JSONObject toJSON() {
        String id = getResourceId();
        if (id == null || id.isEmpty()) {
            id = Long.toString(System.nanoTime());
        }
        return toJSON(id);
    }

    /**
     * Constructs a response JSON from the search result
     * @param itemId
     * @return
     */
    public JSONObject toJSON(Object itemId) {
        //final JSONObject node = JSONHelper.createJSONObject(KEY_NAME, Jsoup.clean(getTitle(), Whitelist.none()));
        final JSONObject node = JSONHelper.createJSONObject(KEY_NAME, getTitle());
        JSONHelper.putValue(node, KEY_ID, itemId);
        JSONHelper.putValue(node, KEY_LON, getLon());
        JSONHelper.putValue(node, KEY_LAT, getLat());

		JSONHelper.putValue(node, KEY_LANG, getLang());
        JSONHelper.putValue(node, KEY_RANK, getRank());
        JSONHelper.putValue(node, KEY_TYPE, getType());
		JSONHelper.putValue(node, KEY_CHANNELID, getChannelId());

        String region = ConversionHelper.getString(getRegion(), "");
		JSONHelper.putValue(node, KEY_REGION, Jsoup.clean(region, Safelist.none()));

        // do the bbox if we have any of the bbox values (Should have all if has any one of these)
        if(getWestBoundLongitude() != null) {
            JSONObject bbox = new JSONObject();
            JSONHelper.putValue(bbox, KEY_RIGHT, getEastBoundLongitude());
            JSONHelper.putValue(bbox, KEY_TOP, getNorthBoundLatitude());
            JSONHelper.putValue(bbox, KEY_LEFT, getWestBoundLongitude());
            JSONHelper.putValue(bbox, KEY_BOTTOM, getSouthBoundLatitude());
            JSONHelper.putValue(node, KEY_BBOX, bbox);
        }

        // Zoom level - prefer scale, zoom level is deprecated
        JSONHelper.putValue(node, KEY_ZOOMLEVEL, getZoomLevel());
        if(getZoomScale() != -1) {
            JSONHelper.putValue(node, KEY_ZOOMSCALE, getZoomScale());
        }
        // setup uuid (TODO: what is uuid used for?)
        if(getUuId() != null && !getUuId().isEmpty()){
            final JSONArray jArray = new JSONArray();
            for(String uuid : getUuId()){
                jArray.put(uuid);
            }
            JSONHelper.put(node, KEY_UUID, jArray);
        }

        // append additional fields
        // since they are at the end, they can be used to override default values
        // should they be attached at the beginning so this won't happen?
        for(String label : getCustomFieldLabels()) {
            JSONHelper.putValue(node, label, getValue(label));
        }
        return node;
    }
}