package fi.nls.oskari.csw.helper;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by TMIKKOLAINEN on 2.9.2014.
 */
public class CSWISORecordNamespaceContext implements NamespaceContext, org.jaxen.NamespaceContext {
    public static final String CSWNS = "http://www.opengis.net/cat/csw/2.0.2";
    public static final String DCNS = "http://purl.org/dc/elements/1.1/";
    public static final String DCTNS = "http://purl.org/dc/terms/";
    public static final String GCONS = "http://www.isotc211.org/2005/gco";
    public static final String GMDNS = "http://www.isotc211.org/2005/gmd";
    public static final String GMLNS = "http://www.opengis.net/gml";
    public static final String SRVNS = "http://www.isotc211.org/2005/srv";
    private Map<String, URI> nsmap = new HashMap<String, URI>();

    private static final Logger log = LogFactory
            .getLogger(CSWISORecordNamespaceContext.class);

    public CSWISORecordNamespaceContext() {
        try {
            nsmap.put("csw", new URI(CSWNS));
            nsmap.put("dc", new URI(DCNS));
            nsmap.put("dct", new URI(DCTNS));
            nsmap.put("gco", new URI(GCONS));
            nsmap.put("gmd", new URI(GMDNS));
            nsmap.put("gml", new URI(GMLNS));
            nsmap.put("srv", new URI(SRVNS));
        } catch (URISyntaxException e) {
            log.error(e, "Error setting up namespace context");
        }
    }

    public Map<String, URI> getNsmap() {
        return nsmap;
    }

    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("Null prefix");
        } else if ("xml".equals(prefix)) {
            return XMLConstants.NULL_NS_URI;
        }
        URI ret = nsmap.get(prefix);
        if (ret == null) {
            log.debug("No URI found for " + prefix);
        }
        return ret == null ? null : nsmap.get(prefix).toString();
    }

    public String translateNamespacePrefixToUri(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return null;
        } else if ("xml".equals(prefix)) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        URI uri = nsmap.get(prefix);
        if (uri == null) {
            return null;
        }
        return uri.toString();
    }

    // This method isn't necessary for XPath processing.
    public String getPrefix(String uri) {
        throw new UnsupportedOperationException();
    }

    // This method isn't necessary for XPath processing either.
    public Iterator<String> getPrefixes(String uri) {
        throw new UnsupportedOperationException();
    }
}
