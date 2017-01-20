package fi.nls.oskari.control.statistics.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.nls.oskari.control.statistics.plugins.APIException;

import java.util.*;

/**
 * This class describes the potential options the user has to limit the indicator query.
 * For now, this is always a set of allowed values.
 * Complete hierarchies are implemented by using several StatisticalIndicatorSelectors, allowing the user
 * to select for example the gender and the year.
 * So far unimplemented partial hierarchies should be serialized as a tree.
 * The selector value is null until selected by the user.
 */
public class StatisticalIndicatorDataDimension {
    private String id;
    private String value = null;
    private List<IdNamePair> allowedValues = new ArrayList<>();
    private String name = null;

    public StatisticalIndicatorDataDimension(String id) {
        this.id = id;
    }
    /**
     * Use this in selections to be sent to the frontend.
     *
     * @param id
     * @param allowedValues
     */
    public StatisticalIndicatorDataDimension(String id, Collection<String> allowedValues) {
        this(id);
        for(String key : allowedValues) {
            addAllowedValue(key, null);
        }
    }
    /**
     * Use this in selections received from the frontend.
     * @param id
     * @param value
     */
    public StatisticalIndicatorDataDimension(@JsonProperty("id") String id, @JsonProperty("value") String value) {
        this(id);
        this.value = value;
    }

    public void addAllowedValue(String value) {
        addAllowedValue(value, null);
    }
    public void addAllowedValue(String value, String label) {
        allowedValues.add(new IdNamePair(value, label));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }
    public void setValue(String value) {

        if (allowedValues.contains(new IdNamePair(value))) {
            this.value = value;
        } else {
            throw new APIException("Statistical indicator selector value: " + value
                    + " is not in allowed values: " + allowedValues);
        }
    }
    public String getValue() {
        return value;
    }
    public List<IdNamePair> getAllowedValues() {
        return allowedValues;
    }
    @Override
    public String toString() {
        return "{ id: " + id + ", value: " + value + ", allowedValues: " + String.valueOf(allowedValues) + "}";
    }

    public void useDefaultValue(String value) {
        // sort allowed values to have default as first
        IdNamePair found = null;
        for(IdNamePair pair : allowedValues) {
            if(pair.getKey().equalsIgnoreCase(value)) {
                found = pair;
                break;
            }
        }
        if(found != null) {
            allowedValues.remove(found);
            allowedValues.add(0, found);
        }
    }

    public void sort(boolean descending) {
        // sort allowed values
        Comparator<IdNamePair> comparator = null;
        if(descending) {
            comparator = Collections.reverseOrder();
        }
        Collections.sort(allowedValues, comparator);
    }
}
