package fi.nls.oskari.wfs.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDResourceImpl;
import org.w3c.dom.Node;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * XML helper methods
 */
public class XMLHelper {

	private static final Logger log = LogFactory.getLogger(XMLHelper.class);

	/**
	 * Creates XML builder for reading XML
	 * 
	 * @param xml
	 * @return builder
	 */
	public static StAXOMBuilder createBuilder(String xml) {
		StringReader reader = new StringReader(xml);
		return createBuilder(reader);
	}

	/**
	 * Creates XML builder for reading XML
	 * 
	 * @param reader
	 * @return builder
	 */
	public static StAXOMBuilder createBuilder(Reader reader) {
		XMLStreamReader xmlStreamReader = null;
		StAXOMBuilder stAXOMBuilder = null;
		try {
			xmlStreamReader = XMLInputFactory.newInstance()
					.createXMLStreamReader(reader);
		} catch (XMLStreamException e) {
			log.error(e, "XML Stream error");
		} catch (FactoryConfigurationError e) {
			log.error(e, "XMLInputFactory configuration error");
		}
		if (xmlStreamReader != null) {
			stAXOMBuilder = new StAXOMBuilder(xmlStreamReader);
		}
		return stAXOMBuilder;
	}

	/**
	 * Transforms from XSDSchema to String
	 * 
	 * @param schema
	 * @return xsd
	 */
	public static String XSDSchemaToString(XSDSchema schema) {
		Node el = schema.getElement();
		TransformerFactory transFactory = TransformerFactory.newInstance();
		
		Transformer transformer = null;
		try {
			transformer = transFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			log.error(e, "Transformer couldn't be configured");
		}
		
		StringWriter buffer = new StringWriter();
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		
		try {
			transformer.transform(new DOMSource(el), new StreamResult(buffer));
		} catch (TransformerException e) {
			log.error(e, "Transform error");
		}
		
		buffer.flush();
		return buffer.toString();
	}
	
	/**
	 * Transforms from String to XSDSchema
	 * 
	 * @param str
	 * @return xsd
	 */
	public static XSDSchema StringToXSDSchema(String str) {
		InputStream stream = null;
		try {
			if(str != null) {
				stream = new ByteArrayInputStream(str.getBytes("UTF-8"));
			}
		} catch (UnsupportedEncodingException e) {
			log.error(e, "Encoding error");
		} catch (Exception e) {
			log.error(e, "Stream error");
		}
		
		if(stream != null) {
			return InputStreamToXSDSchema(stream);
		}
		
		return null;
	}
	
	/**
	 * Transforms from InputStream to XSDSchema
	 * 
	 * @param stream
	 * @return xsd
	 */
	public static XSDSchema InputStreamToXSDSchema(InputStream stream) {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xsd", new org.eclipse.xsd.util.XSDResourceFactoryImpl()); 
		XSDResourceImpl xsdMainResource = (XSDResourceImpl) resourceSet.createResource(URI.createURI(".xsd"));
		try {
			if(stream != null) {
				xsdMainResource.load(stream, resourceSet.getLoadOptions());
			}
		} catch (IOException e) {
			log.error(e, "IO error");
		}
		return xsdMainResource.getSchema();	
	}
}
