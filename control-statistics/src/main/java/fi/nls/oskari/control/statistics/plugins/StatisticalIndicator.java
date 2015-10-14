package fi.nls.oskari.control.statistics.plugins;

import java.util.List;

/**
 * Each indicator has:
 * - A localized description shown to user.
 * - An ordered set of different granularity layers such as "Kunta", or "Maakunta".
 * - A set of selectors with a localized name and type and a list of allowed values, and their localizations.
 *   These could be for example: "Gender": "Male", "Female", "Other", "All", or "Year": "2010", "2011", ....
 */
public interface StatisticalIndicator {
    public List<StatisticalIndicatorLayer> getLayers();
}
