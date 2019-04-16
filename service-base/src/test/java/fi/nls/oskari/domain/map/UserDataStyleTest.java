package fi.nls.oskari.domain.map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import fi.nls.oskari.util.JSONHelper;

public class UserDataStyleTest {
    private UserDataStyle getStyle (){
        UserDataStyle style = new UserDataStyle();
        style.setId(1);
        style.setDot_shape("1");
        style.setDot_color("#010101");
        style.setDot_size(3);

        style.setStroke_width(1);
        style.setStroke_color("");
        style.setStroke_dasharray("5 2");
        style.setStroke_linecap("butt");
        style.setStroke_linejoin("mitre");

        style.setBorder_color("#FFFFFF");
        style.setBorder_dasharray("");
        style.setBorder_linejoin("mitre");
        style.setBorder_width(1);
        style.setFill_color(null); //no fill color
        style.setFill_pattern(-1);
        return style;
    }
    @Test
    public void testJSONparsing(){
        UserDataStyle style = getStyle();
        JSONObject json = style.parseUserLayerStyle2JSON();
        UserDataStyle style2 = new UserDataStyle();
        try {
            style2.populateFromJSON(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertTrue(JSONHelper.isEqual(json, style2.parseUserLayerStyle2JSON()));
    }
    @Test
    public void testOskariJSONParsing() {
        UserDataStyle style = getStyle();
        JSONObject json = style.parseUserLayerStyleToOskariJSON();
        UserDataStyle style2 = new UserDataStyle();
        try {
            assertEquals("dash", json.getJSONObject("stroke").getString("lineDash"));
            style2.populateFromOskariJSON(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertEquals("5 2", style2.getStroke_dasharray());
        assertTrue(JSONHelper.isEqual(json, style2.parseUserLayerStyleToOskariJSON()));
    }
}
