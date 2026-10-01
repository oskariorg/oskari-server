package org.oskari.print.mvt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Compiles the supported subset of Mapbox GL style filters into a predicate
 * over MVT feature attributes.
 *
 * Both the legacy filter syntax ["==", "key", value] and the expression syntax
 * ["==", ["get", "key"], value] are supported as styles in the wild mix them.
 * An unsupported filter matches everything, drawing too much rather than
 * dropping the layer from the print.
 */
class MVTFilter {

    /** Not a tile attribute: MVTStyleLayer puts the geometry type in under this key for one test. */
    static final String TYPE_KEY = "$type";
    static final String GEOMETRY_TYPE = "geometry-type";

    private static final Predicate<Map<String, Object>> MATCH_ALL = attributes -> true;

    private MVTFilter() {}

    public static Predicate<Map<String, Object>> parse(JSONArray filter) {
        if (filter == null || filter.length() == 0) {
            return MATCH_ALL;
        }
        Predicate<Map<String, Object>> parsed = parseFilter(filter);
        return parsed != null ? parsed : MATCH_ALL;
    }

    /**
     * @return null when the filter is not supported
     */
    private static Predicate<Map<String, Object>> parseFilter(JSONArray filter) {
        String operator = filter.optString(0, null);
        if (operator == null) {
            return null;
        }
        switch (operator) {
        case "all":
            return combine(filter, true);
        case "any":
            return combine(filter, false);
        case "none":
            return negate(combine(filter, false));
        case "!":
            JSONArray negated = filter.length() == 2 ? filter.optJSONArray(1) : null;
            return negated == null ? null : negate(parseFilter(negated));
        case "==":
            return compare(filter, result -> result != null && result == 0);
        case "!=":
            return negate(compare(filter, result -> result != null && result == 0));
        case "<":
            return compare(filter, result -> result != null && result < 0);
        case "<=":
            return compare(filter, result -> result != null && result <= 0);
        case ">":
            return compare(filter, result -> result != null && result > 0);
        case ">=":
            return compare(filter, result -> result != null && result >= 0);
        case "in":
            return in(filter);
        case "!in":
            return negate(in(filter));
        case "has":
            return has(filter);
        case "!has":
            return negate(has(filter));
        case "match":
            Function<Map<String, Object>, Object> match = match(filter);
            return match == null ? null : attributes -> Boolean.TRUE.equals(match.apply(attributes));
        default:
            return null;
        }
    }

    private static Predicate<Map<String, Object>> negate(Predicate<Map<String, Object>> predicate) {
        return predicate == null ? null : predicate.negate();
    }

    private static Predicate<Map<String, Object>> combine(JSONArray filter, boolean and) {
        List<Predicate<Map<String, Object>>> predicates = new ArrayList<>();
        for (int i = 1; i < filter.length(); i++) {
            JSONArray sub = filter.optJSONArray(i);
            if (sub == null) {
                return null;
            }
            Predicate<Map<String, Object>> predicate = parseFilter(sub);
            if (predicate == null) {
                return null;
            }
            predicates.add(predicate);
        }
        if (predicates.isEmpty()) {
            return MATCH_ALL;
        }
        return attributes -> {
            for (Predicate<Map<String, Object>> predicate : predicates) {
                if (predicate.test(attributes) != and) {
                    return !and;
                }
            }
            return and;
        };
    }

    private static Predicate<Map<String, Object>> compare(JSONArray filter,
            Predicate<Integer> acceptResult) {
        String key = getKey(filter.opt(1));
        if (key == null) {
            return null;
        }
        Object expected = filter.opt(2);
        return attributes -> acceptResult.test(compareValues(getValue(attributes, key), expected));
    }

    private static Predicate<Map<String, Object>> in(JSONArray filter) {
        String key = getKey(filter.opt(1));
        if (key == null) {
            return null;
        }
        List<Object> values = new ArrayList<>();
        for (int i = 2; i < filter.length(); i++) {
            values.add(filter.opt(i));
        }
        // Expression syntax allows ["in", value, ["literal", [...]]]
        if (values.size() == 1 && values.get(0) instanceof JSONArray) {
            JSONArray literal = (JSONArray) values.get(0);
            values.clear();
            JSONArray items = "literal".equals(literal.optString(0, null)) ? literal.optJSONArray(1) : literal;
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    values.add(items.opt(i));
                }
            }
        }
        return attributes -> {
            Object value = getValue(attributes, key);
            for (Object candidate : values) {
                Integer result = compareValues(value, candidate);
                if (result != null && result == 0) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Compiles ["match", ["get", key], label, output, ..., fallback], where a
     * label is a single value or an array of them.
     *
     * @return the output for a feature's attributes, null when the input is
     *         something other than a property of the feature
     */
    static Function<Map<String, Object>, Object> match(JSONArray expression) {
        // "match", input, label/output pairs and the fallback
        if (expression.length() < 5 || expression.length() % 2 == 0
                || !(expression.opt(1) instanceof JSONArray)) {
            return null;
        }
        String key = getKey(expression.opt(1));
        if (key == null) {
            return null;
        }
        List<List<Object>> labels = new ArrayList<>();
        List<Object> outputs = new ArrayList<>();
        for (int i = 2; i + 1 < expression.length(); i += 2) {
            Object label = expression.opt(i);
            List<Object> values = new ArrayList<>();
            if (label instanceof JSONArray) {
                JSONArray array = (JSONArray) label;
                for (int j = 0; j < array.length(); j++) {
                    values.add(array.opt(j));
                }
            } else {
                values.add(label);
            }
            labels.add(values);
            outputs.add(expression.opt(i + 1));
        }
        Object fallback = expression.opt(expression.length() - 1);
        return attributes -> {
            Object value = getValue(attributes, key);
            for (int i = 0; i < labels.size(); i++) {
                for (Object candidate : labels.get(i)) {
                    Integer result = compareValues(value, candidate);
                    if (result != null && result == 0) {
                        return outputs.get(i);
                    }
                }
            }
            return fallback;
        };
    }

    private static Predicate<Map<String, Object>> has(JSONArray filter) {
        String key = getKey(filter.opt(1));
        if (key == null) {
            return null;
        }
        return attributes -> getValue(attributes, key) != null;
    }

    /**
     * @return property name from a legacy key or a ["get", "key"] expression,
     *         and TYPE_KEY for ["geometry-type"], which is how the expression
     *         syntax asks for the legacy $type. Null if unsupported.
     */
    private static String getKey(Object key) {
        if (key instanceof String) {
            return (String) key;
        }
        if (key instanceof JSONArray) {
            JSONArray expression = (JSONArray) key;
            String operator = expression.optString(0, null);
            if (expression.length() == 2 && "get".equals(operator)) {
                return expression.optString(1, null);
            }
            if (expression.length() == 1 && GEOMETRY_TYPE.equals(operator)) {
                return TYPE_KEY;
            }
        }
        return null;
    }

    private static Object getValue(Map<String, Object> attributes, String key) {
        if (TYPE_KEY.equals(key)) {
            return attributes == null ? null : attributes.get(TYPE_KEY);
        }
        // The other GL specials ($id) are not available here
        if (key.startsWith("$")) {
            return null;
        }
        return attributes == null ? null : attributes.get(key);
    }

    /**
     * @return negative/zero/positive like Comparable, null if the values are not comparable
     */
    private static Integer compareValues(Object value, Object expected) {
        // A null literal in the style, a missing property compares equal to it
        if (expected == JSONObject.NULL) {
            expected = null;
        }
        if (value == null || expected == null) {
            return value == expected ? Integer.valueOf(0) : null;
        }
        if (value instanceof Number && expected instanceof Number) {
            return Double.compare(((Number) value).doubleValue(), ((Number) expected).doubleValue());
        }
        if (value instanceof Boolean && expected instanceof Boolean) {
            return Boolean.compare((Boolean) value, (Boolean) expected);
        }
        if (value instanceof String && expected instanceof String) {
            return Integer.signum(((String) value).compareTo((String) expected));
        }
        return null;
    }
}
