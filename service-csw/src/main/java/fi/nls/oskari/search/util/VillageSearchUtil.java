package fi.nls.oskari.search.util;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.json.XMLTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VillageSearchUtil {

    public static final String VILLAGES_URL_PROPERTY = "search.villages.url";
    private static final Logger log = LogFactory.getLogger(VillageSearchUtil.class);
    private final static long reloadInterval = 1000 * 60 * 60 * 24;
    static long villageLastUpdate = 0;
    private static Map<String, String> villageCache = new HashMap<>();

    public static URL getVillagesUrl()
            throws MalformedURLException {
        return new URL(PropertyUtil.get(VILLAGES_URL_PROPERTY));
    }

    public static boolean isVillage(String village) {
        final long currentTime = System.currentTimeMillis();
        if (villageLastUpdate == 0 || currentTime < villageLastUpdate + reloadInterval) {
            updateVillageCache();
            villageLastUpdate = currentTime;
        }
        return villageCache.containsValue(village);
    }

    /**
     * Returns the village name  by village code.
     *
     * @param villageCode Village code
     * @return Village name
     */

    public static String getVillageName(String villageCode) {
        final long currentTime = System.currentTimeMillis();

        if (villageLastUpdate == 0 || currentTime > villageLastUpdate + reloadInterval) {
            updateVillageCache();
            villageLastUpdate = currentTime;
        }
        if (!villageCache.containsKey(villageCode)) {
            return villageCode;
        }
        return villageCache.get(villageCode);
    }

    /**
     * Updating village cache
     */

    private static void updateVillageCache() {

        try (InputStreamReader isr = new InputStreamReader(getVillagesUrl().openStream(), "UTF-8");
             BufferedReader reader = new BufferedReader(isr)) {

            StringBuilder readXML = new StringBuilder();

            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                readXML.append(inputLine);
            }

            XMLTokener xmlTokener = new XMLTokener(readXML.toString().replace(':', '_'));
            while (xmlTokener.more()) {

                String nextContent = xmlTokener.nextContent().toString();

                if (!"<".equals(nextContent) && nextContent.startsWith("xsd_enumeration value")) {

                    String[] code = nextContent.split("\"");

                    while (xmlTokener.more()) {

                        String content = xmlTokener.nextContent().toString();

                        if (!"<".equals(content) && content.startsWith("xsd_documentation xml_lang")) {
                            String[] languageAndName = content.split("\"");

                            villageCache.put(code[1] + "_" + languageAndName[1], languageAndName[2].substring(1));
                            villageCache.put(languageAndName[2].substring(1), code[1]);

                        } else if ("/xsd_annotation>".equals(content)) {
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to update village cache", e);
        }
    }

    public static Map<String, String> getVillages() {
        final long currentTime = System.currentTimeMillis();
        if (villageLastUpdate == 0 || currentTime < villageLastUpdate + reloadInterval) {
            updateVillageCache();
            villageLastUpdate = currentTime;
        }
        return villageCache;
    }
}
