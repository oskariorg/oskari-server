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

    public ELFGeoLocatorCountries(ELFGeoLocatorSearchChannel channel) {
        this.channel = channel;
        serviceURL = PropertyUtil.getOptional(ELFGeoLocatorSearchChannel.PROPERTY_SERVICE_URL);
        if (serviceURL == null) {
            log.warn("ServiceURL not configured. Add property with key", ELFGeoLocatorSearchChannel.PROPERTY_SERVICE_URL);
        }
    }

    public Map<String, String> getCountryMap() throws IOException {
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
        HttpURLConnection conn = channel.getConnection(buf.toString());
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
                countries.put(eElement.getElementsByTagName("code").item(0).getTextContent(),
                        eElement.getElementsByTagName("administrator").item(0).getTextContent());
            }
        } catch (Exception e) {
            log.error(e, "Error parsing countries with ELFGeolocator, got exception");
        }
        lastSync = System.currentTimeMillis();
        return countries;
    }
}