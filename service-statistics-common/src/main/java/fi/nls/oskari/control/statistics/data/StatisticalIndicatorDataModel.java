package fi.nls.oskari.control.statistics.data;

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
    private List<StatisticalIndicatorDataDimension> dimensions =
            new ArrayList<StatisticalIndicatorDataDimension>();
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
    public void merge(StatisticalIndicatorDataModel model) {
        // A naive array lookup is fastest for small arrays.
        for (StatisticalIndicatorDataDimension selector : model.getDimensions()) {
            StatisticalIndicatorDataDimension foundSelector = null;
            for (StatisticalIndicatorDataDimension originalSelector : this.dimensions) {
                if (originalSelector.getId().equals(selector.getId())) {
                    // Found match. We can assume these are identical here.
                    foundSelector = originalSelector;
                }
            }
            if (foundSelector == null) {
                // The selector is a new one which does not exist in dimensions. Adding.
                this.dimensions.add(selector);
            }
        }
    }
}
