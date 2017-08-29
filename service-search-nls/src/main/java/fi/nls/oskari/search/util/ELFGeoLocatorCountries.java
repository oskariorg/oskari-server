package fi.nls.oskari.search.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.channel.ELFGeoLocatorSearchChannel;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Parses countries map for
 */
public class ELFGeoLocatorCountries {
    private Logger log = LogFactory.getLogger(this.getClass());

    public static final String DEFAULT_GETCOUNTRIES_TEMPLATE = "?SERVICE=WFS&VERSION=2.0.0&REQUEST=CountryFilter";
    // 30 minutes
    public long REFRESH_INTERVAL = 30 * 60 * 1000;

    private long lastSync;
    private String serviceURL;
    private ELFGeoLocatorSearchChannel channel;
    private Map<String, String> countries = new HashMap<>();
    private static Map<String, String> countryMap = null;

    public ELFGeoLocatorCountries() {
        serviceURL = PropertyUtil.getOptional(ELFGeoLocatorSearchChannel.PROPERTY_SERVICE_URL);
        if (serviceURL == null) {
            log.warn("ServiceURL not configured. Add property with key", ELFGeoLocatorSearchChannel.PROPERTY_SERVICE_URL);
        }
        loadCountryMap();
    }

    public void loadCountryMap () {
        try {
            countryMap = getCountryMap();
            if(countryMap.isEmpty()) {
                log.debug("Could not get countries");
            }
        }
        catch (Exception e) {
            log.debug(e, "Could not get countries");
        }
    }

    private Map<String, String> getCountryMap() throws IOException {
        // not properly configured
        if (serviceURL == null) {
            return countries;
        }
        // synced recently
        if(System.currentTimeMillis() < lastSync + REFRESH_INTERVAL) {
            return countries;
        }

        // Start from scratch
        String countriesUrl = serviceURL + DEFAULT_GETCOUNTRIES_TEMPLATE;
        StringBuffer buf = new StringBuffer(countriesUrl);
        log.debug("Server request: " + buf.toString());
        HttpURLConnection conn = IOHelper.getConnection(buf.toString());
        String response = IOHelper.readString(conn);
        log.debug("Server response: " + response);

        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(response.getBytes("UTF-8")));

            NodeList nList = doc.getElementsByTagName("Country");
            countries.clear();
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element eElement = (Element) nNode;
                NodeList elements = eElement.getElementsByTagName("administrator");
                for(int i=1; i<elements.getLength(); i++) {
                    countries.put(eElement.getElementsByTagName("administrator").item(i).getTextContent(),
                            eElement.getElementsByTagName("code").item(0).getTextContent());
                }
            }
        } catch (Exception e) {
            log.error(e, "Error parsing countries with ELFGeolocator, got exception");
        }
        lastSync = System.currentTimeMillis();
        return countries;
    }

    public String getAdminCountry(Locale locale, String admin_name, boolean reloadIfNotFound) {
        if (countryMap.containsKey(admin_name)) {
            return countryMap.get(admin_name);
        }
        if(!reloadIfNotFound) {
            // loading didn't get us the requested country so just return empty string
            return "";
        }
        // reload countries map contents and try again
        loadCountryMap();
        return getAdminName(admin_name, false);
    }

    public String getAdminName(String country_code, boolean reloadIfNotFound) {
        for (Map.Entry<String, String> entry : countryMap.entrySet()) {
            String country = entry.getValue();
            if (country.equals(country_code)) {
                return entry.getKey();
            }
        }
        if(!reloadIfNotFound) {
            // loading didn't get us the requested country so just return empty string
            return "";
        }
        // reload countries map contents and try again
        loadCountryMap();
        return getAdminName(country_code, false);
    }
}