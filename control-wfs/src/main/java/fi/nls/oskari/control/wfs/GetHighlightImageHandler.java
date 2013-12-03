package fi.nls.oskari.control.wfs;

import fi.nls.oskari.annotation.OskariActionRoute;

/**
 * Additional mapping for GetImageHandler due to WFS backend internal routing.
 * Returns a highlight image to be shown on top of the actual WFS layer tiles.
 */
@OskariActionRoute("GET_HIGHLIGHT_WFS_FEATURE_IMAGE")
public class GetHighlightImageHandler extends GetImageHandler {

}
