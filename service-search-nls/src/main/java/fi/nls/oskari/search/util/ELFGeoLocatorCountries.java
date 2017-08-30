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
import java.util.*;

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
    private static Map<String, String> countryMap = new HashMap<>();

    public ELFGeoLocatorCountries() {
        serviceURL = PropertyUtil.getOptional(ELFGeoLocatorSearchChannel.PROPERTY_SERVICE_URL);
        if (serviceURL == null) {
            log.warn("ServiceURL not configured. Add property with key", ELFGeoLocatorSearchChannel.PROPERTY_SERVICE_URL);
        }
        loadCountryMap();
    }

    public void loadCountryMap () {
        try {
            // not properly configured
            if (serviceURL == null) {
                return ;
            }

            // synced recently
            if(System.currentTimeMillis() < lastSync + REFRESH_INTERVAL) {
                return ;
            }
            parseCountryMap(getCountryData());
            if(countryMap.isEmpty()) {
                log.debug("Could not get countries");
            }
        }
        catch (Exception e) {
            log.debug(e, "Could not get countries");
        }
        lastSync = System.currentTimeMillis();
    }

    public String getCountryData() throws IOException {
        String countriesUrl = serviceURL + DEFAULT_GETCOUNTRIES_TEMPLATE;
        log.debug("Server request: " + countriesUrl);
        HttpURLConnection conn = IOHelper.getConnection(countriesUrl);
        String response = IOHelper.readString(conn);
        return response;
    }

    public void parseCountryMap(String response) {
        if (response == null || response.isEmpty()) {
            return ;
        }

        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(response.getBytes("UTF-8")));

            NodeList nList = doc.getElementsByTagName("Country");
            countryMap.clear();
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element eElement = (Element) nNode;
                NodeList elements = eElement.getElementsByTagName("administrator");
                for(int i=0; i<elements.getLength(); i++) {
                    countryMap.put(eElement.getElementsByTagName("administrator").item(i).getTextContent(),
                            eElement.getElementsByTagName("code").item(0).getTextContent());
                }
            }
        } catch (Exception e) {
            log.error(e, "Error parsing countries with ELFGeolocator, got exception");
        }
    }

    public String getAdminCountry(Locale locale, String admin_name, boolean reloadIfNotFound) {
        if (countryMap.containsKey(admin_name)) {
            String countryCode = countryMap.get(admin_name);
            Locale obj = new Locale("", countryCode);
            return obj.getDisplayCountry(locale);
        }
        if(!reloadIfNotFound) {
            // loading didn't get us the requested country so just return empty string
            return "";
        }
        // reload countries map contents and try again
        loadCountryMap();
        return getAdminCountry(locale, admin_name, false);
    }

    public List<String> getAdminName(String country_code, boolean reloadIfNotFound) {
        List<String> adminNameList = new ArrayList<String>();
        for (Map.Entry<String, String> entry : countryMap.entrySet()) {
            String country = entry.getValue();
            if (country.equals(country_code)) {
                adminNameList.add(entry.getKey());
                reloadIfNotFound = false;
            }
        }
        if(!reloadIfNotFound) {
            // loading didn't get us the requested country so just return empty string
            return adminNameList;
        }
        // reload countries map contents and try again
        loadCountryMap();
        return getAdminName(country_code, false);
    }

    public String getAdminNamesForFilter(String country) {
        List<String> adminNameList = getAdminName(country, true);
        String adminNames = "";
        if (adminNameList.size() == 1) {
            adminNames = adminNameList.get(0);
        } else {
            for (int i = 0; i < adminNameList.size()-1; i++) {
                adminNames = adminNames + adminNameList.get(i) + "<OR>";
            }
            adminNames = adminNames + adminNameList.get(adminNameList.size() - 1);
        }
        return adminNames;
    }
}