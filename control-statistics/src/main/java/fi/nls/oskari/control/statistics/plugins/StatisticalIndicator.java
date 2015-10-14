package fi.nls.oskari.control.statistics.plugins;

import java.util.List;
import java.util.Map;

/**
 * Each indicator has:
 * - A localized description shown to user.
 * - An ordered set of different granularity layers such as "Kunta", or "Maakunta".
 * - A set of selectors with a localized name and type and a list of allowed values, and their localizations.
 *   These could be for example: "Gender": "Male", "Female", "Other", "All", or "Year": "2010", "2011", ....
 */
public interface StatisticalIndicator {
    public String getId();
    public Map<String, String> getLocalizedName();
    public List<StatisticalIndicatorLayer> getLayers();
    public StatisticalIndicatorSelectors getSelectors();
}
