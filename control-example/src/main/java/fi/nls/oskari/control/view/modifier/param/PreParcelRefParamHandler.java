package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONObject;

/**
 * Parcel application
 * Set preparcel (part parcel) code for parcel bundle state
 */
@OskariViewModifier("preparcel.initRef")
public class PreParcelRefParamHandler extends ParamHandler {

    private Logger log = LogFactory.getLogger(PreParcelRefParamHandler.class);

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        log.debug("PreParcelRefParamHandler.handleParam");
        final JSONObject bundleState = getBundleState(params.getConfig(), "parcel");
        JSONHelper.putValue(bundleState, "initPreParcelRef", params.getParamValue());
        return true;
    }
}
