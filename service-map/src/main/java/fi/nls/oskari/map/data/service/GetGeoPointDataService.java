package fi.nls.oskari.map.data.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.w3c.dom.Document;

import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.data.domain.GFIRequestParams;
import fi.nls.oskari.util.IOHelper;

public class GetGeoPointDataService {

    private Logger log = LogFactory.getLogger(GetGeoPointDataService.class);
    
    public static final String TYPE = "type";
    public static final String LAYER_ID = "layerId";
    public static final String PRESENTATION_TYPE = "presentationType";
    public static final String CONTENT = "content";
    public static final String GFI_CONTENT = "gfiContent";

    public static final String PRESENTATION_TYPE_JSON = "JSON";
    public static final String PRESENTATION_TYPE_TEXT = "TEXT";
    

    public JSONObject getWMSFeatureInfo(final GFIRequestParams params) {

        final Map<String, String> headers = new HashMap<String,String>();
        headers.put("User-Agent", "Mozilla/5.0 "
                + "(Windows; U; Windows NT 6.0; pl; rv:1.9.1.2) "
                + "Gecko/20090729 Firefox/3.5.2");
        headers.put("Referer", "/");
        headers.put("Cookie", "_ptifiut_");
        
        String gfiResponse;
        try {
            final String url = params.getGFIUrl();
            log.debug("Calling GFI url:", url);
            gfiResponse = IOHelper.getURL(url, headers);
            log.debug("Got GFI response:", gfiResponse);
        } catch (IOException e) {
            log.error(e, "Couldn't call GFI URL with params:", params);
            return null;
        }
        if (gfiResponse != null && !gfiResponse.isEmpty()) {
            final JSONObject response = new JSONObject();
            JSONHelper.putValue(response, TYPE, params.getLayer().getType());
            JSONHelper.putValue(response, LAYER_ID, params.getLayer().getId());
            // try transform if XSLT is provided
            final String xslt = params.getLayer().getGfiXslt();
            JSONObject respObj = null;
            if (xslt != null && !xslt.isEmpty()) {
                final String transformedResult = transformResponse(xslt, gfiResponse);
                respObj = JSONHelper.createJSONObject(transformedResult);
                if(respObj != null) {
                    JSONHelper.putValue(response, PRESENTATION_TYPE, PRESENTATION_TYPE_JSON);
                    JSONHelper.putValue(response, CONTENT, respObj);
                }
            }
            // use text content if respObj isn't present (transformed JSON not created)
            if(respObj == null) {
                JSONHelper.putValue(response, PRESENTATION_TYPE, PRESENTATION_TYPE_TEXT);
                JSONHelper.putValue(response, CONTENT, gfiResponse);
            }
            // Add gfi content, it needs to be a separate field so we can mangle it as we like in the frontend
            final String gfiContent = params.getLayer().getGfiContent();
            if (gfiContent != null) {
                JSONHelper.putValue(response, GFI_CONTENT, gfiContent);
            }
            return response;
        }
        return null;
    }

  
    private String transformResponse(final String xslt, final String response) {

        if (xslt == null || "".equals(xslt)) {
            // if not found, return as is
            return response;
        }

        ByteArrayInputStream respInStream = null;
        ByteArrayInputStream xsltInStream = null;
        Writer outWriter = null;
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            final DocumentBuilder builder = factory.newDocumentBuilder();

            respInStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
            final Document document = builder.parse(respInStream);
            xsltInStream = new ByteArrayInputStream(xslt.getBytes());
            final StreamSource stylesource = new StreamSource(xsltInStream);
            final String transformedResponse = getFormatedJSONString(document, stylesource);
            
            if (transformedResponse == null
                    || transformedResponse.isEmpty()) {
                log.info("got empty result from transform with:", xslt, " - Response:", response);
                return response;
            }

            return transformedResponse;
        } catch (Exception e) {
            log.error("Error transforming GFI response: ", response, "- with XSLT:", xslt,
                    "Error:", e.getMessage());
        } finally {
            if (respInStream != null) {
                try {
                    respInStream.close();
                } catch (Exception ignored) {
                }
            }
            if (xsltInStream != null) {
                try {
                    xsltInStream.close();
                } catch (Exception ignored) {
                }
            }
            if (outWriter != null) {
                try {
                    outWriter.close();
                } catch (Exception ignored) {
                }
            }
        }
        // Sanitize response
        return Jsoup.clean(response, Whitelist.relaxed());
    }

    
    public static String getFormatedJSONString(Document document, StreamSource stylesource) throws TransformerException {
        final Transformer transformer = TransformerFactory.newInstance()
                .newTransformer(stylesource);

        final DOMSource source = new DOMSource(document);
        final StringWriter outWriter = new StringWriter();
        final StreamResult result = new StreamResult(outWriter);
        transformer.transform(source, result);
        final String transformedResponse = outWriter.toString();
        return transformedResponse.trim();
    }
}
