package fi.nls.oskari.control.statistics.plugins.kapa.requests;

import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataDimension;
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

/**
 * Kansallinen Palveluväylä statistics queries.
 */
public class KapaRequest {

    private static final Logger log = LogFactory.getLogger(KapaRequest.class);

    private static final String KAPA_ENCODING = "UTF-8";

    private static String kapaBaseURL;
    private static final String version = "1.0";

    public KapaRequest() {
        kapaBaseURL = PropertyUtil.get("kapa.baseurl");
    }

    public String getUrl(String method, Map<String, String> params) {
        StringWriter writer = new StringWriter();
        writer.write(kapaBaseURL);
        writer.write("/");
        writer.write(version);
        writer.write("/");
        writer.write(method);
        if (params != null && params.size() > 0) {
            writer.write("?");
            boolean first = true;
            for (Entry<String, String> keyValue : params.entrySet()) {
                try {
                    if (!first) {
                        writer.write("&");
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

    public String getIndicators() {
        return getData("indicators", null);
    }
    public String getIndicatorData(String indicator, StatisticalIndicatorDataModel selectors) {
        
        Map<String, String> parameters = new HashMap<>();
        for (StatisticalIndicatorDataDimension selector : selectors.getDimensions()) {
            parameters.put(selector.getId(), selector.getValue());
        }
        try {
            return getData("indicators/" + URLEncoder.encode(indicator, "UTF-8"), parameters);
        } catch (UnsupportedEncodingException e) {
            throw new APIException("Error while encoding request parameters.", e);
        }
    }
    public String getData(String method, Map<String, String> params) {
        HttpURLConnection con = null;
        try {
            final String url = getUrl(method, params);
            con = IOHelper.getConnection(url);

            final String data = IOHelper.readString(con.getInputStream(), KAPA_ENCODING);
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
