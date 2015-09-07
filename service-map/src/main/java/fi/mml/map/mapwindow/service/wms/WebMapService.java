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
	public Map<String, String> getSupportedStyles();
	
	
	/**
	 * Return Map of strings representing supported legend for WMS interface
	 * key: styleName, value: legend
	 * 
	 * @return List of Strings
	 */
	public Map<String, String> getSupportedLegends();
	
	
	/**
	 * returns getCapabilitiesUrl
	 */
	public String getCapabilitiesUrl();
	
	
	/**
	 * returns queryable:
	 * @return
	 */
	public boolean isQueryable();
	
	
	/**
	 * Return Array of strings representing supported formats for feature info
	 * @return String array
	 */
	public String[] getFormats();


    /**
     * Return Array of strings representing keywords listed in capabilities
     * @return String array
     */
    public String[] getKeywords();

    /**
     * Return WMS version as string
     * @return String
     */
    public String getVersion();

	/**
	 * Return WMS time extent / dimension value as string
	 * @return String
	 */
	public List<String> getTime();
}
