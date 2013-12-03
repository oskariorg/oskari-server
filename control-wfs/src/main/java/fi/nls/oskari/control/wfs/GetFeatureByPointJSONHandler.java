package fi.nls.oskari.control.wfs;

import fi.nls.oskari.annotation.OskariActionRoute;

/**
 * Additional mapping for GetGridJSONHandler due to WFS backend internal routing
 */
@OskariActionRoute("GET_HIGHLIGHT_WFS_FEATURE_IMAGE_BY_POINT")
public class GetFeatureByPointJSONHandler extends GetGridJSONHandler {

}
