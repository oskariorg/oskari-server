package downloadbasket.helpers;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import java.util.HashMap;
import java.util.Map;
import fi.nls.oskari.util.IOHelper;

public class Helpers {

	private static final Logger log = LogFactory.getLogger(Helpers.class);

	/**
	 * Get GetFeatureInfoUrl for proxy
	 * 
	 * @param url
	 *            a url
	 * @param projection
	 *            a projection
	 * @param bbox
	 *            a bbox
	 * @param width
	 *            a width
	 * @param height
	 *            a heigth
	 * @param x
	 *            a x
	 * @param y
	 *            a y
	 * @param layerName
	 *            a layer name
	 * @return getFeatureInfoUrl
	 */
	public static String getGetFeatureInfoUrlForProxy(String url, String projection, String bbox, String width,
			String height, String x, String y, String layerName) {
		Map<String, String> urlParams = new HashMap<String, String>();
		urlParams.put("SERVICE", "WMS");
		urlParams.put("VERSION", "1.1.1");
		urlParams.put("REQUEST", "GetFeatureInfo");
		urlParams.put("SRS", projection);
		urlParams.put("BBOX", bbox);
		urlParams.put("WIDTH", width);
		urlParams.put("HEIGHT", height);
		urlParams.put("QUERY_LAYERS", layerName);
		urlParams.put("X", Long.toString(Math.round(Float.parseFloat(x))));
		urlParams.put("Y", Long.toString(Math.round(Float.parseFloat(y))));
		urlParams.put("LAYERS", layerName);
		urlParams.put("FEATURECOUNT", "1");
		urlParams.put("INFO_FORMAT", "application/json");
		urlParams.put("EXCEPTIONS", "application/vnd.ogc.se_xml");
		urlParams.put("BUFFER", "0");

		return IOHelper.constructUrl(url, urlParams);
	}

	/**
	 * Get layer name without namespace.
	 * 
	 * @param layerName
	 *            layer name
	 * @return layer name without namespace
	 */
	public static String getLayerNameWithoutNameSpace(String layerName) {
		String[] temp = layerName.split(":");
		String layerNameWithoutNameSpace = layerName;
		if (temp.length == 2) {
			layerNameWithoutNameSpace = temp[1];
		}
		return layerNameWithoutNameSpace;
	}
}
