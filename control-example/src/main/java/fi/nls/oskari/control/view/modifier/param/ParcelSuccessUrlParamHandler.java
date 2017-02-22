package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.view.modifier.ParamHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONObject;

/**
 * Parcel application
 * Set success url for parcel bundle config
 */
@OskariViewModifier("parcel.successUrl")
public class ParcelSuccessUrlParamHandler extends ParamHandler {

    private Logger log = LogFactory.getLogger(ParcelSuccessUrlParamHandler.class);

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        log.debug("ParcelSuccessUrlParamHandler.handleParam");
        final JSONObject bundleState = getBundleConfig(params.getConfig(), "parcel");
        JSONHelper.putValue(bundleState, "successUrl", params.getParamValue());
        return true;
    }
}
