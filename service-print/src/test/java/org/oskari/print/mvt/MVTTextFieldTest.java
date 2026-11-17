package org.oskari.print.mvt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import fi.nls.oskari.util.JSONHelper;

public class MVTTextFieldTest {

    private static Map<String, Object> attributes(String... keyValues) {
        Map<String, Object> attributes = new HashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            attributes.put(keyValues[i], keyValues[i + 1]);
        }
        return attributes;
    }

    private static JSONArray expression(String json) {
        return JSONHelper.createJSONArray(json);
    }

    @Test
    public void templateWithSeveralPropertiesKeepsTheOnesThatAreThere() {
        // OSM Bright labels carry a latin and a non-latin name side by side
        Map<String, Object> attributes = attributes("name:latin", "Helsinki");
        Assertions.assertEquals("Helsinki",
                MVTTextField.resolve("{name:latin} {name:nonlatin}", attributes),
                "a missing property leaves nothing behind");
    }

    @Test
    public void coalescePicksTheFirstNameThatIsSet() {
        // The NLS basemap carries a place name in five languages
        JSONArray field = expression("[\"coalesce\", [\"get\", \"nimi_fin\"],"
                + " [\"get\", \"nimi_swe\"], [\"get\", \"nimi_sme\"]]");
        Assertions.assertEquals("Utsjoki",
                MVTTextField.resolve(field, attributes("nimi_swe", "Utsjoki")),
                "falls through to the language that is present");
        Assertions.assertEquals("Ohcejohka",
                MVTTextField.resolve(field, attributes("nimi_fin", "Ohcejohka", "nimi_swe", "x")),
                "the first one wins when several are set");
        Assertions.assertNull(MVTTextField.resolve(field, attributes()),
                "a feature with no name at all has no label");
    }

    @Test
    public void formatJoinsItsSectionsAndSkipsTheirOptions() {
        JSONArray field = expression("[\"format\", [\"get\", \"a\"], {\"font-scale\": 1.2},"
                + " \" \", {}, [\"get\", \"b\"], {}]");
        Assertions.assertEquals("one two",
                MVTTextField.resolve(field, attributes("a", "one", "b", "two")),
                "sections are concatenated, their options ignored");
    }

    @Test
    public void wrapHonoursMaxWidth() {
        // One unit per character, so the limit is in characters
        List<String> lines = MVTTextField.wrap("aaa bbb ccc", 7, text -> text.length());
        Assertions.assertEquals(2, lines.size(), "wrapped once");
        Assertions.assertEquals("aaa bbb", lines.get(0), "fills up to the limit");
        Assertions.assertEquals("ccc", lines.get(1), "and continues on the next line");
        Assertions.assertEquals(1, MVTTextField.wrap("Hämeenlinnantie", 5, text -> text.length()).size(),
                "a long word is not broken mid word");
    }
}
