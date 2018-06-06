package fi.nls.oskari.control.statistics.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for describing filters applied to a layer indicator data.
 * For example: "Gender": "male", "Year": 2008.
 * The selector values are null if not defined yet.
 */
public class StatisticalIndicatorDataModel {
    /**
     * The dimensions have a defined order.
     */
    private List<StatisticalIndicatorDataDimension> dimensions = new ArrayList<>();

    // this is the id of the dimension that re-presents time -> enables time-series analyzes
    private String timeVariable;
    public String getTimeVariable() {
        return timeVariable;
    }

    public boolean isTimeVariable(StatisticalIndicatorDataDimension selector) {
        if (selector == null || getTimeVariable() == null) {
            return false;
        }
        String id = selector.getId();
        if (id == null) {
            return false;
        }
        return id.equalsIgnoreCase(getTimeVariable());
    }

    public void setTimeVariable(String timeVariable) {
        this.timeVariable = timeVariable;
    }
    /**
     * @return A mutable list of dimensions.
     */
    public List<StatisticalIndicatorDataDimension> getDimensions() {
        return this.dimensions;
    }
    public void addDimension(StatisticalIndicatorDataDimension selector) {
        this.dimensions.add(selector);
    }
    @Override
    public String toString() {
        return "{" + String.valueOf(dimensions) + "}";
    }

    @JsonIgnore
    public StatisticalIndicatorDataDimension getDimension(String id) {
        for(StatisticalIndicatorDataDimension dim : getDimensions()) {
            if(dim.getId().equalsIgnoreCase(id)) {
                return dim;
            }
        }
        return null;
    }
}
