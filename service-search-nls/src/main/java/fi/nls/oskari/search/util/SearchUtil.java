package fi.nls.oskari.search.util;

import fi.mml.nameregister.FeatureCollectionDocument;
import fi.mml.nameregister.FeaturePropertyType;
import fi.mml.portti.service.search.ChannelSearchResult;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.channel.RegisterOfNomenclatureChannelSearchService;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.XmlHelper;
import org.apache.xmlbeans.XmlObject;
import org.json.XMLTokener;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class SearchUtil {
	
	private static final Logger log = LogFactory.getLogger(SearchUtil.class);
	
	private static final int RANK_540 = 10;
	private static final int RANK_550 = 20;
	private static final int RANK_560 = 30;
	private static final int RANK_120 = 40;
	public static final int RANK_OTHER = 50;
	
	public static final String NAME_REGISTER_URL_PROPERTY = "search.nameregister.url";
	public static final String NAME_REGISTER_USER_PROPERTY = "search.nameregister.user";
	public static final String NAME_REGISTER_PASSWORD_PROPERTY = "search.nameregister.password";

	public static final String LOCATION_TYPE_URL_PROPERTY = "search.locationtype.url";

	private static final Map<String, String> localeMap = new HashMap<String, String>();
	
	// KEY: locationTypeCode, VALUE: ranking
	private static final Map<String, Integer> rankMap = new HashMap<String, Integer>();

	private static Map<String,String> locationTypeCache = new HashMap<String, String>();
	private static final Map<String, String> nameSpaceUri = new HashMap<String, String>();
	private static long locationTypeLastUpdate = 0;
	
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
	
	
	/**
	 * Returns the location code by locale. 
	 * @param locale Locale
	 * @return Location code
	 */
	
	public static String getLocaleCode(String locale) {
		return localeMap.get(locale);
	}


	@Deprecated
	public static String getNameRegisterUrl() throws Exception {
		return PropertyUtil.get(NAME_REGISTER_URL_PROPERTY);
	}


	public static URL getLocationTypeUrl() throws Exception {
        if(PropertyUtil.getOptional(LOCATION_TYPE_URL_PROPERTY) == null) return null;
		final URL url = new URL(PropertyUtil.get(LOCATION_TYPE_URL_PROPERTY));
		return url;
	}

	
	
	private static String getData(String villageName) throws Exception {
		
		final String login = PropertyUtil.getOptional(NAME_REGISTER_USER_PROPERTY);
        final String password = PropertyUtil.getOptional(NAME_REGISTER_PASSWORD_PROPERTY);
        final String url = getWFSUrl(villageName);
		return IOHelper.getURL(url, login, password);
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
	

    private static Document getDocument(InputStream inputStream)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = XmlHelper.newDocumentBuilderFactory();
		factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(inputStream));
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
            if (locationTypeUrl != null) {
                log.debug("locationtypeURL: ", locationTypeUrl);
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

                        String[] code = nextContent.split("\"");

                        while (xmlTokener.more()) {

                            String content = xmlTokener.nextContent().toString();

                            if (!"<".equals(content) && content.startsWith("xsd_documentation xml_lang")) {
                                String[] languageAndName = content.split("\"");

                                locationTypeCache.put(code[1] + "_" + languageAndName[1], languageAndName[2].substring(1));

                            } else if ("/xsd_annotation>".equals(content)) {
                                break;
                            }
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
