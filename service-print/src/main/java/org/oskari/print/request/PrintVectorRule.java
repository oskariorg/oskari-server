package org.oskari.print.request;

import org.opengis.filter.Filter;

public class PrintVectorRule {
    public enum RuleType  {
        POINT,
        LINE,
        POLYGON
    }

    private RuleType type;
    private Filter filter;
    private PDPrintStyle style;

    public PrintVectorRule (RuleType type, Filter filter, PDPrintStyle style) {
        this.type = type;
        this.filter = filter;
        this.style = style;
    }

    public RuleType getType() {
        return type;
    }

    public void setType(RuleType type) {
        this.type = type;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public PDPrintStyle getStyle() {
        return style;
    }

    public void setStyle(PDPrintStyle style) {
        this.style = style;
    }
}
