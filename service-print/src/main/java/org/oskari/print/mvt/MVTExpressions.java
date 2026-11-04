package org.oskari.print.mvt;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Resolves the subset of Mapbox GL style expressions that print supports:
 * literal values and zoom dependent "interpolate" / "step" expressions.
 *
 * Data driven expressions (ones reading feature properties) are not supported,
 * resolving one returns the fallback value.
 */
class MVTExpressions {

    private MVTExpressions() {}

    /**
     * Resolve a paint/layout property value at given zoom level.
     * @return resolved number or fallback if the value is missing or unsupported
     */
    public static double getNumber(Object value, double zoom, double fallback) {
        Object resolved = resolve(value, zoom);
        if (resolved instanceof Number) {
            return ((Number) resolved).doubleValue();
        }
        return fallback;
    }

    /**
     * Resolve a paint/layout property value at given zoom level.
     * @return resolved String or fallback if the value is missing or unsupported
     */
    public static String getString(Object value, double zoom, String fallback) {
        Object resolved = resolve(value, zoom);
        if (resolved instanceof String) {
            return (String) resolved;
        }
        return fallback;
    }

    private static Object resolve(Object value, double zoom) {
        if (value == null || value == JSONObject.NULL) {
            return null;
        }
        if (value instanceof JSONArray) {
            return resolveExpression((JSONArray) value, zoom);
        }
        if (value instanceof JSONObject) {
            // Legacy "stop function": { "base": x, "stops": [[zoom, value], ...] }
            return resolveStopFunction((JSONObject) value, zoom);
        }
        return value;
    }

    private static Object resolveExpression(JSONArray expression, double zoom) {
        String operator = expression.optString(0, null);
        if (operator == null) {
            return null;
        }
        switch (operator) {
        case "literal":
            return expression.opt(1);
        case "interpolate":
            return resolveInterpolate(expression, zoom);
        case "step":
            return resolveStep(expression, zoom);
        default:
            return null;
        }
    }

    /**
     * ["interpolate", ["linear"]|["exponential", base], ["zoom"], stop, value, ...]
     */
    private static Object resolveInterpolate(JSONArray expression, double zoom) {
        if (!isZoomInput(expression.opt(2))) {
            return null;
        }
        double base = getInterpolationBase(expression.opt(1));
        if (base < 0) {
            return null;
        }

        // Stops start at index 3 as [input, output] pairs
        final int first = 3;
        if (expression.length() < first + 2) {
            return null;
        }

        double prevStop = expression.optDouble(first, Double.NaN);
        Object prevValue = expression.opt(first + 1);
        if (Double.isNaN(prevStop) || zoom <= prevStop) {
            return prevValue;
        }

        for (int i = first + 2; i + 1 < expression.length(); i += 2) {
            double stop = expression.optDouble(i, Double.NaN);
            Object value = expression.opt(i + 1);
            if (Double.isNaN(stop)) {
                return prevValue;
            }
            if (zoom <= stop) {
                return interpolate(prevStop, prevValue, stop, value, zoom, base);
            }
            prevStop = stop;
            prevValue = value;
        }
        return prevValue;
    }

    /**
     * ["step", ["zoom"], defaultValue, stop, value, ...]
     */
    private static Object resolveStep(JSONArray expression, double zoom) {
        if (!isZoomInput(expression.opt(1))) {
            return null;
        }
        Object result = expression.opt(2);
        for (int i = 3; i + 1 < expression.length(); i += 2) {
            double stop = expression.optDouble(i, Double.NaN);
            if (Double.isNaN(stop) || zoom < stop) {
                break;
            }
            result = expression.opt(i + 1);
        }
        return result;
    }

    /**
     * Legacy function syntax: { "base": x, "stops": [[zoom, value], ...] }
     */
    private static Object resolveStopFunction(JSONObject function, double zoom) {
        if (function.has("property")) {
            return null;
        }
        JSONArray stops = function.optJSONArray("stops");
        if (stops == null || stops.length() == 0) {
            return null;
        }
        double base = function.optDouble("base", 1);

        JSONArray prev = stops.optJSONArray(0);
        if (prev == null) {
            return null;
        }
        if (zoom <= prev.optDouble(0, Double.NaN)) {
            return prev.opt(1);
        }
        for (int i = 1; i < stops.length(); i++) {
            JSONArray current = stops.optJSONArray(i);
            if (current == null) {
                break;
            }
            double stop = current.optDouble(0, Double.NaN);
            if (Double.isNaN(stop)) {
                break;
            }
            if (zoom <= stop) {
                return interpolate(prev.optDouble(0, Double.NaN), prev.opt(1),
                        stop, current.opt(1), zoom, base);
            }
            prev = current;
        }
        return prev.opt(1);
    }

    private static boolean isZoomInput(Object input) {
        if (!(input instanceof JSONArray)) {
            return false;
        }
        JSONArray arr = (JSONArray) input;
        return arr.length() == 1 && "zoom".equals(arr.optString(0, null));
    }

    /**
     * @return interpolation base, 1 for linear, -1 if the interpolation type is unsupported
     */
    private static double getInterpolationBase(Object interpolation) {
        if (!(interpolation instanceof JSONArray)) {
            return -1;
        }
        JSONArray arr = (JSONArray) interpolation;
        String type = arr.optString(0, null);
        if ("linear".equals(type) || "cubic-bezier".equals(type)) { // cubic-bezier approximated as linear
            return 1;
        }
        if ("exponential".equals(type)) {
            return arr.optDouble(1, 1);
        }
        return -1;
    }

    private static Object interpolate(double stop1, Object value1,
            double stop2, Object value2, double zoom, double base) {
        if (!(value1 instanceof Number) || !(value2 instanceof Number)) {
            // Only numeric values are interpolated, colors etc. step at each stop
            return zoom >= stop2 ? value2 : value1;
        }
        double t = getInterpolationFactor(zoom, base, stop1, stop2);
        double v1 = ((Number) value1).doubleValue();
        double v2 = ((Number) value2).doubleValue();
        return v1 + t * (v2 - v1);
    }

    private static double getInterpolationFactor(double zoom, double base, double stop1, double stop2) {
        double difference = stop2 - stop1;
        if (difference == 0) {
            return 0;
        }
        double progress = zoom - stop1;
        if (base == 1) {
            return progress / difference;
        }
        return (Math.pow(base, progress) - 1) / (Math.pow(base, difference) - 1);
    }
}
