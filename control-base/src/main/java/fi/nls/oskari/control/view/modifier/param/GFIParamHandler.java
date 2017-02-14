package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.view.modifier.ParamHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;

@OskariViewModifier("showGetFeatureInfo")
public class GFIParamHandler extends ParamHandler {

    private static final Logger log = LogFactory.getLogger(GFIParamHandler.class);
    private static final String PARAM_ADDRESS = "showGetFeatureInfo";

    
    public boolean handleParam(final ModifierParams params) throws ModifierException {
        if(params.getParamValue() == null) {
            return false;
        }


        // TODO: not implemented yet, gfi need to add to bundle state?
        // handled in index.js currently
        // maybe should add infobox/gfiplugin if not present?
        return true;
    }
    
}
