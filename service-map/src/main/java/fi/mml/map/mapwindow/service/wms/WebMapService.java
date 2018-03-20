package fi.mml.map.mapwindow.service.wms;

import java.util.List;
import java.util.Map;

public interface WebMapService {

	/**
	 * Return Map of strings representing supported styles for WMS interface
	 * key: styleName, value: styleTitle
	 * 
	 * @return List of Strings
	 */
	Map<String, String> getSupportedStyles();
	
	
	/**
	 * Return Map of strings representing supported legend for WMS interface
	 * key: styleName, value: legend
	 * 
	 * @return List of Strings
	 */
	Map<String, String> getSupportedLegends();

	/**
	 * Return legend image for given style
	 *
	 * @return legend image url
	 */
	String getLegendForStyle(String key);
	
	/**
	 * returns getCapabilitiesUrl
	 */
	String getCapabilitiesUrl();
	
	
	/**
	 * returns queryable:
	 * @return
	 */
	boolean isQueryable();
	
	
	/**
	 * Return Array of strings representing supported formats for feature info
	 * @return String array
	 */
	String[] getFormats();


    /**
     * Return Array of strings representing keywords listed in capabilities
     * @return String array
     */
    String[] getKeywords();

    /**
     * Return WMS version as string
     * @return String
     */
    String getVersion();

	/**
	 * Return WMS time extent / dimension value as string
	 * @return String
	 */
	List<String> getTime();

    /**
     * Return Array of strings representing supported CRS of layer service
     * @return String array
     */
    String[] getCRSs();
    
    /**
     * Returns a WKT representation of the bounding box of the layer.
     * @return String bounding box
     */
    String getGeom();

}
