package fi.nls.oskari.domain.map.userlayer;

import static org.junit.Assert.assertTrue;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import fi.nls.oskari.util.JSONHelper;

public class UserLayerStyleTest {
    
    private UserLayerStyle getStyle (){
        UserLayerStyle style = new UserLayerStyle();
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
        UserLayerStyle style = getStyle();
        JSONObject json = style.parseUserLayerStyle2JSON();
        UserLayerStyle style2 = new UserLayerStyle();
        try {
            style2.populateFromJSON(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assertTrue(JSONHelper.isEqual(json, style2.parseUserLayerStyle2JSON()));
    }
}