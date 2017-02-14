package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.view.modifier.ParamHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONObject;

@OskariViewModifier("coord")
public class CoordinateParamHandler extends ParamHandler {

    @Override
    public int getPriority() {
        return 10;
    }

    private static final Logger log = LogFactory.getLogger(CoordinateParamHandler.class);

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        if(params.getParamValue() == null) {
            return false;
        }

        final String[] coords = parseParam(params.getParamValue());
        if (coords.length == 2) {
            final JSONObject state = getBundleState(params.getConfig(), BUNDLE_MAPFULL);
            try {
                state.put(KEY_EAST, coords[0]);
                state.put(KEY_NORTH, coords[1]);
                return true;
            } catch (Exception ex) {
                throw new ModifierException("Could not set coordinates from URL param.");
            }
        }
        return false;
    }
    
    public static String[] parseParam(final String value) {
        if (value.indexOf('_') > 0) {
            return value.split("_");
        } 
        return value.split(" ");
    }
    
}
