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
 * Set the free text property for parcel bundle config
 */
@OskariViewModifier("parcel.printContent")
public class ParcelPrintContentlParamHandler extends ParamHandler {

    private Logger log = LogFactory.getLogger(ParcelPrintContentlParamHandler.class);

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        log.debug("ParcelPrintContentParamHandler.handleParam");
        final JSONObject bundleState = getBundleConfig(params.getConfig(), "parcel");
        JSONHelper.putValue(bundleState, "printContent", params.getParamValue());
        return true;
    }
}
