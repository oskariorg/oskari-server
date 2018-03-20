package fi.nls.oskari.csw.service;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.oskari.csw.helper.CSWISORecordNamespaceContext;
import fi.nls.oskari.csw.helper.CSWISORecordParser;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.XmlHelper;
import org.deegree.framework.xml.XMLException;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
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

    private static final Logger log = LogFactory
            .getLogger(CSWService.class);

    GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);

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
     * @throws XMLException
     * @throws OGCWebServiceException
     * @throws URISyntaxException
     * @throws IOException
     * @throws SAXException
     * @throws XMLParsingException
     * @throws XPathExpressionException
     * @throws ParseException
     * @throws TransformException
     */
    public CSWIsoRecord getRecordById(String uuid, String lang) throws SAXException, IOException, URISyntaxException, TransformException, XPathExpressionException, ParseException, ParserConfigurationException {
        CSWIsoRecord record = null;
        Node responseElement;
        Locale locale = new Locale(lang);
        final URL url = getGetRecordByIdUrl(uuid, lang);
        responseElement = invokeCswGetRecordById(url);

        if (responseElement == null) {
            return null;
        }
        if (CSWISORecordNamespaceContext.GMDNS.equals(responseElement.getNamespaceURI())) {
            record = mapIsoRecordElementToObject(responseElement, locale);
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
     * @throws URISyntaxException
     * @throws XMLException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    protected Node invokeCswGetRecordById(final URL url)
            throws URISyntaxException, IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory dbf = XmlHelper.newDocumentBuilderFactory();
        dbf.setNamespaceAware(true);

        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(url.openStream());
        Node root = doc.getDocumentElement();
        NodeList children = root.getChildNodes();
        Node ret = null;
        for (int i = 0; i < children.getLength(); i++) {
            if ("MD_Metadata".equals(children.item(i).getLocalName())) {
                ret = children.item(i);
            }
        }
        return ret;
    }

    private CSWIsoRecord mapIsoRecordElementToObject(Node el, Locale locale) throws XPathExpressionException, TransformException, ParseException {
        CSWISORecordParser parser = new CSWISORecordParser();
        CSWIsoRecord ret;
        ret = parser.parse(el, locale, transform);
        return ret;
    }

}