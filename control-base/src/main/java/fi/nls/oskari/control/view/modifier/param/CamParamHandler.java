package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import fi.nls.oskari.view.modifier.ParamHandler;
import org.json.JSONException;
import org.json.JSONObject;

@OskariViewModifier("cam")
public class CamParamHandler extends ParamHandler {

    private static final String KEY_CAMERA = "camera";
    private static final String KEY_ORIENTATION = "orientation";
    private static final String KEY_HEADING = "heading";
    private static final String KEY_PITCH = "pitch";
    private static final String KEY_ROLL = "roll";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_ALTITUDE = "altitude";
    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        if(params.getParamValue() == null) {
            return false;
        }

        final String[] camProps = params.getParamValue().split("_");
        if (camProps.length != 6) {
            return false;
        }

        JSONObject location = new JSONObject();
        JSONHelper.putValue(location, KEY_X, camProps[0]);
        JSONHelper.putValue(location, KEY_Y, camProps[1]);
        JSONHelper.putValue(location, KEY_ALTITUDE, camProps[2]);

        JSONObject orientation = new JSONObject();
        JSONHelper.putValue(orientation, KEY_HEADING, camProps[3]);
        JSONHelper.putValue(orientation, KEY_PITCH, camProps[4]);
        JSONHelper.putValue(orientation, KEY_ROLL, camProps[5]);

        JSONObject camera = new JSONObject();
        JSONHelper.putValue(camera, KEY_LOCATION, location);
        JSONHelper.putValue(camera, KEY_ORIENTATION, orientation);

        try {
            final JSONObject mapfullState = getBundleState(params.getConfig(), BUNDLE_MAPFULL);
            mapfullState.put(KEY_CAMERA, camera);
        } catch (JSONException je) {
            throw new ModifierException("Could not apply cam parameters");
        }
        return false;
    }
}
