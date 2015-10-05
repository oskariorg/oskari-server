package fi.nls.oskari.wfs;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.wfs.util.HttpHelper;
import fi.nls.oskari.wfs.util.XMLHelper;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDSchemaLocator;
import org.geotools.xml.Schemas;

import java.io.BufferedInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * XSD Schema loader with caching for Geotools' XML parser
 */
public class CachingSchemaLocator implements XSDSchemaLocator {

	private static final String cacheHashKey = "hSchemas";
	
    private static final Logger log = LogFactory.getLogger(CachingSchemaLocator.class);

    private static final long ERROR_REPORT_QUIET_PERIOD_MS = 30 * 60 * 1000;

	private static Map<String, XSDSchema> cache = new ConcurrentHashMap<String, XSDSchema>();
    private static Map<String, Long> errorTracker = new ConcurrentHashMap<String, Long>();
	private String username;
	private String password;

	/**
	 * Constructs loader
	 * 
	 * @param username
	 * @param password
	 */
	public CachingSchemaLocator(String username, String password) {
		this.username = username;
		this.password = password;
		if(cache.size() == 0) {
			init();
		}
	}
	
	/**
	 * Init cache from jedis
	 */
	public static void init() {
		log.debug("Init schemas");
		Set<String> schemas = JedisManager.hkeys(cacheHashKey);
		Iterator<String> it = schemas.iterator();
		while(it.hasNext()) {
			String field = (String)it.next();
			String str = JedisManager.hget(cacheHashKey, field);
			XSDSchema xsd = XMLHelper.StringToXSDSchema(str);
			cache.put(field, xsd);
		}
	}

    /**
     * Flush cache map and redis
     */
    public static void flushAll() {
        cache.clear();
        errorTracker.clear();
        JedisManager.del(cacheHashKey);
    }

	public static long getCacheSize() {
		return cache.size();
	}
	
	/**
	 * Loads schema from given location and caches it
	 * 
	 * @param schema
	 * @param namespaceURI
	 * @param rawSchemaLocationURI
	 * @param resolvedSchemaLocationURI
	 * 
	 * @see org.eclipse.xsd.util.XSDSchemaLocator#locateSchema(org.eclipse.xsd.XSDSchema, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
    public XSDSchema locateSchema(
    		XSDSchema schema, 
    		String namespaceURI, 
    		String rawSchemaLocationURI, 
    		String resolvedSchemaLocationURI) {
		
		String url = rawSchemaLocationURI;
		if (url == null) {
			return null;
		}
		
		XSDSchema foundSchema = cache.get(url);
		if (foundSchema != null) {
			return foundSchema;
		}
		
		try {
			if (url.toLowerCase().startsWith("https")) {
				BufferedInputStream response = HttpHelper.getRequestStream(url, "application/xml", username, password);
				foundSchema = XMLHelper.InputStreamToXSDSchema(response);
			} else {			
				foundSchema = Schemas.parse(url);
			}
		} catch (Exception e) {
            final Long lastError = errorTracker.get(url);
            long now = System.currentTimeMillis();
            // only log the same error once per ERROR_REPORT_QUIET_PERIOD_MS
            if(lastError == null || (now - lastError.longValue() > ERROR_REPORT_QUIET_PERIOD_MS) ) {
                errorTracker.put(url, now);
                log.error("Failed to locate Schema '" + url + "' - Error message:", e.getMessage());
            }
            else {
                log.debug("Failed to locate Schema '" + url + "' - Error message:", e.getMessage());
            }
		}
	    
		if (foundSchema != null) {
			log.debug("Caching schema", url);

			JedisManager.hset(cacheHashKey, url, XMLHelper.XSDSchemaToString(foundSchema));
			cache.put(url, foundSchema);
		}

		return foundSchema;
	}


}