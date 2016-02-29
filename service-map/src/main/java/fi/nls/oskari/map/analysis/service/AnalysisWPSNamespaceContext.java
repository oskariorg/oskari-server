package fi.nls.oskari.map.analysis.service;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AnalysisWPSNamespaceContext implements NamespaceContext {

	static final String ns_wps = "http://www.opengis.net/wps/1.0.0";
	static final String ns_wfs = "http://www.opengis.net/wfs";
	static final String ns_gml = "http://www.opengis.net/gml";
	static final String ns_wcs = "http://www.opengis.net/wcs/1.1.1";
	static final String ns_ows = "http://www.opengis.net/ows/1.1";
	static final String ns_ogc = "http://www.opengis.net/ogc";

	Map<String, String> ns2prefix = new HashMap<String, String>();
	Map<String, String> prefix2ns = new HashMap<String, String>();

	public AnalysisWPSNamespaceContext() {
		add("wps", ns_wps);
		add("wfs", ns_wfs);
		add("gml", ns_gml);
		add("wcs", ns_wcs);
		add("ows", ns_ows);
		add("ogc", ns_ogc);

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

class WPSVariableResolver implements XPathVariableResolver {

	Map<QName, Object> vars = new HashMap<QName, Object>();

	public void add(QName qname, Object value) {
		vars.put(qname, value);
	}

	public Object resolveVariable(QName var) {
		if (var == null)
			throw new NullPointerException("The variable name cannot be null");

		return vars.get(var);
	}
}
