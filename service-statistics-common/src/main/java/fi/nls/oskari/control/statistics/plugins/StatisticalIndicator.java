package fi.nls.oskari.control.statistics.plugins;

import java.util.List;
import java.util.Map;

/**
 * Each indicator has:
 * - Plugin id and id (id is unambiguous only within one plugin)
 * - A localized description shown to user.
 * - An ordered set of different granularity layers such as "Kunta", or "Maakunta".
 * - A set of selectors with a localized name and type and a list of allowed values, and their localizations.
 *   These could be for example: "Gender": "Male", "Female", "Other", "All", or "Year": "2010", "2011", ....
 */
public interface StatisticalIndicator {
    String getPluginId();
    String getId();
    /**
     * User created indicators can be private so that they are only shown to the user who created them.
     */
    Boolean isPublic();
    List<StatisticalIndicatorLayer> getLayers();
    StatisticalIndicatorLayer getLayer(long id);
    StatisticalIndicatorSelectors getSelectors();
    /*
     * Please note that while it would be convenient to just pass untyped JSON here,
     * it would make developing future plugins more error prone.
     */
    Map<String, String> getLocalizedName();
    Map<String, String> getLocalizedSource();
    Map<String, String> getLocalizedDescription();
}
