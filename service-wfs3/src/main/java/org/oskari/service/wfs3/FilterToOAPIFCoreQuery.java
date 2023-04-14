package org.oskari.service.wfs3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.FilterCapabilities;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.json.JSONArray;
import org.opengis.filter.And;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
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
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.NilExpression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;
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

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.JSONHelper;

public class FilterToOAPIFCoreQuery implements FilterVisitor, ExpressionVisitor {

    static final String ATTRIBUTE_QUERYABLES = "queryables";

    private final OskariLayer layer;
    private boolean insideAnd = false;

    public FilterToOAPIFCoreQuery(OskariLayer layer) {
        this.layer = layer;
    }

    public Filter toQueryParameters(Filter filter, Map<String, String> query) {
        FilterCapabilities capabilities = createFilterCapabilities(layer);
        Set<String> queryables = getQueryables(layer);

        Filter flattenedFilter = flatten(filter);

        OAPIFCoreFilterSplittingVisitor splitter = new OAPIFCoreFilterSplittingVisitor(capabilities, queryables);
        flattenedFilter.accept(splitter, null);

        Filter preFilter = flatten(splitter.getFilterPre());
        preFilter.accept(this, query);

        return splitter.getFilterPost();
    }

    /**
     * Flatten unnecessarily nested AND expressions if obvious it can be done (no OR expressions present)
     * e.g. (a AND b) AND (c AND (d AND e))
     * <=>   a AND b  AND  c AND  d AND e
     */
    private Filter flatten(Filter filter) {
        if (!(filter instanceof And)) {
            return filter;
        }
        And and = (And) filter;
        if (and.getChildren().stream().anyMatch(it -> it instanceof Or)) {
            // Not obvious we can do it -- bail out
            return filter;
        }
        if (and.getChildren().stream().noneMatch(it -> it instanceof And)) {
            // No nested ANDs, nothing to do
            return filter;
        }
        // Recursive part
        List<Filter> flattened = new ArrayList<>();
        boolean ok = recursiveFlatten(and, flattened);
        return ok ? CommonFactoryFinder.getFilterFactory().and(flattened) : filter;
    }

    private boolean recursiveFlatten(And filter, List<Filter> flattened) {
        for (Filter child : filter.getChildren()) {
            if (child instanceof Or) {
                return false;
            } else if (child instanceof And) {
                boolean ok = recursiveFlatten((And) child, flattened);
                if (!ok) {
                    return false;
                }
            } else {
                flattened.add(child);
            }
        }
        return true;
    }

    private FilterCapabilities createFilterCapabilities(OskariLayer layer) {
        // TODO: Determine capabilities based on layer
        FilterCapabilities capabilities = new FilterCapabilities();

        capabilities.addType(FilterCapabilities.COMPARE_EQUALS);
        capabilities.addType(FilterCapabilities.LOGIC_AND);
        capabilities.addType(FilterCapabilities.SPATIAL_BBOX);

        return capabilities;
    }

    private Set<String> getQueryables(OskariLayer layer) {
        JSONArray array = JSONHelper.getJSONArray(layer.getAttributes(), ATTRIBUTE_QUERYABLES);
        if (array == null) {
            return null;
        }

        List<String> queryables = JSONHelper.getArrayAsList(array);
        if (queryables.isEmpty()) {
            return null;
        }

        return new HashSet<>(queryables);
    }

    @Override
    public Object visitNullFilter(Object extraData) {
        return extraData;
    }

    @Override
    public Object visit(ExcludeFilter filter, Object extraData) {
        return null;
    }

    @Override
    public Object visit(IncludeFilter filter, Object extraData) {
        return extraData;
    }

    @Override
    public Object visit(And filter, Object extraData) {
        List<Filter> children = filter.getChildren();
        if (children != null && !children.isEmpty()) {
            if (insideAnd) {
                throw new UnsupportedOperationException("Nested AND not supported");
            }
            insideAnd = true;
            for (Filter child : children) {
                child.accept(this, extraData);
            }
            insideAnd = false;
        }
        return extraData;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object visit(Or filter, Object extraData) {
        Map<String, String> query = (Map<String, String>) extraData;
        for (Filter f : filter.getChildren()) {
            visit((PropertyIsEqualTo) f, query, true);
        }
        return extraData;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object visit(PropertyIsEqualTo filter, Object extraData) {
        visit(filter, (Map<String, String>) extraData, false);
        return extraData;
    }

    private void visit(PropertyIsEqualTo filter, Map<String, String> query, boolean insideOr) {
        Expression e1 = filter.getExpression1();
        Expression e2 = filter.getExpression2();

        PropertyName prop;
        Literal literal;
        if (e1 instanceof PropertyName && e2 instanceof Literal) {
            prop = (PropertyName) e1;
            literal = (Literal) e2;
        } else if (e2 instanceof PropertyName && e1 instanceof Literal) {
            prop = (PropertyName) e2;
            literal = (Literal) e1;
        } else {
            throw new UnsupportedOperationException("Expressions must be one PropertyName and one Literal");
        }

        Object key = prop.accept(this, null);
        Object value = literal.accept(this, null);
        if (key == null || value == null) {
            return;
        }
        String k = key.toString();
        String v = value.toString();
        if (insideOr) {
            // foo = 1 OR foo = 2 => &foo=1,2
            query.compute(k, (__, curr) -> (curr == null) ? v : curr + "," + v);
        } else {
            String curr = query.get(k);
            if (curr == null) {
                query.put(k, v);
            } else {
                if (curr.equals(v)) {
                    // foo = 1 AND foo = 1, OK just ignore it(?)
                    return;
                }
                // foo = 1 AND foo = 2 => Doesn't make sense
                // We probably should handle this before entering here, look into extending SimplifyingFilterVisitor
                throw new UnsupportedOperationException("Multiple PropertyIsEqualTo filters with different values for same PropertyName");
            }
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public Object visit(BBOX filter, Object extraData) {
        // TODO: Ignore other than "primary" geometry for now
        ReferencedEnvelope bbox = (ReferencedEnvelope) filter.getBounds();
        Map<String, String> query = (Map<String, String>) extraData;
        OskariWFS3Client.addBboxToQuery(layer, bbox, query);
        return query;
    }

    @Override
    public Object visit(PropertyName expression, Object extraData) {
        // TODO: Check this PropertyName is queryable
        return expression.getPropertyName();
    }

    @Override
    public Object visit(Literal expression, Object extraData) {
        return expression.getValue();
    }

    @Override
    public Object visit(PropertyIsBetween filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(PropertyIsNotEqualTo filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(PropertyIsGreaterThan filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(PropertyIsGreaterThanOrEqualTo filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(PropertyIsLessThan filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(PropertyIsLessThanOrEqualTo filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(PropertyIsLike filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(PropertyIsNull filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(Not filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }


    @Override
    public Object visit(Id filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }


    @Override
    public Object visit(PropertyIsNil filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }


    @Override
    public Object visit(Beyond filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(Contains filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(Crosses filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(Disjoint filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(DWithin filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(Equals filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(Intersects filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }


    @Override
    public Object visit(Overlaps filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(Touches filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(Within filter, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(After after, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(AnyInteracts anyInteracts, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(Before before, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(Begins begins, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(BegunBy begunBy, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(During during, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(EndedBy endedBy, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(Ends ends, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(Meets meets, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(MetBy metBy, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(OverlappedBy overlappedBy, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(TContains contains, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(TEquals equals, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(TOverlaps contains, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(NilExpression expression, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(Add expression, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(Divide expression, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(Function expression, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(Multiply expression, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public Object visit(Subtract expression, Object extraData) {
        throw new UnsupportedOperationException("Not supported");
    }

}
