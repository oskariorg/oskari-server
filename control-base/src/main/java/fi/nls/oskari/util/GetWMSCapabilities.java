package fi.nls.oskari.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.json.XML;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParamsException;

public class GetWMSCapabilities {

    final static String ENCODE_ATTRIBUTE =  "encoding=\"";


    public static String getResponse(final String url, final String user, final String pwd) throws ActionException {
        try {

            final String rawResponse =  IOHelper.getURL(getUrl(url), user, pwd);
            final String response = fixEncode(rawResponse, url);

            if (response == null) {
                throw new ActionParamsException("Response was <null>");
            } else if (response.toUpperCase().indexOf("EXCEPTIONREPORT") > -1) {
                throw new ActionParamsException(response);
            }
            return response;
        }
        catch (IOException ex) {
            throw new ActionException("Couldn't read server response from url.", ex);
        }
    }
    // Check if not UTF-8
    private static String fixEncode(String response, String pUrl) throws ActionException {
        String encodedResponse = response;
        String[] processingSplit  = response.split("\\?>");

        if (processingSplit != null && processingSplit.length > 0) {

            int encodeAttributeStart = processingSplit[0].indexOf(ENCODE_ATTRIBUTE);

            if (encodeAttributeStart > 0) {
                encodeAttributeStart = encodeAttributeStart+ ENCODE_ATTRIBUTE.length();
                String charset = processingSplit[0].substring(encodeAttributeStart, processingSplit[0].indexOf("\"", encodeAttributeStart));
                try {
                    if (!IOHelper.DEFAULT_CHARSET.equals(charset)) {
                        encodedResponse = IOHelper.getURL(getUrl(pUrl), Collections.EMPTY_MAP, charset);
                    }
                } catch (UnsupportedEncodingException ex) {
                    throw new ActionException("UnsupportedEncodingException", ex);
                } catch (IOException ex) {
                    throw new ActionException("Couldnt read server response from url.", ex);
                }
            }
        }


        return encodedResponse;
    }

    public static JSONObject parseCapabilities(String response) throws ActionException {
        try {
            // convert xml String to JSON
            return XML.toJSONObject(response);

        } catch (Exception e) {
            throw new ActionException("XML to JSON failed", e);
        }
    }

    private static String getUrl(final String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }

        final Map<String, String> params = new HashMap<String, String>();
        // check existing params
        if(!url.toLowerCase().contains("service=")) {
            params.put("service", "WMS");
        }
        if(!url.toLowerCase().contains("getcapabilities=")) {
            params.put("request", "GetCapabilities");
        }

        return IOHelper.constructUrl(url, params);
    }
}