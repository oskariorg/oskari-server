package fi.nls.oskari.control.statistics.user;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.control.statistics.StatisticsHelper;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePluginManager;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.utils.AuditLog;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.statistics.user.StatisticalIndicatorService;

/**
 * Deletes indicator that the user has previously saved.
 * Only allows deletion of the users own indicators
 */
@OskariActionRoute("DeleteIndicator")
public class DeleteIndicatorHandler extends RestActionHandler {
    private static final Logger LOG = LogFactory.getLogger(DeleteIndicatorHandler.class);
    private StatisticalIndicatorService indicatorService;

    @Override
    public void init() {
        super.init();
        if (indicatorService == null) {
            indicatorService = OskariComponentManager.getComponentOfType(StatisticalIndicatorService.class);
        }
    }

    public void handlePost(ActionParameters params) throws ActionException {
        // user indicators are user content so deleting one requires to be logged in
        params.requireLoggedInUser();

        int datasourceId = params.getRequiredParamInt(StatisticsHelper.PARAM_DATASOURCE_ID);
        int id = params.getRequiredParamInt(ActionConstants.PARAM_ID);
        StatisticalIndicator ind = indicatorService.findById(id, params.getUser().getId());
        if(ind == null) {
            // or might not be the owner
            throw new ActionDeniedException("Unknown indicator/not the owner: " + id );
        }
        JSONObject selectors = params.getHttpParamAsJSON(StatisticsHelper.PARAM_SELECTORS);
        if(selectors != null) {
            // if selectors present, regionset is also required
            int regionset = params.getRequiredParamInt("regionset");
            int year = parseYearSelector(selectors);
            indicatorService.deleteIndicatorData(id, regionset, year);
            LOG.info("Deleted indicator data for indicator:", id, "year:", year, "regionset:", regionset);
            StatisticsHelper.flushDataFromCache(datasourceId, Integer.toString(id), regionset, selectors);

            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", id)
                    .withParam("ds", datasourceId)
                    .withMsg("Data removed")
                    .updated(AuditLog.ResourceType.STATISTICAL_DATA);
        } else {
            // remove the whole indicator
            removeIndicator(id, params.getUser().getId());
            LOG.info("Deleted indicator", id);
            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", id)
                    .withParam("ds", datasourceId)
                    .deleted(AuditLog.ResourceType.STATISTICAL_DATA);
        }

        StatisticalDatasourcePlugin datasource = StatisticalDatasourcePluginManager.getInstance().getPlugin(datasourceId);
        if (datasource != null && datasource.canCache()) {
            // TODO: flush/update caches
            // Not an issue for now since user indicators are not cached and they are are the
            //  only ones that can be added/edited/removed
        }
        try {
            ResponseHelper.writeResponse(params, StatisticsHelper.toJSON(ind));
        } catch (JSONException ex) {
            ResponseHelper.writeResponse(params, JSONHelper.createJSONObject("id", id));
        }
    }

    private int parseYearSelector(JSONObject selectors) throws ActionException {
        try {
            return selectors.getInt("year");
        } catch (JSONException ex) {
            throw new ActionParamsException("Year was not part of the selectors parameter");
        }
    }

    private void removeIndicator(int id, long userId) throws ActionException {
        if (!indicatorService.delete(id, userId)) {
            // remove the whole indicator
            throw new ActionParamsException("Indicator wasn't removed: " +  + id);
        }
    }
}
