package fi.nls.oskari.search.ktjkiiwfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import fi.nls.oskari.log.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import fi.nls.oskari.log.Logger;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 
 * @author JKORHONEN
 * 
 *         Search Channel that requests Register Units for The Finnish Cadastral
 *         Registry.
 * 
 *         Request: A String of type aaA-bbB-cccC-dddD
 * 
 *         Request Processing Only Valid requests are processed.
 * 
 *         A Valid request String is converted to 'Database form' AAABBBCCCCDDDD
 * 
 *         Feature Type (PalstanTunnuspisteenSijaintitiedot does not support
 *         query by Register unit i otherwise it would have been used)
 *         PalstanTietoja is queried by property
 *         rekisteriyksikonKiinteistotunnus (register unit id)
 * 
 *         Feature Result Properties ktjkiiwfs:rekisteriyksikonKiinteistotunnus
 *         ktjkiiwfs:tunnuspisteSijainti
 * 
 *         Response Processing Response is processed to POJOs that will be
 *         transformed to Search Channel JSON
 * 
 * 
 *         <wfs:GetFeature xmlns:ktjkiiwfs="http://xml.nls.fi/ktjkiiwfs/2010/02"
 *         xmlns:wfs="http://www.opengis.net/wfs"
 *         xmlns:gml="http://www.opengis.net/gml"
 *         xmlns:ogc="http://www.opengis.net/ogc"
 *         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.1.0"
 *         xsi:schemaLocation=
 *         "http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd"
 *         > <wfs:Query typeName="ktjkiiwfs:PalstanTietoja" srsName="EPSG:2393">
 *         <wfs:PropertyName>ktjkiiwfs:rekisteriyksikonKiinteistotunnus</wfs:
 *         PropertyName>
 *         <wfs:PropertyName>ktjkiiwfs:tunnuspisteSijainti</wfs:PropertyName>
 *         <ogc:Filter> <ogc:PropertyIsEqualTo>
 *         <ogc:PropertyName>ktjkiiwfs:rekisteriyksikonKiinteistotunnus
 *         </ogc:PropertyName> <ogc:Literal>21442500160044</ogc:Literal>
 *         </ogc:PropertyIsEqualTo> </ogc:Filter> </wfs:Query> </wfs:GetFeature>
 * 
 *         <wfs:FeatureCollection
 *         xmlns:ktjkiiwfs="http://xml.nls.fi/ktjkiiwfs/2010/02"
 *         xmlns:wfs="http://www.opengis.net/wfs"
 *         xmlns:xlink="http://www.w3.org/1999/xlink"
 *         xmlns:gml="http://www.opengis.net/gml"
 *         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *         numberOfFeatures="2" xsi:schemaLocation=
 *         "http://xml.nls.fi/ktjkiiwfs/2010/02 http://ktjkiiwfs.nls.fi/ktjkii/wfs/wfs?SERVICE=WFS&VERSION=1.1.0&REQUEST=DescribeFeatureType&TYPENAME=ktjkiiwfs:PalstanTietoja&NAMESPACE=xmlns(ktjkiiwfs=http://xml.nls.fi/ktjkiiwfs/2010/02) http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd"
 *         > <gml:boundedBy> <gml:Envelope srsName="EPSG:2393">
 *         <gml:lowerCorner>6854214.407 3256421.651</gml:lowerCorner>
 *         <gml:upperCorner>6854304.671 3257168.342</gml:upperCorner>
 *         </gml:Envelope> </gml:boundedBy> <gml:featureMember>
 *         <ktjkiiwfs:PalstanTietoja
 *         gml:id="FI.KTJkii-PalstanTietoja-9029395820111017"> <gml:boundedBy>
 *         <gml:Envelope srsName="EPSG:2393"> <gml:lowerCorner>6854304.671
 *         3257168.342</gml:lowerCorner> <gml:upperCorner>6854304.671
 *         3257168.342</gml:upperCorner> </gml:Envelope> </gml:boundedBy>
 *         <ktjkiiwfs:paivityspvm>20090926</ktjkiiwfs:paivityspvm>
 *         <ktjkiiwfs:rekisteriyksikonKiinteistotunnus
 *         >21442500160044</ktjkiiwfs:rekisteriyksikonKiinteistotunnus>
 *         <ktjkiiwfs:tunnuspisteSijainti> <gml:Point srsName="EPSG:2393">
 *         <gml:pos>6854304.671 3257168.342</gml:pos> </gml:Point>
 *         </ktjkiiwfs:tunnuspisteSijainti> </ktjkiiwfs:PalstanTietoja>
 *         </gml:featureMember> <gml:featureMember> <ktjkiiwfs:PalstanTietoja
 *         gml:id="FI.KTJkii-PalstanTietoja-9029396420111017"> <gml:boundedBy>
 *         <gml:Envelope srsName="EPSG:2393"> <gml:lowerCorner>6854214.407
 *         3256421.651</gml:lowerCorner> <gml:upperCorner>6854214.407
 *         3256421.651</gml:upperCorner> </gml:Envelope> </gml:boundedBy>
 *         <ktjkiiwfs:paivityspvm>20090925</ktjkiiwfs:paivityspvm>
 *         <ktjkiiwfs:rekisteriyksikonKiinteistotunnus
 *         >21442500160044</ktjkiiwfs:rekisteriyksikonKiinteistotunnus>
 *         <ktjkiiwfs:tunnuspisteSijainti> <gml:Point srsName="EPSG:2393">
 *         <gml:pos>6854214.407 3256421.651</gml:pos> </gml:Point>
 *         </ktjkiiwfs:tunnuspisteSijainti> </ktjkiiwfs:PalstanTietoja>
 *         </gml:featureMember> </wfs:FeatureCollection>
 * 
 */

public class KTJkiiWFSSearchChannelImpl implements KTJkiiWFSSearchChannel {

	public class RegisterUnitIdImpl implements RegisterUnitId {
		String value = null;

		RegisterUnitIdImpl(String value) {
			this.value = value;
		}

		public String getValue() {
			// TODO Auto-generated method stub
			return value;
		}

	}

	private final static Logger logger = LogFactory.getLogger(KTJkiiWFSSearchChannelImpl.class);

	KTJkiiWFSNamespaceContext nscontext = new KTJkiiWFSNamespaceContext();

	URL serviceURL;
    String host;
    String auth;

	public void setServiceURL(URL url) {
		this.serviceURL = url;
	}

    public void setHost(String host) {
        this.host = host;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }



	/**
	 * 
	 * @param requestString
	 * @return
	 */
	public RegisterUnitId convertRequestStringToRegisterUnitID(
			String requestString) {

		if (requestString == null)
			return null;
		if (requestString.isEmpty())
			return null;

		// System.out.println(requestString + " > ");

		Pattern finishedAlreadyPattern = Pattern.compile("^(\\d{14})$");
		Matcher finishedAlreadyMatcher = finishedAlreadyPattern
				.matcher(requestString);
		while (finishedAlreadyMatcher.find()) {
			String group = finishedAlreadyMatcher.group();
			return new RegisterUnitIdImpl(group);
		}

		Pattern overallPattern = Pattern
				.compile("^(\\d{1,3})\\-{1}(\\d{1,3})\\-{1}(\\d{1,4})\\-{1}(\\d{1,4})$");

		Matcher matcher = overallPattern.matcher(requestString);

		boolean found = false;
		while (matcher.find()) {
			String group = matcher.group();
			// System.out.println("overall [" + group + "]");
			found = true;
		}
		if (!found) {
			return null;
		}

		/** assuming overallPattern handled any errors... */
		Pattern partsPattern = Pattern.compile("(\\d{1,4})");
		Matcher partsMatcher = partsPattern.matcher(requestString);

		StringBuffer resultString = new StringBuffer();

		String formats[] = { "%03d", "%03d", "%04d", "%04d" };

		for (int n = 0; partsMatcher.find(); n++) {
			if (n > 3)
				return null; // confused

			String group = partsMatcher.group();
			// System.out.println("part [" + group + "]");

			resultString.append(String.format(formats[n],
					Integer.valueOf(group)));

		}

		String result = resultString.toString();
		logger.info(result);

		return new RegisterUnitIdImpl(result);
	}

	/**
	 * 
	 * @param registerUnitId
	 * @param outs
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws TransformerException
	 */
	public void buildParcelFeatureQueryToStream(RegisterUnitId registerUnitId,
			OutputStream outs) throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException,
			TransformerException {

		buildQueryToStream("ktjkiiwfs-query-template.xml", registerUnitId, outs);
	}
	
	/**
	 * 
	 * @param registerUnitId
	 * @param outs
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws TransformerException
	 */
	public void buildRegisterUnitFeatureQueryToStream(RegisterUnitId registerUnitId,
			OutputStream outs) throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException,
			TransformerException {

		buildQueryToStream("ktjkiiwfs-register-unit-query-template.xml", registerUnitId, outs);
	}

	/**
	 * 
	 * @param registerUnitId
	 * @param outs
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws TransformerException
	 */
	public void buildQueryToStream(String resourceName,
			RegisterUnitId registerUnitId, OutputStream outs)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException, TransformerException {

		String requestedRegisterUnitId = registerUnitId.getValue();

		logger.info("buildParcelFeatureQueryToStream "
				+ requestedRegisterUnitId);
		/**
		 * 1) Read Query Template
		 */
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		Document doc = null;
		builder = factory.newDocumentBuilder();

		InputStream inp = this.getClass().getResourceAsStream(resourceName);

		doc = builder.parse(inp);
		inp.close();

		// Create a XPathFactory
		XPathFactory xFactory = XPathFactory.newInstance();

		// Create a XPath object
		XPath xpath = xFactory.newXPath();
		xpath.setNamespaceContext(nscontext);

		// Compile the XPath expression
		XPathExpression expr = xpath
				.compile("//ogc:Literal[.='{REGISTER-UNIT-ID}']");

		Node nd = (Node) expr.evaluate(doc, XPathConstants.NODE);

		nd.setTextContent(requestedRegisterUnitId);

		/**
		 * 2) Fix registerUnitID to //ogc:Literal[.='{REGISTER-UNIT-ID}']
		 */

		/**
		 * 3) Transform XML to POST body
		 * 
		 */
		// Use a Transformer for output
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(outs);
		transformer.transform(source, result);

		/**
		 * 4) Process RESPONSE
		 */

	}

	/**
	 * Processes Response to a Set of Response Objects
	 * 
	 * @param registerUnitId
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 */
	public List<RegisterUnitParcelSearchResult> processParcelFeatureResponseFromStream(
			RegisterUnitId registerUnitId, InputStream inp)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		String requestedRegisterUnitId = registerUnitId.getValue();

		logger.info("processResponseFromStream " + requestedRegisterUnitId);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		Document doc = null;
		builder = factory.newDocumentBuilder();
		doc = builder.parse(inp);

		// Create a XPathFactory
		XPathFactory xFactory = XPathFactory.newInstance();

		// Create a XPath object
		XPath xpath = xFactory.newXPath();
		xpath.setNamespaceContext(nscontext);

		KTJkiiWFSVariableResolver varresolver = new KTJkiiWFSVariableResolver();
		varresolver.add(new QName("registerUnitID"), requestedRegisterUnitId);

		xpath.setXPathVariableResolver(varresolver);

		// Compile the XPath expression
		XPathExpression expr = xpath
				.compile("//ktjkiiwfs:PalstanTietoja[ktjkiiwfs:rekisteriyksikonKiinteistotunnus=$registerUnitID]");
						/*+ "/ktjkiiwfs:tunnuspisteSijainti/gml:Point/gml:pos/text()");*/

		XPathExpression exprTunnuspisteSijaintiPointPosText = xpath
				.compile("ktjkiiwfs:tunnuspisteSijainti/gml:Point/gml:pos/text()");


		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		if (nodes == null)
			return null;

		// System.out.println();
		if (nodes.getLength() == 0)
			return null;

        // bbox of GetFeature response
        String bbox = getFirstNodeValue(doc, nscontext.getNamespaceURI("gml"), "lowerCorner") + " " + getFirstNodeValue(doc, nscontext.getNamespaceURI("gml"), "upperCorner");

		ArrayList<RegisterUnitParcelSearchResult> results = new ArrayList<RegisterUnitParcelSearchResult>(
				nodes.getLength());

		for (int n = 0; n < nodes.getLength(); n++) {

			Node ndPt = nodes.item(n);
			
			String gmlID = ndPt.getAttributes().getNamedItemNS(nscontext.getNamespaceURI("gml"), "id").getTextContent() ;
			
			Node nd = (Node) exprTunnuspisteSijaintiPointPosText.evaluate(ndPt, XPathConstants.NODE);
			/*Node nd = nodes.item(n);*/
			
			if (nd.getNodeType() == Node.TEXT_NODE) {

				String val = nd.getTextContent();
				String[] EN = val.split(" ");
				if (EN == null)
					continue;
				if (EN.length < 2)
					continue;

				String E = EN[0];
				String N = EN[1];

				RegisterUnitParcelSearchResult rupsr = new RegisterUnitParcelSearchResult();
				rupsr.setGmlID(gmlID);
				rupsr.setRegisterUnitID(requestedRegisterUnitId);
				rupsr.setE(E);
				rupsr.setN(N);
                rupsr.setBBOX(bbox);

				results.add(rupsr);
			}
		}

		return results;
	}
	

	/**
	 * Processes Response to a Set of Response Objects
	 * 
	 * @param registerUnitId
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 */
	public List<RegisterUnitParcelSearchResult> processRegisterUnitFeatureResponseFromStream(
			RegisterUnitId registerUnitId, InputStream inp)
			throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		String requestedRegisterUnitId = registerUnitId.getValue();

		logger.info("processResponseFromStream " + requestedRegisterUnitId);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		Document doc = null;
		builder = factory.newDocumentBuilder();
		doc = builder.parse(inp);

		// Create a XPathFactory
		XPathFactory xFactory = XPathFactory.newInstance();

		// Create a XPath object
		XPath xpath = xFactory.newXPath();
		xpath.setNamespaceContext(nscontext);

		KTJkiiWFSVariableResolver varresolver = new KTJkiiWFSVariableResolver();
		varresolver.add(new QName("registerUnitID"), requestedRegisterUnitId);

		xpath.setXPathVariableResolver(varresolver);

		XPathExpression exprGmlId = xpath
				.compile("//ktjkiiwfs:RekisteriyksikonTietoja[ktjkiiwfs:kiinteistotunnus=$registerUnitID]/@gml:id");
		
		String registerUnitGmlId = (String) exprGmlId.evaluate(doc,XPathConstants.STRING);
		
		// Compile the XPath expression
		XPathExpression expr = xpath
				.compile("//ktjkiiwfs:RekisteriyksikonTietoja[ktjkiiwfs:kiinteistotunnus=$registerUnitID]/"+
						"ktjkiiwfs:rekisteriyksikonPalstanTietoja/" +
						"ktjkiiwfs:RekisteriyksikonPalstanTietoja/" +
						"ktjkiiwfs:tunnuspisteSijainti/gml:Point/gml:pos/text()");

		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		if (nodes == null)
			return null;

		// System.out.println();
		if (nodes.getLength() == 0)
			return null;

		ArrayList<RegisterUnitParcelSearchResult> results = new ArrayList<RegisterUnitParcelSearchResult>(
				nodes.getLength());

		for (int n = 0; n < nodes.getLength(); n++) {

			Node nd = nodes.item(n);
			if (nd.getNodeType() == Node.TEXT_NODE) {

				String val = nd.getTextContent();
				String[] EN = val.split(" ");
				if (EN == null)
					continue;
				if (EN.length < 2)
					continue;

				String E = EN[0];
				String N = EN[1];

				RegisterUnitParcelSearchResult rupsr = new RegisterUnitParcelSearchResult();
				rupsr.setRegisterUnitID(requestedRegisterUnitId);
				rupsr.setE(E);
				rupsr.setN(N);
				
				rupsr.setRegisterUnitGmlID(registerUnitGmlId);

				results.add(rupsr);
			}
		}

		return results;
	}
	

	/**
	 * default implementation using Feature PalstanTietoja
	 * 
	 */
	
	public List<RegisterUnitParcelSearchResult> searchByRegisterUnitIdWithParcelFeature(
			RegisterUnitId registerUnitId) throws IOException {
		if (registerUnitId == null) {
			return null;
		}

		String requestedRegisterUnitId = registerUnitId.getValue();

		logger.info("searchByRegisterUnitId " + requestedRegisterUnitId);

		RegisterUnitId registerUnitID = convertRequestStringToRegisterUnitID(requestedRegisterUnitId);
		if (registerUnitID == null) {
			return null;
		}

		logger.info("searchByRegisterUnitId -> " + registerUnitID);

		List<RegisterUnitParcelSearchResult> results = null;

		logger.info("opening -> " + serviceURL);

		URLConnection connection = getUrlConnection();//serviceURL.openConnection();
		//connection.setDoOutput(true);

		OutputStream outs = connection.getOutputStream();
		try {
			try {
				logger.info("searchByRegisterUnitId -> " + registerUnitID
						+ " sending Request");
				buildParcelFeatureQueryToStream(registerUnitID, outs);
			} finally {
				outs.close();
			}
			logger.info("searchByRegisterUnitId -> " + registerUnitID
					+ " reading response");
			InputStream inp = connection.getInputStream();
			try {
				results = processParcelFeatureResponseFromStream(registerUnitID, inp);
			} finally {
				inp.close();
			}

			logger.info("searchByRegisterUnitId -> " + registerUnitID
					+ " finished");

		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return results;

	}


    private URLConnection getUrlConnection() throws IOException {
        URLConnection connection = serviceURL.openConnection();
        if (host != null)  {
            connection.addRequestProperty("Host",host);
        }

        if (auth != null) {
            connection.addRequestProperty("Authorization", auth);
        }
        connection.setDoOutput(true);

        return connection;
    }

	/**
	 * alternate search via Feature RekisteriyksikonTietoja
	 * 
	 */

	public List<RegisterUnitParcelSearchResult> searchByRegisterUnitIdWithRegisterUnitFeature(
			RegisterUnitId registerUnitId) throws IOException,
			XPathExpressionException, ParserConfigurationException,
			SAXException, TransformerException {
		if (registerUnitId == null) {
			return null;
		}

		String requestedRegisterUnitId = registerUnitId.getValue();

		logger.info("searchByRegisterUnitId " + requestedRegisterUnitId);

		RegisterUnitId registerUnitID = convertRequestStringToRegisterUnitID(requestedRegisterUnitId);
		if (registerUnitID == null) {
			return null;
		}

		logger.info("searchByRegisterUnitId -> " + registerUnitID);

		List<RegisterUnitParcelSearchResult> results = null;

		logger.info("opening -> " + serviceURL);

        URLConnection connection = getUrlConnection();//serviceURL.openConnection();
		//connection.setDoOutput(true);

		OutputStream outs = connection.getOutputStream();
		try {
			try {
				logger.info("searchByRegisterUnitId -> " + registerUnitID
						+ " sending Request");
				buildRegisterUnitFeatureQueryToStream(registerUnitID, outs);
			} finally {
				outs.close();
			}
			logger.info("searchByRegisterUnitId -> " + registerUnitID
					+ " reading response");
			InputStream inp = connection.getInputStream();
			try {
				results = processRegisterUnitFeatureResponseFromStream(registerUnitID, inp);
			} finally {
				inp.close();
			}

			logger.info("searchByRegisterUnitId -> " + registerUnitID
					+ " finished");

		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return results;

	
	}

	/**
	 * currently routes to searchByRegisterUnitIdWithRegisterUnitFeature
	 */
	public List<RegisterUnitParcelSearchResult> searchByRegisterUnitId(
			RegisterUnitId registerUnitId) throws IOException,
			XPathExpressionException, ParserConfigurationException,
			SAXException, TransformerException {

		return searchByRegisterUnitIdWithRegisterUnitFeature(registerUnitId);
	}

    public String getFirstNodeValue(Document doc, String ns, String elem) {
        NodeList nl = doc.getElementsByTagNameNS(ns, elem);
        if (nl != null && nl.getLength() > 0) {
            return nl.item(0).getTextContent();
        }
        return null;
    }

}

class KTJkiiWFSNamespaceContext implements NamespaceContext {

	static final String ns_ktjkiiwfs = "http://xml.nls.fi/ktjkiiwfs/2010/02";
	static final String ns_wfs = "http://www.opengis.net/wfs";
	static final String ns_gml = "http://www.opengis.net/gml";
	static final String ns_ogc = "http://www.opengis.net/ogc";

	Map<String, String> ns2prefix = new HashMap<String, String>();
	Map<String, String> prefix2ns = new HashMap<String, String>();

	KTJkiiWFSNamespaceContext() {
		add("ktjkiiwfs", ns_ktjkiiwfs);
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

class KTJkiiWFSVariableResolver implements XPathVariableResolver {

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
