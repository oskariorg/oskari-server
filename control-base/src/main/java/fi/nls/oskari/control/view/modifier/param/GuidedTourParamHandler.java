package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.view.modifier.ParamHandler;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONObject;

@OskariViewModifier("showIntro")
public class GuidedTourParamHandler extends ParamHandler {

    @Override
    public int getPriority() {
        return 1;
    }

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        if ("false".equals(params.getParamValue())){
            final JSONObject state = getBundleState(params.getConfig(), BUNDLE_GUIDEDTOUR);
            JSONHelper.putValue(state, "showIntro", false);
            return true;
        }
       return false;
    }
    
}
