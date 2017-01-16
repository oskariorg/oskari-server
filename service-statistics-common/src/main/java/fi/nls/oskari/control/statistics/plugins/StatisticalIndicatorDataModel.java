package fi.nls.oskari.control.statistics.plugins;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for describing filters applied to a layer indicator data.
 * For example: "Gender": "male", "Year": 2008.
 * The selector values are null if not defined yet.
 */
public class StatisticalIndicatorDataModel {
    /**
     * The selectors have a defined order.
     */
    private List<StatisticalIndicatorSelector> selectors =
            new ArrayList<StatisticalIndicatorSelector>();
    /**
     * @return A mutable list of selectors.
     */
    public List<StatisticalIndicatorSelector> getSelectors() {
        return this.selectors;
    }
    public void addSelector(StatisticalIndicatorSelector selector) {
        this.selectors.add(selector);
    }
    @Override
    public String toString() {
        return "{" + String.valueOf(selectors) + "}";
    }
    public void merge(StatisticalIndicatorDataModel selectors2) {
        // A naive array lookup is fastest for small arrays.
        for (StatisticalIndicatorSelector selector : selectors2.getSelectors()) {
            StatisticalIndicatorSelector foundSelector = null;
            for (StatisticalIndicatorSelector originalSelector : this.selectors) {
                if (originalSelector.getId().equals(selector.getId())) {
                    // Found match. We can assume these are identical here.
                    foundSelector = originalSelector;
                }
            }
            if (foundSelector == null) {
                // The selector is a new one which does not exist in selectors. Adding.
                this.selectors.add(selector);
            }
        }
    }
}
