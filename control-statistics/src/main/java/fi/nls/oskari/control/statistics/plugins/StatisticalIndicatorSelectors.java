package fi.nls.oskari.control.statistics.plugins;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for describing filters applied to a layer indicator data.
 * For example: "Gender": "male", "Year": 2008.
 * The selector values are null if not defined yet.
 */
public class StatisticalIndicatorSelectors {
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
}
