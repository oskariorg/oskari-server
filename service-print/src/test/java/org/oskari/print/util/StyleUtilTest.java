package org.oskari.print.util;

import org.apache.pdfbox.pdmodel.PDResources;
import org.junit.Assert;
import org.junit.Test;
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
            Assert.assertTrue("Marker:" + i +  " should have placeholder for fill", data.contains(PLACEHOLDER_FILL));
            Assert.assertTrue("Marker: " + i +  " should have placeholder for stroke", data.contains(PLACEHOLDER_STROKE));
        }
        doc.close();
    }

    @Test
    public void getStyles() throws Exception  {
        JSONObject style = WFSLayerOptions.getDefaultOskariStyle();

        PDDocument doc = new PDDocument();
        PDPrintStyle point = StyleUtil.getPointStyle(style, doc);
        Assert.assertNotNull("Point should have icon", point.getIcon());
        doc.close();

        PDPrintStyle line = StyleUtil.getLineStyle(style);
        Assert.assertTrue("Line should have color", line.hasLineColor());

        PDPrintStyle polygon = StyleUtil.getPolygonStyle(style, null);
        Assert.assertTrue("Polygon should have fill color", polygon.hasFillColor());
        Assert.assertTrue("Polygon should have line color", polygon.hasLineColor());

        JSONObject area = style.getJSONObject("fill").getJSONObject("area");
        for (int i = 0; i <= 3; i++) {
            area.put("pattern", i);
            PDPrintStyle pattern = StyleUtil.getPolygonStyle(style, new PDResources());
            Assert.assertTrue("Pattern:" + i +  " should have fill color", pattern.hasFillColor());
        }
    }
}
