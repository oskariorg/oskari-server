package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import fi.nls.oskari.log.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Alternate (legacy) parameter for showMarker. Both do the same thing.
 */
@OskariViewModifier("isCenterMarker")
public class CenterMarkerParamHandler extends ShowMarkerParamHandler {
// dummy class for mapping showMarker to isCenterMarker
}
