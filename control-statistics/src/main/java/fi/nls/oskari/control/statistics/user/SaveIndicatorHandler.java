package fi.nls.oskari.control.statistics.user;

import fi.nls.oskari.control.*;
import fi.nls.oskari.control.statistics.StatisticsHelper;
import fi.nls.oskari.control.statistics.data.*;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePluginManager;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.log.AuditLog;
import org.json.JSONException;
import org.oskari.statistics.user.StatisticalIndicatorService;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

import java.util.NoSuchElementException;

@OskariActionRoute("SaveIndicator")
public class SaveIndicatorHandler extends RestActionHandler {

    public static final String PARAM_NAME = "name";
    public static final String PARAM_DESCRIPTION = "desc";
    public static final String PARAM_SOURCE = "source";

    private StatisticalIndicatorService indicatorService;

    @Override
    public void init() {
        super.init();
        if (indicatorService == null) {
            indicatorService = OskariComponentManager.getComponentOfType(StatisticalIndicatorService.class);
        }
    }

    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();

        int datasourceId = params.getRequiredParamInt(StatisticsHelper.PARAM_DATASOURCE_ID);
        StatisticalDatasourcePlugin datasource = StatisticalDatasourcePluginManager.getInstance().getPlugin(datasourceId);
        if(datasource == null) {
            throw new ActionParamsException("Invalid datasource:" + datasourceId);
        }
        if(!datasource.canModify(params.getUser())) {
            throw new ActionDeniedException("User doesn't have permission to modify datasource:" + datasourceId);
        }
        String id = params.getHttpParam(ActionConstants.PARAM_ID);
        StatisticalIndicator indicator = parseIndicator(params, datasource);
        StatisticalIndicator savedIndicator;
        try {
            savedIndicator = datasource.saveIndicator(indicator, params.getUser());
        } catch (Exception ex) {
            throw new ActionException("Couldn't save indicator", ex);
        }

        AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("id", id)
                .withParam("ds", datasourceId)
                .withParam("name", indicator.getName(PropertyUtil.getDefaultLanguage()))
                .added(AuditLog.ResourceType.STATISTICAL_DATA);
        try {
            ResponseHelper.writeResponse(params, StatisticsHelper.toJSON(savedIndicator));
        } catch (JSONException shouldNeverHappen) {
            ResponseHelper.writeResponse(params, JSONHelper.createJSONObject("id", id));
        }
    }

    private StatisticalIndicator parseIndicator(ActionParameters params, StatisticalDatasourcePlugin datasource)
            throws ActionException {

        StatisticalIndicator ind;
        String id = params.getHttpParam(ActionConstants.PARAM_ID);
        if (id != null) {
            ind = datasource.getIndicator(params.getUser(), id);
            if(ind == null) {
                // indicator removed or is not owned by this user
                throw new ActionParamsException("Requested invalid indicator:" + id);
            }
        } else {
            ind = new StatisticalIndicator();
        }
        // always set to true, but doesn't really matter?
        // TODO: should this be the toggle if guest users can see the indicator on embedded maps?
        ind.setPublic(true);

        String language = params.getLocale().getLanguage();
        ind.addName(language, params.getHttpParam(PARAM_NAME, getExistingName(ind, language)));
        ind.addDescription(language, params.getHttpParam(PARAM_DESCRIPTION, ind.getDescription(language)));
        ind.addSource(language, params.getHttpParam(PARAM_SOURCE, ind.getSource(language)));
        return ind;
    }

    private String getExistingName(StatisticalIndicator ind, String language) {
        try {
            return ind.getName(language);
        } catch (NoSuchElementException e) {
            // handle exception from getting existing name since new indicators don't have an existing one
            return null;
        }
    }
}
