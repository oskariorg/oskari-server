package fi.nls.oskari.control.statistics.plugins.pxweb.requests;

import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelector;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicatorSelectors;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PxwebRequest {
    private static final Logger log = LogFactory.getLogger(PxwebRequest.class);

    private static final String PXWEB_ENCODING = "UTF-8";
    private static String pxwebBaseURL;

    public PxwebRequest() {
        pxwebBaseURL = PropertyUtil.get("pxweb.baseurl");
    }

    public String getIndicators() {
        return "";
    }


    public String getUrl(String method, Map<String, String> params) {
        StringWriter writer = new StringWriter();
        writer.write(pxwebBaseURL);
        writer.write(method);
        if (params != null && params.size() > 0) {
            boolean first = true;
            for (Entry<String, String> keyValue : params.entrySet()) {
                try {
                    if (!first) {
                    }
                    writer.write(URLEncoder.encode(keyValue.getKey(), "UTF-8"));
                    writer.write("=");
                    writer.write(URLEncoder.encode(keyValue.getValue(), "UTF-8"));
                    first = false;
                } catch (UnsupportedEncodingException e) {
                    throw new APIException("Error while encoding request parameters.", e);
                }

            }
        }

        return writer.toString();
    }


    public String getData(String method, Map<String, String> params) {
        HttpURLConnection con = null;
        try {
            final String url = getUrl(method, params);
            con = IOHelper.getConnection(url);

            final String data = IOHelper.readString(con.getInputStream());
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            throw new APIException("Couldn't request data from the KaPa server", e);
        } finally {
            try {
                con.disconnect();
            }
            catch (Exception ignored) {}
        }
    }


}
