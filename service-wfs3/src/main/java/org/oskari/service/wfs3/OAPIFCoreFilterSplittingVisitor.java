package org.oskari.service.wfs3;

import java.util.Set;

import org.geotools.filter.FilterCapabilities;
import org.geotools.filter.visitor.PostPreProcessFilterSplittingVisitor;
import org.opengis.filter.Filter;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

/**
 * Split a Filter into pre-filter and post-filter.
 * Pre-filter is the part the OAPIF server can handle and apply
 * Post-filter is the part oskari-server has to handle and apply to the response from the OAPIF server
 *
 * This class overrides visit(PropertyIsEqualTo) method as the super class
 * handles all SIMPLE_COMPARISONS_OPENGIS as same group and we don't want that
 * since we only support PropertyIsEqualTo from that group
 *
 * Also override the visit(Or) as we want to be able to handle case where
 * all children of the Or are simple PropertyIsEqualTo filters and that all
 * of them share the same PropertyName
 */
class OAPIFCoreFilterSplittingVisitor extends PostPreProcessFilterSplittingVisitor {

    private final Set<String> queryables;

    public OAPIFCoreFilterSplittingVisitor(FilterCapabilities fcs, Set<String> queryables) {
        super(fcs, null, null);
        this.queryables = queryables;
    }

    @Override
    public Object visit(PropertyName expression, Object notUsed) {
        if (queryables != null && queryables.contains(expression.getPropertyName())) {
            preStack.push(expression);
        } else {
            postStack.push(expression);
        }
        return null;
    }

    @Override
    public Object visit(PropertyIsEqualTo filter, Object notUsed) {
        if (original == null) original = filter;

        int i = postStack.size();
        Expression leftValue = filter.getExpression1();
        Expression rightValue = filter.getExpression2();
        if (leftValue == null || rightValue == null) {
            postStack.push(filter);
            return null;
        }

        leftValue.accept(this, null);

        if (i < postStack.size()) {
            postStack.pop();
            postStack.push(filter);

            return null;
        }

        rightValue.accept(this, null);

        if (i < postStack.size()) {
            preStack.pop(); // left
            postStack.pop();
            postStack.push(filter);

            return null;
        }

        preStack.pop(); // left side
        preStack.pop(); // right side
        preStack.push(filter);

        return null;
    }

    public Object visit(Or filter, Object notUsed) {
        if (isSimpleIN(filter)) {
            preStack.push(filter);
        } else {
            super.visit(filter, notUsed);
        }
        return null;
    }

    private boolean isSimpleIN(Or filter) {
        String prevName = null;

        for (Filter f : filter.getChildren()) {
            if (!(f instanceof PropertyIsEqualTo)) {
                return false;
            }

            PropertyName prop = getPropertyName((PropertyIsEqualTo) f);
            if (prop == null) {
                return false;
            }
            String propName = prop.getPropertyName();

            if (prevName == null) {
                prevName = propName;
            } else if (propName.equals(prevName)) {
                continue;
            } else {
                return false;
            }
        }

        return true;
    }

    private PropertyName getPropertyName(PropertyIsEqualTo eq) {
        if (eq.getExpression1() instanceof PropertyName && eq.getExpression2() instanceof Literal) {
            return (PropertyName) eq.getExpression1();
        } else if (eq.getExpression2() instanceof PropertyName && eq.getExpression1() instanceof Literal) {
            return (PropertyName) eq.getExpression2();
        } else {
            return null;
        }
    }

}
