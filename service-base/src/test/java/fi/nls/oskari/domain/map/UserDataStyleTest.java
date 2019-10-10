package fi.nls.oskari.domain.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import fi.nls.oskari.util.JSONHelper;

public class UserDataStyleTest {

    private UserDataStyle getStyle(){
        UserDataStyle style = new UserDataStyle();

        style.setId(15);

        style.setDot_shape("6");
        style.setDot_color("#010101");
        style.setDot_size(3);

        style.setStroke_width(5);
        style.setStroke_color("#ff0000");
        style.setStroke_dasharray("5 2");
        style.setStroke_linecap("butt");
        style.setStroke_linejoin("round");

        style.setBorder_color("#FFFFFF");
        style.setBorder_dasharray("");
        style.setBorder_linejoin("mitre");
        style.setBorder_width(1);
        style.setFill_color(null); //no fill color

        style.setFont("Foobar");
        style.setText_align("baz");
        style.setText_fill_color("qux");
        style.setText_label("first one");
        style.setText_offset_x(100);
        style.setText_offset_y(-500);
        style.setText_label_property("yes");
        style.setText_label_property(new String[]{ "no" });
        style.setText_stroke_color("strong");
        style.setText_stroke_width(9000);

        return style;
    }
    @Test
    public void testOskariJSONParsing() throws JSONException {
        UserDataStyle original = getStyle();
        JSONObject originalAsOskariStyle = original.parseUserLayerStyleToOskariJSON();
        UserDataStyle another = new UserDataStyle();
        another.populateFromOskariJSON(originalAsOskariStyle);
        another.setId(original.getId());
        assertEquals(original, another);
        JSONObject anotherAsOskariStyle = another.parseUserLayerStyleToOskariJSON();
        assertTrue(JSONHelper.isEqual(originalAsOskariStyle, anotherAsOskariStyle));
    }
}
