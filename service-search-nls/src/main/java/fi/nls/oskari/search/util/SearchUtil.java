package fi.nls.oskari.search.util;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import fi.mml.nameregister.FeatureCollectionDocument;
import fi.mml.nameregister.FeaturePropertyType;
import fi.mml.portti.service.search.ChannelSearchResult;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.channel.RegisterOfNomenclatureChannelSearchService;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.xmlbeans.XmlObject;
import org.json.XMLTokener;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;
import java.util.*;

public class SearchUtil {
	
	private static final Logger log = LogFactory.getLogger(SearchUtil.class);
	public static final int maxCount = 100;
	
	private static final int RANK_540 = 10;
	private static final int RANK_550 = 20;
	private static final int RANK_560 = 30;
	private static final int RANK_120 = 40;
	public static final int RANK_OTHER = 50;
	
	public static final String NAME_REGISTER_URL_PROPERTY = "search.nameregister.url";
	public static final String NAME_REGISTER_USER_PROPERTY = "search.nameregister.user";
	public static final String NAME_REGISTER_PASSWORD_PROPERTY = "search.nameregister.password";
	
	public static final String VILLAGES_URL_PROPERTY = "search.villages.url";
	public static final String LOCATION_TYPE_URL_PROPERTY = "search.locationtype.url";

	private static final Map<String, String> localeMap = new HashMap<String, String>();
	private static final Map<String, String> zoomLevel = new HashMap<String, String>();
	
	// KEY: locationTypeCode, VALUE: ranking
	private static final Map<String, Integer> rankMap = new HashMap<String, Integer>();
	
	private static Map<String,String> villageCache = new HashMap<String, String>();
	private static Map<String,String> locationTypeCache = new HashMap<String, String>();
	private static final Map<String, String> nameSpaceUri = new HashMap<String, String>();
	
	static {
		
		nameSpaceUri.put("pnr", "http://xml.nls.fi/Nimisto/Nimistorekisteri/2009/02");
		nameSpaceUri.put("gml", "http://www.opengis.net/gml");
		
		rankMap.put("540", RANK_540);
		rankMap.put("550", RANK_550);
		rankMap.put("560", RANK_560);
		rankMap.put("120", RANK_120);

        for (String locale : PropertyUtil.getSupportedLocales()) {
            String lang = locale.split("_")[0];
            Locale loc = new Locale(lang);
            localeMap.put(lang, loc.getISO3Language());
        }
		//Luontonimet, maasto	
		zoomLevel.put("300","10");
		zoomLevel.put("305","10");
		zoomLevel.put("310","10");
		zoomLevel.put("315","9");
		zoomLevel.put("325","8");
		zoomLevel.put("330","8");
		zoomLevel.put("335","9");
		zoomLevel.put("340","9");
		zoomLevel.put("345","9");
		zoomLevel.put("350","9");
		zoomLevel.put("430","9");
		zoomLevel.put("390","8");
		//Luontonimet, vesistö	
		zoomLevel.put("400","10");
		zoomLevel.put("410","6");
		zoomLevel.put("415","8");
		zoomLevel.put("420","9");
		zoomLevel.put("425","8");
		zoomLevel.put("435","10");
		zoomLevel.put("490","9");
		//Kulttuurinimet, asutus
		zoomLevel.put("540","6");
		zoomLevel.put("550","6");
		zoomLevel.put("560","8");
		zoomLevel.put("570","10");
		zoomLevel.put("590","10");
		//Kulttuurinimet, muut	
		zoomLevel.put("110","8");
		zoomLevel.put("120","9");
		zoomLevel.put("130","10");
		zoomLevel.put("200","9");
		zoomLevel.put("205","9");
		zoomLevel.put("210","8");
		zoomLevel.put("215","9");
		zoomLevel.put("225","8");
		zoomLevel.put("230","9");
		zoomLevel.put("235","9");
		zoomLevel.put("240","9");
		zoomLevel.put("245","9");
		zoomLevel.put("320","9");
		zoomLevel.put("500","10");
		zoomLevel.put("510","10");
		zoomLevel.put("520","9");
		zoomLevel.put("530","10");
		zoomLevel.put("600","7");
		zoomLevel.put("602","6");
		zoomLevel.put("604","6");
		zoomLevel.put("610","10");
		zoomLevel.put("612","10");
		zoomLevel.put("614","10");
		zoomLevel.put("620","7");
		zoomLevel.put("630","7");
		zoomLevel.put("640","7");
		zoomLevel.put("700","10");
		zoomLevel.put("710","10");
		zoomLevel.put("Tie","10");
		zoomLevel.put("Rakennus","10");
		zoomLevel.put("Kiinteistötunnus","10");
	}
	
	public static final QName pnrPaikka = getQName("Paikka", "pnr");
	public static final QName pnrPaikkatyyppiKoodi = getQName("paikkatyyppiKoodi", "pnr");
	/*public static final QName pnrseutukuntaKoodi = getQName("seutukuntaKoodi", "pnr");*/
	public static final QName pnrkuntaKoodi = getQName("kuntaKoodi", "pnr");
	
	public static final QName pnrPaikkaSijaintiName = getQName("paikkaSijainti", "pnr");
	
	public static final QName gmlPoint = getQName("Point", "gml");
	public static final QName gmlPos = getQName("pos", "gml");
	
	public static final QName pnrNimi = getQName("nimi","pnr");
	public static final QName pnrPaikanNimi = getQName("PaikanNimi","pnr");
	
	public static final QName pnrKirjoitusAsu = getQName("kirjoitusasu","pnr");
	public static final QName pnrkieliKoodi = getQName("kieliKoodi","pnr");

    /**
     * Returns map URL for given locale
     * @param locale Locale
     * @return Map URL
     */
    public static String getMapURL(String locale) {
        return PropertyUtil.get("map.url." + locale);
    }
	
	
	/**
	 * Returns the ZoomLevel by location type. 
	 * @param type Location type
	 * @return Zoom level
	 */
	
	public static String getZoomLevel(String type) {
		if (zoomLevel.containsKey(type)) {
			return (zoomLevel.get(type));
		} else {
			return "5";
		}
	}
	
	/**
	 * Returns the list rank by location type. 
	 * @param locationTypeCode Location type code
	 * @return List rank
	 */
	public static int getRank(String locationTypeCode) {
		if ((locationTypeCode != null) && rankMap.containsKey(locationTypeCode)) {
			return rankMap.get(locationTypeCode);
		}		
		return RANK_OTHER;
	}
	
	private final static long reloadInterval = 1000*60*60*24;
	static long villageLastUpdate = 0;
	private static long locationTypeLastUpdate = 0;
	
	
	/**
	 * Returns the location code by locale. 
	 * @param locale Locale
	 * @return Location code
	 */
	
	public static String getLocaleCode(String locale) {
		return localeMap.get(locale);
	}
	
	public static boolean isVillage(String village) {
		final long currentTime = System.currentTimeMillis(); 
		if (villageLastUpdate == 0 || currentTime <  villageLastUpdate + reloadInterval) {
			updateVillageCache();
			villageLastUpdate = currentTime;
		}
		return villageCache.containsValue(village);
	}
	
	public static String getNameRegisterUrl() throws Exception {
		return PropertyUtil.get(NAME_REGISTER_URL_PROPERTY);
	}
	
	public static URL getVillagesUrl() throws Exception {
		final URL villagesUrl = new URL(PropertyUtil.get(VILLAGES_URL_PROPERTY));
		return villagesUrl;
	}
	
	public static URL getLocationTypeUrl() throws Exception {
		final URL villagesUrl = new URL(PropertyUtil.get(LOCATION_TYPE_URL_PROPERTY));
		return villagesUrl;
	}
	
	/**
	 * Returns the village name  by village code. 
	 * @param villageCode Village code
	 * @return Village name
	 */
	
	public static String getVillageName(String villageCode) {
		final long currentTime = System.currentTimeMillis(); 
		
		if (villageLastUpdate == 0 || currentTime >  villageLastUpdate + reloadInterval) {
			updateVillageCache();
			villageLastUpdate = currentTime;
		}
		if (!villageCache.containsKey(villageCode)) {
			return villageCode;
		}
		return villageCache.get(villageCode);
	}
	
	
	private static String getData(String villageName) throws Exception {
		
		final String login = PropertyUtil.get(SearchUtil.NAME_REGISTER_USER_PROPERTY);
        final String password = PropertyUtil.get(SearchUtil.NAME_REGISTER_PASSWORD_PROPERTY);
        
        if (login != null && !"".equals(login) && password != null && !"".equals(password)) {
	        Authenticator.setDefault(new Authenticator() {
	            protected PasswordAuthentication getPasswordAuthentication() {
	                return new PasswordAuthentication (login, password.toCharArray());
	            }
	        }); 
        }
		final URL url = new URL(getWFSUrl(villageName));
		URLConnection conn = url.openConnection();
		InputStream ins;
		
		StringBuilder data = new StringBuilder();
		if (conn instanceof HttpsURLConnection) {
			HttpsURLConnection https_conn = (HttpsURLConnection) conn;
		    ins = https_conn.getInputStream();
		} else {
			ins = conn.getInputStream();
		}
		InputStreamReader isr = new InputStreamReader(ins);
	    BufferedReader in = new BufferedReader(isr);
	    String inputLine;
	    	
	    while ((inputLine = in.readLine()) != null) {
	        data.append(inputLine);
	    }
	    in.close();
		conn.connect();
			  
		return data.toString();
	}
	
	
	/**
	 * Returns the village code  by village name. 
	 * @param villageName Village name
	 * @return Village code
	 */
	
	public static String getVillageCode(String villageName) {
		
		ChannelSearchResult searchResultList = new ChannelSearchResult();
		searchResultList.setChannelId(RegisterOfNomenclatureChannelSearchService.ID);
		
		try {
			//final URL wfsUrl = new URL(getWFSUrl(villageName));
			
			String data = getData(villageName);
			final FeatureCollectionDocument fDoc =  FeatureCollectionDocument.Factory.parse(data);
			
			FeaturePropertyType[] fMembersArray = fDoc.getFeatureCollection().getFeatureMemberArray();
			
			if ((fMembersArray == null) || (fMembersArray.length == 0)) {
				return "";
			}
			
			XmlObject[] paikka = fMembersArray[0].selectChildren(SearchUtil.pnrPaikka);
											
			String kuntaKoodi = paikka[0].selectChildren(SearchUtil.pnrkuntaKoodi)[0].newCursor().getTextValue();
			return kuntaKoodi;				
			
						
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to get village code", e);
		}
		
		//return "";
	}
	
	private static String getWFSUrl(String filter) throws Exception {
		
		String filterXml = 
			"<Filter>" +
			"<And>" +
			"	<PropertyIsEqualTo matchCase='false'>" +
			"       <PropertyName>pnr:nimi/pnr:PaikanNimi/pnr:kirjoitusasu</PropertyName>" +
			"       <Literal>" + filter + "</Literal>" +
			"   </PropertyIsEqualTo>" +
			"<Or>"+
			"	<PropertyIsEqualTo>" +
			"       <PropertyName>pnr:paikkatyyppiKoodi</PropertyName>" +
			"       <Literal>540</Literal>" +
			"   </PropertyIsEqualTo>" +
			"	<PropertyIsEqualTo>" +
			"       <PropertyName>pnr:paikkatyyppiKoodi</PropertyName>" +
			"       <Literal>550</Literal>" +
			"   </PropertyIsEqualTo>" +
			"</Or>"+
			"</And>" +
			"</Filter>";
		
		filterXml = URLEncoder.encode(filterXml, "UTF-8");
		
		String wfsUrl = SearchUtil.getNameRegisterUrl() +
			"?SERVICE=WFS&VERSION=1.1.0" +
			"&REQUEST=GetFeature&TYPENAME=pnr:Paikka" +
			"&NAMESPACE=xmlns%28pnr=http://xml.nls.fi/Nimisto/Nimistorekisteri/2009/02%29" +
			"&filter=" +filterXml;
		
		return wfsUrl;
	}
	

	/**
	 * Updating village cache 
	 */
	
	private static void updateVillageCache() {
		
		try {
			final URL villagesUrl = getVillagesUrl();
            InputStreamReader isr = new InputStreamReader(villagesUrl.openStream(), "UTF-8");
            
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder readXML = new StringBuilder();
            
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
            	readXML.append(inputLine);
            }
            isr.close();
            
            XMLTokener xmlTokener = new XMLTokener(readXML.toString().replace(':', '_'));
            while (xmlTokener.more()) {
            	
            	String nextContent = xmlTokener.nextContent().toString();
            	
            	if (!"<".equals(nextContent) && nextContent.startsWith("xsd_enumeration value")) {
            		
            		String[] code =  nextContent.split("\"");
            		
            		 while (xmlTokener.more()) {
            			 
            			 String content = xmlTokener.nextContent().toString();
            			 
            			 if (!"<".equals(content) && content.startsWith("xsd_documentation xml_lang")) {
            				 String[] languageAndName = content.split("\"");
            				 
            				 villageCache.put(code[1]+"_"+languageAndName[1], languageAndName[2].substring(1));
            				 villageCache.put(languageAndName[2].substring(1),code[1]);
            				 
            			 }else if ("/xsd_annotation>".equals(content)) {
            				 break;
            			 }
            		 }
            	}
            }      
          
		} catch (Exception e) {
			throw new RuntimeException("Failed to update village cache", e);
		}
	}

    private static Document getDocument(InputStream inputStream)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(inputStream));
    }
	
	/**
	 * Updating village cache 
	 * * @param villages xml url
	 */
	
	static void updateVillageCache(String villages) {
        InputStream is = null;
        try {

	    	URL villagesUrl = new URL(villages);
	        URLConnection villagesCon = villagesUrl.openConnection();
	    	is = villagesCon.getInputStream();
			Document doc = getDocument(is);
			NodeList nl = doc.getElementsByTagName("xsd:enumeration");
			
			for (int i = 0; i < nl.getLength(); i++) {
				String code = nl.item(i).getAttributes().item(0).getNodeValue();
				
				
				NodeList languageVersions = nl.item(i).getChildNodes().item(1).getChildNodes();
				
				for (int j = 0; j < languageVersions.getLength(); j++) {
					
					
					if ("xsd:documentation".equals(languageVersions.item(j).getNodeName())) {
						String language = languageVersions.item(j).getAttributes().item(0).getNodeValue();
						String name = languageVersions.item(j).getTextContent();
						
						villageCache.put(code+"_"+language, name);
	   				 	villageCache.put(name,code);
					}
					
				}
				
			}
			villageLastUpdate = System.currentTimeMillis();
		} catch (Exception e) {
			throw new RuntimeException("Failed to update village cache", e);
		}
        finally {
            IOHelper.close(is);
        }
	}
	
	
	

	/**
	 * Returns the location type by location type code. 
	 * @param locationTypeCode Location type code
	 * @return location name 
	 */
	
	public static String getLocationType(String locationTypeCode) {
		final long currentTime = System.currentTimeMillis(); 
		
		if (locationTypeLastUpdate == 0 || currentTime <  locationTypeLastUpdate * reloadInterval) {
			updateLocationTypeCache();
			locationTypeLastUpdate = currentTime;
		}
        //log.debug(locationTypeCache);
		return locationTypeCache.get(locationTypeCode);
	}
    private static String getLocalization(Properties locales, Properties defaults, String key) {
        if(locales.contains(key)) {
            return (String)locales.get(key);
        }
        if(defaults.contains(key)) {
            return (String)defaults.get(key);
        }
        return "";
    }

	/**
	 * Updating location type cache
	 */
	private static void updateLocationTypeCache() {
		
		try {
            // populate defaults if we dont get results from services
            // resourcebundles are not used since they present a problem with liferay usage (need to be registered for portlet specifically).
            final Properties defaultProps = new Properties();
            defaultProps.load(SearchUtil.class.getResourceAsStream("/SearchLocalization.properties"));

            for (String localeString : PropertyUtil.getSupportedLocales()) {
                String lang = localeString.split("_")[0];
                Locale locale = new Locale(lang);
                String iso3Lang = locale.getISO3Language();

                final Properties localeProps = new Properties();
                localeProps.load(SearchUtil.class.getResourceAsStream("/SearchLocalization_" + lang + ".properties"));
// fi.nls.search.util.Language
                final String road = getLocalization(localeProps, defaultProps, "address");
                final String building = getLocalization(localeProps, defaultProps, "building");
                final String realEstateIdentifiers = getLocalization(localeProps, defaultProps, "realEstateIdentifiers");

                locationTypeCache.put("Tie_" + iso3Lang, road );
                locationTypeCache.put("Rakennus_" + iso3Lang, building );
                locationTypeCache.put("Kiinteistötunnus_" + iso3Lang, realEstateIdentifiers );
            }
        }
        catch(Exception ex) {
            log.error("Couldn't populate default/fallback values for location types");
        }
        try {

			final URL locationTypeUrl = getLocationTypeUrl();

			InputStreamReader isr = new InputStreamReader(locationTypeUrl.openStream(), "UTF-8");
            
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder readXML = new StringBuilder();
            
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
            	readXML.append(inputLine);
            }
            isr.close();
            
            XMLTokener xmlTokener = new XMLTokener(readXML.toString().replace(':', '_'));
            while (xmlTokener.more()) {
            	
            	String nextContent = xmlTokener.nextContent().toString();
            	
            	if (!"<".equals(nextContent) && nextContent.startsWith("xsd_enumeration value")) {
            		
            		String[] code =  nextContent.split("\"");
            		
            		 while (xmlTokener.more()) {
            			 
            			 String content = xmlTokener.nextContent().toString();
            			 
            			 if (!"<".equals(content) && content.startsWith("xsd_documentation xml_lang")) {
            				 String[] languageAndName = content.split("\"");
            				 
            				 locationTypeCache.put(code[1]+"_"+languageAndName[1], languageAndName[2].substring(1));
            				 
            			 }else if ("/xsd_annotation>".equals(content)) {
            				 break;
            			 }
            		 }
            	}
            }      
          
		} catch (Exception e) {
			throw new RuntimeException("Failed to update location type", e);
		}
	}
	
	private static QName getQName(final String attribute, final String prefix) {
		return new QName(nameSpaceUri.get(prefix), attribute, prefix);
	}
	
}
