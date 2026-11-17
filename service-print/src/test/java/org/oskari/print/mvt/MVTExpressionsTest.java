package org.oskari.print.mvt;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import fi.nls.oskari.util.JSONHelper;

public class MVTExpressionsTest {

    private static final double DELTA = 0.0001;

    @Test
    public void linearInterpolationBetweenStopsClampsOutsideThem() {
        JSONArray expression = JSONHelper.createJSONArray(
                "[\"interpolate\", [\"linear\"], [\"zoom\"], 10, 1, 20, 11]");
        Assertions.assertEquals(1, MVTExpressions.getNumber(expression, 10, -1), DELTA, "at first stop");
        Assertions.assertEquals(6, MVTExpressions.getNumber(expression, 15, -1), DELTA, "halfway");
        Assertions.assertEquals(11, MVTExpressions.getNumber(expression, 20, -1), DELTA, "at last stop");
        Assertions.assertEquals(1, MVTExpressions.getNumber(expression, 5, -1), DELTA, "below first stop");
        Assertions.assertEquals(11, MVTExpressions.getNumber(expression, 25, -1), DELTA, "above last stop");
    }

    @Test
    public void stepPicksValueOfLastPassedStop() {
        JSONArray expression = JSONHelper.createJSONArray(
                "[\"step\", [\"zoom\"], 0, 10, 1, 20, 2]");
        Assertions.assertEquals(0, MVTExpressions.getNumber(expression, 9, -1), DELTA, "before first stop");
        Assertions.assertEquals(1, MVTExpressions.getNumber(expression, 10, -1), DELTA, "stops are inclusive");
        Assertions.assertEquals(1, MVTExpressions.getNumber(expression, 19, -1), DELTA, "between stops");
        Assertions.assertEquals(2, MVTExpressions.getNumber(expression, 20, -1), DELTA, "at second stop");
    }

    @Test
    public void nonNumericStopsAreNotInterpolated() {
        JSONArray expression = JSONHelper.createJSONArray(
                "[\"interpolate\", [\"linear\"], [\"zoom\"], 10, \"#000\", 20, \"#fff\"]");
        Assertions.assertEquals("#000", MVTExpressions.getString(expression, 15, null),
                "colors step to the lower stop");
        Assertions.assertEquals("#fff", MVTExpressions.getString(expression, 20, null),
                "interpolate expression at stop");
        JSONObject function = JSONHelper.createJSONObject(
                "{\"base\": 1, \"stops\": [[10, \"point\"], [11, \"line\"]]}");
        Assertions.assertEquals("line", MVTExpressions.getString(function, 11, null),
                "legacy stop function at stop");
    }

    @Test
    public void legacyStopFunctionIsSupported() {
        JSONObject function = JSONHelper.createJSONObject(
                "{\"stops\": [[10, 1], [20, 11]]}");
        Assertions.assertEquals(6, MVTExpressions.getNumber(function, 15, -1), DELTA, "halfway");
    }
}
