package org.oskari.print.mvt;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import fi.nls.oskari.util.JSONHelper;

public class MVTFilterTest {

    private static Map<String, Object> attributes(Object... keysAndValues) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i + 1 < keysAndValues.length; i += 2) {
            map.put((String) keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }

    private static Predicate<Map<String, Object>> parse(String filter) {
        return MVTFilter.parse(JSONHelper.createJSONArray(filter));
    }

    @Test
    public void equalsComparesPropertyInBothSyntaxes() {
        Predicate<Map<String, Object>> legacy = parse("[\"==\", \"class\", \"road\"]");
        Assertions.assertTrue(legacy.test(attributes("class", "road")), "matching value");
        Assertions.assertFalse(legacy.test(attributes("class", "water")), "other value");
        Assertions.assertFalse(legacy.test(attributes()), "missing property");

        Predicate<Map<String, Object>> expression = parse("[\"==\", [\"get\", \"class\"], \"road\"]");
        Assertions.assertTrue(expression.test(attributes("class", "road")), "expression, matching value");
        Assertions.assertFalse(expression.test(attributes("class", "water")), "expression, other value");
    }

    @Test
    public void notEqualsMatchesMissingProperty() {
        Predicate<Map<String, Object>> filter = parse("[\"!=\", \"class\", \"road\"]");
        Assertions.assertTrue(filter.test(attributes()), "missing property is not equal");
        Assertions.assertFalse(filter.test(attributes("class", "road")), "matching value");
    }

    @Test
    public void comparisonOfIncomparableValuesDoesNotMatch() {
        Predicate<Map<String, Object>> filter = parse("[\"<\", \"population\", 100]");
        Assertions.assertFalse(filter.test(attributes("population", "lots")), "string vs number");
        Assertions.assertFalse(filter.test(attributes()), "missing property");
    }

    @Test
    public void inSupportsLiteralArraySyntax() {
        Predicate<Map<String, Object>> filter = parse(
                "[\"in\", [\"get\", \"class\"], [\"literal\", [\"road\", \"path\"]]]");
        Assertions.assertTrue(filter.test(attributes("class", "road")), "listed value");
        Assertions.assertFalse(filter.test(attributes("class", "water")), "unlisted value");
    }

    @Test
    public void matchPicksFeaturesByListedValues() {
        // As the MTK styles pick their features by kohdeluokka, which the tiles carry as a long
        Predicate<Map<String, Object>> filter = parse(
                "[\"match\", [\"get\", \"kohdeluokka\"], [32611, 32200], true, false]");
        Assertions.assertTrue(filter.test(attributes("kohdeluokka", 32611L)), "listed value");
        Assertions.assertFalse(filter.test(attributes("kohdeluokka", 12345L)), "unlisted value");
        Assertions.assertFalse(filter.test(attributes()), "missing property takes the fallback");
    }

    @Test
    public void unsupportedSubFilterMakesWholeFilterMatchEverything() {
        Predicate<Map<String, Object>> filter = parse(
                "[\"all\", [\"==\", \"class\", \"road\"], [\"case\", [\"has\", \"x\"], true, false]]");
        Assertions.assertTrue(filter.test(attributes("class", "water")),
                "an unsupported part must not silently drop features");
    }

    @Test
    public void notOfUnsupportedExpressionMatchesEverything() {
        Predicate<Map<String, Object>> filter = parse(
                "[\"all\", [\"==\", \"class\", \"road\"], [\"!\", [\"case\", [\"has\", \"x\"], true, false]]]");
        Assertions.assertTrue(filter.test(attributes("class", "water")),
                "an unsupported negated part must not silently drop features");
    }

    @Test
    public void nullLiteralEqualsMissingProperty() {
        Predicate<Map<String, Object>> equals = parse("[\"==\", [\"get\", \"name\"], null]");
        Assertions.assertTrue(equals.test(attributes()), "missing property equals null");
        Assertions.assertFalse(equals.test(attributes("name", "Helsinki")), "present property");
        Predicate<Map<String, Object>> notEquals = parse("[\"!=\", [\"get\", \"name\"], null]");
        Assertions.assertFalse(notEquals.test(attributes()), "missing property equals null");
        Assertions.assertTrue(notEquals.test(attributes("name", "Helsinki")), "present property");
    }
}
