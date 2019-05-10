package fi.nls.oskari.control.statistics.plugins.unsd.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegionMapper {

    private static String[] ALPHA_2_REGION_CODES = new String[]{
            "CA",
            "NO",
            "US",
            "GL",
            "DK",
            "SE",
            "IS",
            "FI"
    };
    private Map<String, String> regionsToUnsd = new HashMap<>();
    private Map<String, String> unsdToRegions = new HashMap<>();

    public RegionMapper() {
        init();
    }

    public void init() {
        Arrays.stream(ALPHA_2_REGION_CODES).forEach(a2Code -> {
            Locale locale = new Locale("", a2Code);
            regionsToUnsd.put(a2Code, locale.getISO3Country());
            unsdToRegions.put(locale.getISO3Country(), a2Code);
        });
    }

    public String getRegionCode(String unsdAreaCode) {
        return unsdToRegions.get(unsdAreaCode);
    }
    public String getUNSDAreaCode(String regionCode) {
        return regionsToUnsd.get(regionCode);
    }

}