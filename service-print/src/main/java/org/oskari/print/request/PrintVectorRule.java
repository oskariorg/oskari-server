package org.oskari.print.request;

import org.geotools.api.filter.Filter;

public class PrintVectorRule {

    private final Filter filter;
    private final PDPrintStyle style;

    public PrintVectorRule(Filter filter, PDPrintStyle style) {
        this.filter = filter;
        this.style = style;
    }

    public Filter getFilter() {
        return filter;
    }

    public PDPrintStyle getStyle() {
        return style;
    }

}
