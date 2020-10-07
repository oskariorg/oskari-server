package org.oskari.service.wfs.client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fi.nls.oskari.service.ServiceRuntimeException;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

public class WFSFilter {
    private String key;
    private String value; // equal
    private boolean caseSensitive;
    private Double greaterThan;
    private Double atLeast;
    private Double lessThan;
    private Double atMost;
    private List<String> like;
    private List<String> notLike;
    private List<String> in;
    private List<String> notIn;

    private WFSFilter property;
    private List<WFSFilter> and;
    private List<WFSFilter> or;

    private static final FilterFactory ff = CommonFactoryFinder.getFilterFactory();

    public String getValue() {

        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public double getGreaterThan() {
        return greaterThan;
    }

    public void setGreaterThan(double greaterThan) {
        this.greaterThan = greaterThan;
    }

    public double getAtLeast() {
        return atLeast;
    }

    public void setAtLeast(double atLeast) {
        this.atLeast = atLeast;
    }

    public double getLessThan() {
        return lessThan;
    }

    public void setLessThan(double lessThan) {
        this.lessThan = lessThan;
    }

    public double getAtMost() {
        return atMost;
    }

    public void setAtMost(double atMost) {
        this.atMost = atMost;
    }

    public List<String> getLike() {
        return like;
    }

    public void setLike(List<String> like) {
        this.like = like;
    }

    public List<String> getNotLike() {
        return notLike;
    }

    public void setNotLike(List<String> notLike) {
        this.notLike = notLike;
    }

    public List<String> getIn() {
        return in;
    }

    public void setIn(List<String> in) {
        this.in = in;
    }

    public List<String> getNotIn() {
        return notIn;
    }

    public void setNotIn(List<String> notIn) {
        this.notIn = notIn;
    }
    public WFSFilter getProperty() {
        return property;
    }

    public void setProperty(WFSFilter property) {
        this.property = property;
    }

    public List<WFSFilter> getAnd() {
        return and;
    }

    public void setAnd(List<WFSFilter> and) {
        this.and = and;
    }

    public List<WFSFilter> getOr() {
        return or;
    }

    public void setOr(List<WFSFilter> or) {
        this.or = or;
    }
    protected Filter getFilter() {
        if (key != null) {
            return getPropertyFilter();
        }
        if (property !=null) {
            return property.getPropertyFilter();
        }
        if (and != null && !and.isEmpty()) {
            List<Filter> andFilters = and.stream().map(f -> f.getFilter()).collect(Collectors.toList());
            if (!andFilters.isEmpty()) {
                return ff.and(andFilters);
            }
        }
        if (or != null) {
            List<Filter> orFilters = or.stream().map(f -> f.getFilter()).collect(Collectors.toList());
            if (!orFilters.isEmpty()) {
                return ff.or(orFilters);
            }
        }
        return null;
    }
    protected Filter getPropertyFilter() {
        Expression name = ff.property(key);
        if (value != null) {
            return ff.equal(name, ff.literal(value), isCaseSensitive());
        }
        List <Filter> filters = new ArrayList<>();
        if (greaterThan != null) {
            filters.add(ff.greater(name, ff.literal(greaterThan)));
        }
        if (lessThan != null) {
            filters.add(ff.less(name, ff.literal(lessThan)));
        }
        if (atMost != null) {
            filters.add(ff.lessOrEqual(name, ff.literal(atMost)));
        }
        if (atLeast != null) {
            filters.add(ff.greaterOrEqual(name, ff.literal(atLeast)));
        }
        if (filters.size() > 2) {
            throw new ServiceRuntimeException("Coudn't create range filter from: " + filters);
        } else if (!filters.isEmpty()) {
            return ff.and(filters);
        }
        if (in != null) {
            filters = in.stream()
                    .map(value -> ff.equal(name, ff.literal(value), isCaseSensitive()))
                    .collect(Collectors.toList());
            return filters.isEmpty() ? null : ff.or(filters);
        }
        if (notIn != null) {
            filters = notIn.stream()
                    .map(value -> ff.notEqual(name, ff.literal(value), isCaseSensitive()))
                    .collect(Collectors.toList());
            return filters.isEmpty() ? null : ff.and(filters);
        }
        if (like != null) {
            filters = like.stream()
                    .map(value -> ff.like(name, value))
                    .collect(Collectors.toList());
            return filters.isEmpty() ? null : ff.or(filters);
        }
        if (notLike != null) {
            filters = notLike.stream()
                    .map(value -> ff.like(name, value))
                    .collect(Collectors.toList());
            return filters.isEmpty() ? null : ff.and(filters);
        }
        return null;
    }
}
