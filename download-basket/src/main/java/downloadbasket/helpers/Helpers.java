package downloadbasket.helpers;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class Helpers {
	
	private static final Logger log = LogFactory.getLogger(Helpers.class);
	
	/**
	 * Get GetFeatureInfoUrl for proxy
	 * @param url a url
	 * @param projection a projection
	 * @param bbox a bbox
	 * @param width a width
	 * @param height a heigth
	 * @param x a x
	 * @param y a y
	 * @param layerName a layer name
	 * @return getFeatureInfoUrl
	 */
	public static String getGetFeatureInfoUrlForProxy(String url, String projection, String bbox, String width, String height, String x, String y, String layerName) {
		String wmsUrl = url+"?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetFeatureInfo&SRS="+projection
			+"&BBOX="+bbox
			+"&WIDTH="+width
			+"&HEIGHT="+height
			+"&QUERY_LAYERS="+layerName
			+"&X="+Math.round(Float.parseFloat(x))
			+"&Y="+Math.round(Float.parseFloat(y))
			+"&LAYERS="+layerName
			+"&FEATURECOUNT=1"
			+"&INFO_FORMAT=application/json"
			+"&EXCEPTIONS=application/vnd.ogc.se_xml"
			+"&BUFFER=0";

		return wmsUrl;
	}

	/**
	 * Get layer name without namespace.
	 * @param layerName layer name
	 * @return layer name without namespace
	 */
	public static String getLayerNameWithoutNameSpace(String layerName) {
		String[] temp = layerName.split(":");
		String layerNameWithoutNameSpace = layerName;
		if(temp.length==2){
			layerNameWithoutNameSpace = temp[1];
		}
		return layerNameWithoutNameSpace;
	}
}
