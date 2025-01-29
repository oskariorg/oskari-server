package fi.nls.oskari.csw.service;

import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.oskari.csw.helper.CSWISORecordNamespaceContext;
import fi.nls.oskari.csw.helper.CSWISORecordParser;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.oskari.xml.XmlHelper;
import org.geotools.referencing.CRS;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Locale;

/**
 * getMetadata and getMetadataById to help linking schema catalogue and metadata
 * catalogue.
 *
 * @author JKORHONEN, TMIKKOLAINEN
 */
public class CSWService {

    public static final String PROP_SERVICE_URL = "service.metadata.url";
    private static final Logger log = LogFactory
            .getLogger(CSWService.class);

    String baseURL;
    private CoordinateReferenceSystem targetCRS;
    private CoordinateReferenceSystem sourceCRS;
    private MathTransform transform;
    private CSWISORecordNamespaceContext nsContext;

    public CSWService(String baseURL)
            throws URISyntaxException, XPathExpressionException, FactoryException {
        this(baseURL, null, null);
    }

    /**
     * @param baseURL       Base URL to Geonetwork CSW service
     * @param fromCRS
     * @param toCRS
     * @throws URISyntaxException
     * @throws XPathExpressionException
     * @throws FactoryException
     */
    public CSWService(String baseURL, String toCRS, String fromCRS)
            throws URISyntaxException, XPathExpressionException, FactoryException {
        nsContext = new CSWISORecordNamespaceContext();

        this.baseURL = baseURL;

        if (fromCRS != null && toCRS != null) {
            //output is always  lon,lat axis order
            targetCRS = CRS.decode(toCRS,true);
            //TOD0 find out source axis orientation
            sourceCRS = CRS.decode(fromCRS);
            transform = CRS.findMathTransform(sourceCRS, targetCRS);
        }
    }

    /**
     * loads metadata by fileIdentifier
     *
     * @param uuid
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws URISyntaxException
     * @throws TransformException
     * @throws XPathExpressionException
     * @throws ParseException
     * @throws ParserConfigurationException
     */
    public CSWIsoRecord getRecordById(String uuid, String lang) throws SAXException, IOException, URISyntaxException, TransformException, XPathExpressionException, ParseException, ParserConfigurationException {
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


    protected CSWIsoRecord mapIsoRecordElementToObject(Node el, String lang) throws XPathExpressionException, TransformException, ParseException {
        CSWISORecordParser parser = new CSWISORecordParser();
        Locale locale = new Locale(lang);
        return parser.parse(el, locale, transform);
    }

}