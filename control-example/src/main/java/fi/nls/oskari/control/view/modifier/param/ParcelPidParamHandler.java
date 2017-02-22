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
 * Set picture id for parcel bundle config
 */
@OskariViewModifier("parcel.pid")
public class ParcelPidParamHandler extends ParamHandler {

    private Logger log = LogFactory.getLogger(ParcelPidParamHandler.class);

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        log.debug("ParcelPidParamHandler.handleParam");
        final JSONObject bundleState = getBundleConfig(params.getConfig(), "parcel");
        JSONHelper.putValue(bundleState, "pid", params.getParamValue());
        return true;
    }
}
