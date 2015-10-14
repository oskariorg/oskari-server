package fi.nls.oskari.control.statistics.plugins;

import java.util.Map;

/**
 * Each different granularity layer has:
 * - A reference to a certain map layer and version in use in Oskari.
 *   This map layer version can be the current one in use, or a previous one. Old layers are preserved in Oskari to
 *   show indicators defined for some older sets of municipalities or so.
 * - A table of data indexed by selectors so that Oskari user can select values for selectors and Oskari can then show
 *   the data for a given granularity level.
 */
public interface StatisticalIndicatorLayer {
    public String getOskariMapLayerId();
    public String getOskariMapLayerVersion();
    /**
     * Single layer contains indicators for one type only.
     * @return "BOOLEAN" for JSON/Java booleans, "INTEGER" for JSON integers/Java Longs, "FLOAT" for JSON numbers/Java Doubles.
     */
    public IndicatorValueType getIndicatorValueType();
    /**
     * @param selectors A set of selected values for selectors used to filter the results.
     * @return Filtered results so that each region id has one IndicatorValue.
     */
    public Map<String, IndicatorValue> getIndicatorValues(StatisticalIndicatorSelectors selectors);
}
