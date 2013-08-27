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
            gfiResponse = IOHelper.getURL(params.getGFIUrl(), headers);
        } catch (IOException e) {
            log.error(e, "Couldn't call GFI URL with params:", params);
            return null;
        }
        if (gfiResponse != null && !gfiResponse.isEmpty()) {
            final JSONObject response = new JSONObject();
            try {
                response.put(GetGeoPointDataService.TYPE, params.getLayer().getType());
                response.put(GetGeoPointDataService.LAYER_ID, params.getLayer().getId());
                final String xslt = params.getLayer().getXslt();
                if (xslt == null || xslt.isEmpty()) {
                    response.put(GetGeoPointDataService.PRESENTATION_TYPE, GetGeoPointDataService.PRESENTATION_TYPE_TEXT);
                    response.put(GetGeoPointDataService.CONTENT, gfiResponse);
                } else {
                    final String transformedResult = transformResponse(xslt, gfiResponse);
                    JSONObject respObj = new JSONObject(transformedResult);
                    response.put(GetGeoPointDataService.PRESENTATION_TYPE, GetGeoPointDataService.PRESENTATION_TYPE_JSON);
                    response.put(GetGeoPointDataService.CONTENT, respObj);
                }
            } catch (JSONException je) {
                log.error(je, "Couldn't construct GFI response from:", gfiResponse, "- params:", params);
                return null;
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
            log.error(e, "Error transforming GFI response: ", response, "- with XSLT:", xslt);
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
