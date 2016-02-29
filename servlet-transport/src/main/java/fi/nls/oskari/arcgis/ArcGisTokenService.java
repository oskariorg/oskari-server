package fi.nls.oskari.arcgis;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArcGisTokenService {
	private static final Logger log = LogFactory.getLogger(ArcGisTokenService.class);
	private static final Pattern _pattern = Pattern.compile("^(.*)/arcgis.*$");
	private static final String _serviceUrl = PropertyUtil.get("arcgis.tokenservice.url", null);
    
    private static class ArcgisTokenServiceHolder {
        static final ArcGisTokenService INSTANCE = new ArcGisTokenService();
    }
	
	public static ArcGisTokenService getInstance() {
		return ArcgisTokenServiceHolder.INSTANCE;
	}
	
	protected ArcGisTokenService() {
	}
	
	@SuppressWarnings("unchecked")
	public String getTokenForLayer(String url) 
	{
		String serverUrl = getServerAddressFromUrl(url);
		String requestUrl = _serviceUrl + "&serverUrl=" + serverUrl;
		try {
			HttpURLConnection conn = IOHelper.getConnection(requestUrl);
			IOHelper.writeHeader(conn, IOHelper.HEADER_ACCEPT, IOHelper.CONTENT_TYPE_JSON);
			String requestBody = IOHelper.readString(conn);

			JSONObject json = (JSONObject) JSONValue.parse(requestBody);
			if (json.containsKey("token")) {
				return (String) json.get("token");
			}
		}
		catch (Exception ex) {
			log.error(ex, "Error getting token for layer with url:", requestUrl);
		}
		return null;
	}
	
	private String getServerAddressFromUrl(String url) {
		
		Matcher matcher = _pattern.matcher(url);
		
		if (matcher.matches()) {
			return matcher.group(1);
		}

		log.warn("Cannot find server from url " + url);

        return null;
	}
}
