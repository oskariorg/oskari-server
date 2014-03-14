package fi.mml.portti.service.search;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Search result item.
 */
public class SearchResultItem implements Comparable<SearchResultItem>, Serializable {
	
	private static final long serialVersionUID = -1272738593795856016L;
	private static final int TRUNCATE_DESCRIPTION_LENGTH = 350;
	private String title;
	private String resourceNameSpace;
	private String resourceId;
	private String description;
	private String contentURL;
	private String actionURL;
	private String gmdURL;
	private String village;
	private String locationTypeCode;
    private String type;
	private String locationName;
	private String lon;
	private String lat;
	private String westBoundLongitude;
	private String southBoundLatitude;
	private String eastBoundLongitude;
	private String northBoundLatitude;	
	private String mapURL;
	private String zoomLevel;
	private String trunkateDescription;
	private boolean downloadable = false;
	private boolean downloadAllowed = false;
    private Map<String, Object> properties = new HashMap<String, Object>();
	
	private int rank;

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

	public String toString() {
		return "resourceId=" + resourceId + ", resourceNameSpace=" + resourceNameSpace 
		+ ", title=" + title + ", actionURL=" + actionURL + ", gmdURL=" + gmdURL;
	}
	
	public int compareTo(SearchResultItem sri) {
		if (this.rank == sri.getRank()) {
			if (this.title.equals(sri.getTitle())) {
				/* Same title, order is determined by village */
				return this.village.compareTo(sri.getVillage());
			} else {
				
				String[] streetName1 = this.getTitle().split("\\s");
				String[] streetName2 = sri.getTitle().split("\\s");
				
				/* whitout street number */
				if (streetName1.length != streetName2.length) {
					return streetName1.length - streetName2.length;
				}
				
				/* Same street names  */
				if (streetName1[0].equals(streetName2[0])) {
					//if (streetName1[1] == null || streetName2[1] != null)
					if (streetName1[1].length() == streetName2[1].length()) {
						return streetName1[1].compareTo(streetName2[1]);
					} else {
						return streetName1[1].length() - streetName2[1].length();
					}
				} 
				else {
					/* But those titles not are of same length */
					return title.compareTo(sri.getTitle());
				}
			}
		}
		
		return this.rank - sri.getRank();
	}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getZoomLevel() {
		return zoomLevel;
	}

	public void setZoomLevel(String zoomLevel) {
		this.zoomLevel = zoomLevel;
	}

	public String getMapURL() {
		return mapURL;
	}

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
	public String getVillage() {
		return village;
	}
	public void setVillage(String village) {
		this.village = village;
	}
	public String getLocationTypeCode() {
		return locationTypeCode;
	}
	public void setLocationTypeCode(String locationTypeCode) {
		this.locationTypeCode = locationTypeCode;
	}
	public String getLon() {
		return lon;
	}
	public void setLon(String lon) {
		this.lon = lon;
	}	
	public String getWestBoundLongitude() {
		return westBoundLongitude;
	}
	public void setWestBoundLongitude(String westBoundLongitude) {
		this.westBoundLongitude = westBoundLongitude;
	}
	public String getSouthBoundLatitude() {
		return southBoundLatitude;
	}
	public void setSouthBoundLatitude(String southBoundLatitude) {
		this.southBoundLatitude = southBoundLatitude;
	}
	public String getEastBoundLongitude() {
		return eastBoundLongitude;
	}
	public void setEastBoundLongitude(String eastBoundLongitude) {
		this.eastBoundLongitude = eastBoundLongitude;
	}
	public String getNorthBoundLatitude() {
		return northBoundLatitude;
	}
	public void setNorthBoundLatitude(String northBoundLatitude) {
		this.northBoundLatitude = northBoundLatitude;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
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
}