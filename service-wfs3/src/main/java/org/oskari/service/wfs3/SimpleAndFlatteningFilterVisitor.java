package org.oskari.service.wfs3;

import java.util.ArrayList;
import java.util.List;

import org.opengis.filter.And;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNil;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;
import org.opengis.filter.temporal.After;
import org.opengis.filter.temporal.AnyInteracts;
import org.opengis.filter.temporal.Before;
import org.opengis.filter.temporal.Begins;
import org.opengis.filter.temporal.BegunBy;
import org.opengis.filter.temporal.During;
import org.opengis.filter.temporal.EndedBy;
import org.opengis.filter.temporal.Ends;
import org.opengis.filter.temporal.Meets;
import org.opengis.filter.temporal.MetBy;
import org.opengis.filter.temporal.OverlappedBy;
import org.opengis.filter.temporal.TContains;
import org.opengis.filter.temporal.TEquals;
import org.opengis.filter.temporal.TOverlaps;

/**
 * Flatten
 * (a AND b) AND (c AND (d AND e))
 * into a AND b AND c AND d AND e
 *
 */
public class SimpleAndFlatteningFilterVisitor implements FilterVisitor {

    private final FilterFactory ff;

    public SimpleAndFlatteningFilterVisitor(FilterFactory ff) {
        this.ff = ff;
    }

    @Override
    public Object visitNullFilter(Object extraData) {
        return null;
    }

    @Override
    public Object visit(ExcludeFilter filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(IncludeFilter filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(And filter, Object extraData) {
        if (filter.getChildren().stream().anyMatch(it -> it instanceof Or)) {
            return filter;
        }
        if (filter.getChildren().stream().noneMatch(it -> it instanceof And)) {
            return filter;
        }
        List<Filter> flattened = new ArrayList<>();
        boolean ok = flatten(filter, flattened);
        return ok ? ff.and(flattened) : filter;
    }

    private boolean flatten(And filter, List<Filter> flattened) {
        for (Filter child : filter.getChildren()) {
            if (child instanceof Or) {
                return false;
            } else if (child instanceof And) {
                if (!flatten((And) child, flattened)) {
                    return false;
                }
            } else {
                flattened.add(child);
            }
        }
        return true;
    }


    @Override
    public Object visit(Id filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(Not filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(Or filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(PropertyIsBetween filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(PropertyIsEqualTo filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(PropertyIsNotEqualTo filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(PropertyIsGreaterThan filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(PropertyIsGreaterThanOrEqualTo filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(PropertyIsLessThan filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(PropertyIsLessThanOrEqualTo filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(PropertyIsLike filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(PropertyIsNull filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(PropertyIsNil filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(BBOX filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(Beyond filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(Contains filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(Crosses filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(Disjoint filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(DWithin filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(Equals filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(Intersects filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(Overlaps filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(Touches filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(Within filter, Object extraData) {
        return filter;
    }

    @Override
    public Object visit(After after, Object extraData) {
        return after;
    }

    @Override
    public Object visit(AnyInteracts anyInteracts, Object extraData) {
        return anyInteracts;
    }

    @Override
    public Object visit(Before before, Object extraData) {
        return before;
    }

    @Override
    public Object visit(Begins begins, Object extraData) {
        return begins;
    }

    @Override
    public Object visit(BegunBy begunBy, Object extraData) {
        return begunBy;
    }

    @Override
    public Object visit(During during, Object extraData) {
        return during;
    }

    @Override
    public Object visit(EndedBy endedBy, Object extraData) {
        return endedBy;
    }

    @Override
    public Object visit(Ends ends, Object extraData) {
        return ends;
    }

    @Override
    public Object visit(Meets meets, Object extraData) {
        return meets;
    }

    @Override
    public Object visit(MetBy metBy, Object extraData) {
        return metBy;
    }

    @Override
    public Object visit(OverlappedBy overlappedBy, Object extraData) {
        return overlappedBy;
    }

    @Override
    public Object visit(TContains contains, Object extraData) {
        return contains;
    }

    @Override
    public Object visit(TEquals equals, Object extraData) {
        return equals;
    }

    @Override
    public Object visit(TOverlaps contains, Object extraData) {
        return contains;
    }

}
