package fi.nls.oskari.control.statistics.plugins;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;

/**
 * This interface gives the data for one indicator to the frontend for showing it on the map and on the table.
 * 
 * - action_route=GetIndicatorData&plugin_id=plugin_id&indicator=indicator_id&selectors=URL_ENCODED_JSON
 * 
 * For example SotkaNET requires selectors for year and gender. This means that selectors parameter content
 * could be for example: selectors=%7B%22gender%22%3A%20%22male%22%2C%20%22year%22%3A%20%222005%22%7D
 * 
 * Response is in JSON, and contains the indicator data.
 */
@OskariActionRoute("GetIndicatorData")
public class GetIndicatorHandler extends ActionHandler {

    @Override
    public void handleAction(ActionParameters ap) throws ActionException {
    }

}
