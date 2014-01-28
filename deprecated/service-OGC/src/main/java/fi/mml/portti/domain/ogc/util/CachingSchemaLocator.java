package fi.mml.portti.domain.ogc.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDResourceImpl;
import org.eclipse.xsd.util.XSDSchemaLocator;
import org.geotools.xml.Schemas;

import fi.mml.portti.domain.ogc.util.http.EasyHttpClient;
import fi.mml.portti.domain.ogc.util.http.HttpPostResponse;

public class CachingSchemaLocator implements XSDSchemaLocator {

	/** Concurrent hashmap cache for schemas. Note that it is static */
	private static Map<String, XSDSchema> cache = new ConcurrentHashMap<String, XSDSchema>();
	
	private String userName;
	
	private String passWord;
	
	private boolean useProxy;
	
	public CachingSchemaLocator(String username, String password, boolean useProxy) {
		this.userName = username;
		this.passWord = password;
		this.useProxy = useProxy;
	}
	
	@Override
	public XSDSchema locateSchema(XSDSchema schema, String namespaceURI,
	        String rawSchemaLocationURI, String resolvedSchemaLocationURI) {
		try {
			String location = rawSchemaLocationURI;
			
			if (location == null) {
				return null;
			}
			
			XSDSchema foundSchema = cache.get(location);
			if (foundSchema != null) {
				return foundSchema;
			}
			
			/* Here we decide how schema should be retrieved.
			 * If url starts with "https" it can be password protected
			 * with http basic authentication
			 * 
			 * This code will not work in cases where resource is behind
			 * http basic authentication, but uses http protocol without "s".
			 * 
			 * Basic authentication without "https" is considered a bad
			 * idea because password is sent in clear text. If such 
			 * implementation is needed in future, you must replace this
			 * by using this:
			 * 
			 * http://download.oracle.com/javase/6/docs/api/java/net/Authenticator.html
			 */
			try {
				if (location.toLowerCase().startsWith("https")) {
					final ResourceSet resourceSet = new ResourceSetImpl();
					
					XSDResourceImpl xsdMainResource = (XSDResourceImpl) resourceSet.createResource(URI.createURI(".xsd"));
				    HttpPostResponse pr = EasyHttpClient.post(location, userName, passWord, null, useProxy);
				    try {
				    	xsdMainResource.load(pr.getResponseAsInputStream(),resourceSet.getLoadOptions());
				    	foundSchema = xsdMainResource.getSchema();
				    } finally {
				    	pr.closeResponseStream();
				    }
				} else {
					foundSchema = Schemas.parse(location);
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed to locate Schema '" + location + "'");
			}
		    
		    
			if (foundSchema != null) {
				cache.put(location, foundSchema);
			}
			return foundSchema;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
