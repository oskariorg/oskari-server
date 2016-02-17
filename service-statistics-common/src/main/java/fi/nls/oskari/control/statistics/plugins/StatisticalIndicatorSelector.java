package fi.nls.oskari.control.statistics.plugins;

import java.util.Collection;

/**
 * This class describes the potential options the user has to limit the indicator query.
 * For now, this is always a set of allowed values.
 * Complete hierarchies are implemented by using several StatisticalIndicatorSelectors, allowing the user
 * to select for example the gender and the year.
 * So far unimplemented partial hierarchies should be serialized as a tree.
 * The selector value is null until selected by the user.
 */
public class StatisticalIndicatorSelector {
    private String id;
    private String value = null;
    private Collection<String> allowedValues = null;
    /**
     * Use this in selections to be sent to the frontend.
     * @param id
     * @param value
     */
    public StatisticalIndicatorSelector(String id, Collection<String> allowedValues) {
        this.id = id;
        this.allowedValues = allowedValues;
    }
    /**
     * Use this in selections received from the frontend.
     * @param id
     * @param value
     */
    public StatisticalIndicatorSelector(String id, String value) {
        this.id = id;
        this.value = value;
    }
    public String getId() {
        return id;
    }
    public void setValue(String value) {
        if (allowedValues.contains(value)) {
            this.value = value;
        } else {
            throw new APIException("Statistical indicator selector value: " + value
                    + " is not in allowed values: " + allowedValues);
        }
    }
    public String getValue() {
        return value;
    }
    public Collection<String> getAllowedValues() {
        return allowedValues;
    }
    @Override
    public String toString() {
        return "{ id: " + id + ", value: " + value + ", allowedValues: " + String.valueOf(allowedValues) + "}";
    }
}
