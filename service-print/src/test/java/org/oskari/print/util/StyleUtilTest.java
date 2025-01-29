package org.oskari.print.util;

import org.apache.pdfbox.pdmodel.PDResources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.pdfbox.pdmodel.PDDocument;

import fi.nls.oskari.util.JSONHelper;
import org.oskari.print.request.PDPrintStyle;
import org.oskari.util.Customization;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;

import static org.oskari.util.Customization.PLACEHOLDER_STROKE;
import static org.oskari.util.Customization.PLACEHOLDER_FILL;


public class StyleUtilTest {
    private static final String FILL = "FF0000";

    @Test
    public void getMarkers() throws Exception  {
        JSONArray svgMarkers = Customization.getMarkers();

        PDDocument doc = new PDDocument();
        for (int i = 0; i < svgMarkers.length(); i++) {
            // test that icons are created without exceptions
            StyleUtil.getIcon(doc, i, FILL, 3);

            String data = JSONHelper.getJSONObject(svgMarkers, i).getString("data");
            Assertions.assertTrue(data.contains(PLACEHOLDER_FILL), "Marker:" + i +  " should have placeholder for fill");
            Assertions.assertTrue(data.contains(PLACEHOLDER_STROKE), "Marker: " + i +  " should have placeholder for stroke");
        }
        doc.close();
    }

    @Test
    public void getStyles() throws Exception  {
        JSONObject style = WFSLayerOptions.getDefaultOskariStyle();

        PDDocument doc = new PDDocument();
        PDPrintStyle point = StyleUtil.getPointStyle(style, doc);
        Assertions.assertNotNull(point.getIcon(), "Point should have icon");
        doc.close();

        PDPrintStyle line = StyleUtil.getLineStyle(style);
        Assertions.assertTrue(line.hasLineColor(), "Line should have color");

        PDPrintStyle polygon = StyleUtil.getPolygonStyle(style, null);
        Assertions.assertTrue(polygon.hasFillColor(), "Polygon should have fill color");
        Assertions.assertTrue(polygon.hasLineColor(), "Polygon should have line color");

        JSONObject area = style.getJSONObject("fill").getJSONObject("area");
        for (int i = 0; i <= 3; i++) {
            area.put("pattern", i);
            PDPrintStyle pattern = StyleUtil.getPolygonStyle(style, new PDResources());
            Assertions.assertTrue(pattern.hasFillColor(), "Pattern:" + i +  " should have fill color");
        }
    }
}
