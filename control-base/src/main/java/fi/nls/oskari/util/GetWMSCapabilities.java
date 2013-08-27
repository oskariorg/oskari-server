package fi.nls.oskari.util;

import java.io.IOException;

import org.json.JSONObject;
import org.json.XML;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParamsException;

public class GetWMSCapabilities {
	
	public static String getResponse(final String url) throws ActionException {
        try {
            final String response = IOHelper.getURL(getUrl(url));
            if (response == null) {
                throw new ActionParamsException("Response was <null>");
            } else if (response.toUpperCase().indexOf("EXCEPTIONREPORT") > -1) {
                throw new ActionParamsException(response);
            }
            return response;
        }
        catch (IOException ex) {
            throw new ActionException("Couldnt read server response from url.", ex);
        }
    }

    public static JSONObject parseCapabilities(String response) throws ActionException {
        try {
            // convert xml String to JSON
            return XML.toJSONObject(response);

        } catch (Exception e) {
            throw new ActionException("XML to JSON failed", e);
        }
    }

    private static String getUrl(String urlin) {

        if (urlin.isEmpty())
            return "";
        String url = urlin;
        // check params
        if (url.indexOf("?") == -1) {
            url = url + "?";
            if (url.toLowerCase().indexOf("service=") == -1)
                url = url + "service=WMS";
            if (url.toLowerCase().indexOf("getcapabilities") == -1)
                url = url + "&request=GetCapabilities";
        } else {
            if (url.toLowerCase().indexOf("service=") == -1)
                url = url + "&service=WMS";
            if (url.toLowerCase().indexOf("getcapabilities") == -1)
                url = url + "&request=GetCapabilities";

        }

        return url;
    }
}