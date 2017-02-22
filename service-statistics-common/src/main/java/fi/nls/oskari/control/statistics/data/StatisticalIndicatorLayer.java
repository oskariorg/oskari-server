package fi.nls.oskari.control.statistics.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Each different granularity layer has:
 * - A reference to a certain map layer in use in Oskari.
 *   This map layer version can be the current one in use, or a previous one. Old layers are preserved in Oskari to
 *   show indicators defined for some older sets of municipalities or so.
 * - A table of data indexed by selectors so that Oskari user can select values for selectors and Oskari can then show
 *   the data for a given granularity level.
 */
public class StatisticalIndicatorLayer {
    private long id;
    private String indicatorId;
    private HashMap<String, String> params = new HashMap<>();
    private IndicatorValueType indicatorValueType =  IndicatorValueType.FLOAT;

    @JsonCreator
    public StatisticalIndicatorLayer(@JsonProperty("oskariLayerId") long id,
                                     @JsonProperty("indicatorId") String indicatorId) {
        this.id = id;
        this.indicatorId = indicatorId;
    }

    public void addParam(String key, String value) {
        params.put(key, value);
    }

    public String getParam(String key) {
        return params.get(key);
    }

    /**
     * Note! Having the getter triggers proper json serialization
     * @return
     */
    public Map<String, String> getParams() {
        return params;
    }

    /**
     *  The layer id in Oskari, for example: 9. This maps to the name in the oskari_maplayers table.
     */
    public long getOskariLayerId() {
        return id;
    }

    public String getIndicatorId() {
        return indicatorId;
    }

    /**
     * Single layer contains indicators for one type only.
     * @return "BOOLEAN" for JSON/Java booleans, "INTEGER" for JSON integers/Java Longs, "FLOAT" for JSON numbers/Java Doubles.
     */
    public IndicatorValueType getIndicatorValueType() {
        return indicatorValueType;
    }
    public void setIndicatorValueType(IndicatorValueType type) {
        indicatorValueType = type;
    }

    public String toString() {
        return "{id: " + getOskariLayerId() + ", valueType: " + getIndicatorValueType() + "}";
    }
}