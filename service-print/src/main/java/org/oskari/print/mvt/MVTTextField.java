package org.oskari.print.mvt;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.ToDoubleFunction;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Resolves a symbol layer's text-field, a {property} template or a
 * get/coalesce/concat/format expression, into the string to draw. Per section
 * fonts and sizes of format are ignored.
 */
class MVTTextField {

    private MVTTextField() {}

    /**
     * @param textField the layer's text-field, as it appears in the style
     * @param attributes the feature's properties
     * @return the label, or null when the feature has nothing to show
     */
    public static String resolve(Object textField, Map<String, Object> attributes) {
        String text = resolveRaw(textField, attributes);
        if (text == null) {
            return null;
        }
        // A template with a missing property leaves whitespace behind, and a
        // coalesce over languages can leave the separator of an empty section
        text = text.strip();
        return text.isEmpty() ? null : text;
    }

    private static String resolveRaw(Object textField, Map<String, Object> attributes) {
        if (textField == null || textField == JSONObject.NULL) {
            return null;
        }
        if (textField instanceof String) {
            return fromTemplate((String) textField, attributes);
        }
        if (textField instanceof JSONArray) {
            return fromExpression((JSONArray) textField, attributes);
        }
        return String.valueOf(textField);
    }

    /**
     * A property the feature lacks becomes empty, so a template can name both a
     * latin and a non-latin name.
     */
    private static String fromTemplate(String template, Map<String, Object> attributes) {
        if (template.indexOf('{') < 0) {
            return template;
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < template.length()) {
            char c = template.charAt(i);
            if (c != '{') {
                sb.append(c);
                i++;
                continue;
            }
            int end = template.indexOf('}', i);
            if (end < 0) {
                // An unclosed brace is taken literally rather than dropping the rest
                sb.append(template.substring(i));
                break;
            }
            Object value = attributes.get(template.substring(i + 1, end));
            if (value != null) {
                sb.append(value);
            }
            i = end + 1;
        }
        return sb.toString();
    }

    private static String fromExpression(JSONArray expression, Map<String, Object> attributes) {
        String operator = expression.optString(0, null);
        if (operator == null) {
            return null;
        }
        switch (operator) {
        case "get":
            Object value = attributes.get(expression.optString(1, null));
            return value == null ? null : String.valueOf(value);
        case "coalesce":
            for (int i = 1; i < expression.length(); i++) {
                String resolved = resolveRaw(expression.opt(i), attributes);
                if (resolved != null && !resolved.isEmpty()) {
                    return resolved;
                }
            }
            return null;
        case "concat":
        case "format":
            return join(expression, attributes);
        case "literal":
            Object literal = expression.opt(1);
            return literal == null ? null : String.valueOf(literal);
        case "to-string":
            return resolveRaw(expression.opt(1), attributes);
        default:
            return null;
        }
    }

    private static String join(JSONArray expression, Map<String, Object> attributes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < expression.length(); i++) {
            Object part = expression.opt(i);
            if (part instanceof JSONObject) {
                // The options of the preceding section
                continue;
            }
            String resolved = resolveRaw(part, attributes);
            if (resolved != null) {
                sb.append(resolved);
            }
        }
        return sb.toString();
    }

    public static String transform(String text, String textTransform) {
        if (text == null || textTransform == null) {
            return text;
        }
        switch (textTransform) {
        case "uppercase":
            // Not the JVM's default locale: a server running in a Turkish one
            // would turn "i" into a dotted capital
            return text.toUpperCase(Locale.ROOT);
        case "lowercase":
            return text.toLowerCase(Locale.ROOT);
        default:
            return text;
        }
    }

    /**
     * @param maxWidthEm text-max-width, 0 or less to only honour explicit newlines
     * @param widthOfEm  measures a string in ems
     */
    public static List<String> wrap(String text, double maxWidthEm,
            ToDoubleFunction<String> widthOfEm) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\n")) {
            if (maxWidthEm <= 0 || widthOfEm.applyAsDouble(paragraph) <= maxWidthEm) {
                lines.add(paragraph);
                continue;
            }
            wrapParagraph(paragraph, maxWidthEm, widthOfEm, lines);
        }
        return lines;
    }

    private static void wrapParagraph(String paragraph, double maxWidthEm,
            ToDoubleFunction<String> widthOfEm, List<String> lines) {
        StringBuilder line = new StringBuilder();
        for (String word : paragraph.split(" ")) {
            if (word.isEmpty()) {
                continue;
            }
            if (line.length() == 0) {
                line.append(word);
                continue;
            }
            String candidate = line + " " + word;
            if (widthOfEm.applyAsDouble(candidate) <= maxWidthEm) {
                line.setLength(0);
                line.append(candidate);
            } else {
                // A word longer than the limit still gets its own line rather
                // than being broken mid word
                lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
    }
}
