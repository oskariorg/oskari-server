package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import fi.nls.oskari.log.Logger;

@OskariViewModifier("isCenterMarker")
public class CenterMarkerParamHandler extends ParamHandler {

    private static final Logger log = LogFactory.getLogger(CenterMarkerParamHandler.class);
    private static final String PARAM_IS_CENTER_MARKER = "isCenterMarker";

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        if(params.getParamValue() == null) {
            return false;
        }

        // TODO: not implemented yet, need to add to bundle state?
        // [jira / PORTTISK-691]
        return true;
    }
    
}
