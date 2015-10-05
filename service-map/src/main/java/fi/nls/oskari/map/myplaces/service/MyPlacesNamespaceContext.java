package fi.nls.oskari.map.myplaces.service;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class MyPlacesNamespaceContext implements NamespaceContext {

    static final String ns_wfs = "http://www.opengis.net/wfs";
    static final String ns_gml = "http://www.opengis.net/gml";
    static final String ns_ogc = "http://www.opengis.net/ogc";
    

    Map<String, String> ns2prefix = new HashMap<String, String>();
    Map<String, String> prefix2ns = new HashMap<String, String>();

    MyPlacesNamespaceContext() {
        add("wfs", ns_wfs);
        add("ogc", ns_ogc);
        add("gml", ns_gml);
    }

    public void add(String prefix, String ns) {
        ns2prefix.put(ns, prefix);
        prefix2ns.put(prefix, ns);
    }

    public String getNamespaceURI(String prefix) {
        if (prefix == null)
            throw new NullPointerException("Null prefix");

        String ns = prefix2ns.get(prefix);

        return ns != null ? ns : XMLConstants.NULL_NS_URI;
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
