package fi.nls.oskari.csw.service;

import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.oskari.csw.helper.CSWISORecordNamespaceContext;
import fi.nls.oskari.csw.helper.CSWISORecordParser;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.oskari.xml.XmlHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * getMetadata and getMetadataById to help linking schema catalogue and metadata
 * catalogue.
 */
public class CSWService {

    public static final String PROP_SERVICE_URL = "service.metadata.url";

    private static final Logger log = LogFactory.getLogger(CSWService.class);

    private final String baseURL;

    public CSWService(String baseURL) {
        this.baseURL = baseURL;
    }

    /**
     * loads metadata by fileIdentifier
     *
     * @param uuid
     * @return
     * @throws IOException
     */
    public CSWIsoRecord getRecordById(String uuid, String lang) throws IOException {
        CSWIsoRecord record;
        final URL url = getGetRecordByIdUrl(uuid, lang);
        Node responseElement = invokeCswGetRecordById(url);

        if (responseElement == null) {
            return null;
        }
        if (CSWISORecordNamespaceContext.GMDNS.equals(responseElement.getNamespaceURI())) {
            record = mapIsoRecordElementToObject(responseElement, lang);
        } else {
            throw new IOException("Invalid response");
        }
        record.setMetadataURL(url);
        return record;
    }

    protected URL getGetRecordByIdUrl(String id, String lang) throws MalformedURLException {
        final String elementSetName = "full";
        // This is basically just the 'right' way to say csw:IsoRecord
        final String outputSchema = "http://www.isotc211.org/2005/gmd";
        final String request = "GetRecordById";
        final String service = "CSW";
        final String version = "2.0.2";

        // Using metadatacsw url as it has absolute image URLs
        final URL url = new URL(
                baseURL +
                        "?elementSetName=" + elementSetName +
                        "&id=" + id +
                        "&outputSchema=" + outputSchema +
                        "&Request=" + request +
                        "&service=" + service +
                        "&version=" + version
        );
        log.debug("GetRecordById URL: " + url.toString());
        return url;
    }

    /**
     * helper to invoke csw query
     *
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    protected Node invokeCswGetRecordById(final URL url)
            throws IOException {

        HttpURLConnection con = IOHelper.followRedirect(
                IOHelper.getConnection(url.toString()), 5);
        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Couldn't connect to service. Got response code " + con.getResponseCode());
        }
        try (InputStream in = con.getInputStream()) {
            return getMetadataRoot(in);
        } catch (Exception e) {
            throw new IOException("Unable to parse XML from " + url, e);
        }
    }

    private Node getMetadataRoot(InputStream in) throws Exception {
        Element root = XmlHelper.parseXML(in, true);
        Element metadata = XmlHelper.getFirstChild(root, "MD_Metadata");
        if (metadata == null) {
            throw new EOFException("No 'MD_Metadata' element in metadata");
        }
        return metadata;
    }


    protected CSWIsoRecord mapIsoRecordElementToObject(Node el, String lang) {
        Locale locale = new Locale(lang);
        return CSWISORecordParser.parse(el, locale);
    }

}