package fi.nls.oskari.map.analysis.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import fi.nls.oskari.log.LogFactory;
import org.w3c.dom.Document;

import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.map.analysis.domain.AnalysisMethodParams;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

public class AnalysisWebProcessingService {

    private static final Logger log = LogFactory.getLogger(
            AnalysisWebProcessingService.class);

    private static final String GEOSERVER_WPS_URL = "geoserver.wps.url";

    /**
     * Get WPS results as wfs FeatureCollection
     * @param analysisLayer WPS method params
     * @return response of WPS (xml FeatureCollection)
     * @throws ServiceException
     */
    public String requestFeatureSet(final AnalysisLayer analysisLayer)  throws ServiceException {
        try {
            // 1) Get Analysis Specific WPS XML
            final AnalysisMethodParams methodParams = analysisLayer
                    .getAnalysisMethodParams();
            final Document doc = methodParams.getWPSXML2();
            return this.requestWPS(doc);
        } catch (Exception e) {
            throw new ServiceException("requestFeatureSet failed due to wps request build", e);
        }
    }

    /**
     *  Get WPS execute response
     * @param doc  WPS execute request (xml)
     * @return
     * @throws ServiceException
     */
    private String requestWPS(final Document doc)
            throws ServiceException {
        InputStream inp = null;
        try {
            final String wpsUrl = PropertyUtil.get(GEOSERVER_WPS_URL);
            final String wpsUser = PropertyUtil.get("geoserver.wms.user");
            final String wpsUserPass = PropertyUtil.get("geoserver.wms.pass");

            final HttpURLConnection connection = IOHelper.getConnection(wpsUrl, wpsUser, wpsUserPass);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type",
                    "application/xml; charset=UTF-8");

            final OutputStream outs = connection.getOutputStream();

            // 2) Transform XML to POST body
            // Use a Transformer for output
            final TransformerFactory tFactory = TransformerFactory
                    .newInstance();
            final Transformer transformer = tFactory.newTransformer();

            final DOMSource source = new DOMSource(doc);
            final StreamResult result = new StreamResult(outs);
            transformer.transform(source, result);

            if (log.isDebugEnabled()) {
                final DOMSource source2 = new DOMSource(doc);
                final StreamResult result2 = new StreamResult(System.out);
                transformer.transform(source2, result2);
            }
            // 4) Returned WPS result
            outs.close();
            inp = connection.getInputStream();
            final String results = IOHelper.readString(inp);
            // log.debug("We got results from GeoServer WPS", results);
            log.debug("We got results from GeoServer WPS");
            return results;

        } catch (Exception e) {
            throw new ServiceException("requestFeatureSet failed due to", e);
        } finally {
            try {
                if(inp != null) {
                    inp.close();
                }
            } catch (Exception ignored) {
            }
        }
    }
}
