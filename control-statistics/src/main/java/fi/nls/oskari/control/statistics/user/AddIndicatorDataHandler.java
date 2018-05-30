package fi.nls.oskari.control.statistics.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.control.statistics.StatisticsHelper;
import fi.nls.oskari.control.statistics.data.IndicatorValue;
import fi.nls.oskari.control.statistics.data.IndicatorValueFloat;
import fi.nls.oskari.control.statistics.data.StatisticalIndicator;
import fi.nls.oskari.control.statistics.data.StatisticalIndicatorDataModel;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePluginManager;
import fi.nls.oskari.control.statistics.plugins.db.DatasourceLayer;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;
import org.oskari.statistics.user.StatisticalIndicatorService;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

@OskariActionRoute("AddIndicatorData")
public class AddIndicatorDataHandler extends ActionHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String PARAM_DATA = "data";

    private StatisticalIndicatorService indicatorService;

    @Override
    public void init() {
        super.init();
        if (indicatorService == null) {
            indicatorService = OskariComponentManager.getComponentOfType(StatisticalIndicatorService.class);
        }
    }

    public void handleAction(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();

        final int datasourceId = params.getRequiredParamInt(StatisticsHelper.PARAM_DATASOURCE_ID);
        final String indicatorId = params.getRequiredParam(ActionConstants.PARAM_ID);
        final int regionsetId = params.getRequiredParamInt(StatisticsHelper.PARAM_REGIONSET);
        final String dataJSON = params.getRequiredParam(PARAM_DATA);

        StatisticalDatasourcePlugin datasource = StatisticalDatasourcePluginManager.getInstance().getPlugin(datasourceId);
        if(datasource == null) {
            throw new ActionParamsException("Invalid datasource:" + datasourceId);
        }
        if(!datasource.canModify(params.getUser())) {
            throw new ActionDeniedException("User doesn't have permission to modify datasource:" + datasourceId);
        }
        StatisticalIndicator indicator = datasource.getIndicator(params.getUser(), indicatorId);
        if(indicator == null) {
            // indicator removed or is not owned by this user
            throw new ActionParamsException("Requested invalid indicator:" + indicatorId);
        }

        // This is just an addition to the existing model, NOT the whole model
        JSONObject selectors = params.getHttpParamAsJSON(StatisticsHelper.PARAM_SELECTORS);
        StatisticalIndicatorDataModel model = StatisticsHelper.getIndicatorDataModel(selectors);
        indicator.setDataModel(model);

        // add the layer if it doesn't exist yet (only if linked to the datasource)
        DatasourceLayer layer = datasource.getSource().getLayers().stream()
                .filter(x -> x.getMaplayerId() == regionsetId)
                .findFirst()
                .orElseThrow(() -> new ActionParamsException("Invalid regionset: " + regionsetId));
        if(indicator.getLayer(layer.getMaplayerId()) == null) {
            indicator.addLayer(layer);
        }

        Map<String, IndicatorValue> data = parseIndicatorData(dataJSON);

        try {
            datasource.saveIndicatorData(indicator, regionsetId, data, params.getUser());
        } catch (Exception ex) {
            throw new ActionException("Couldn't save data", ex);
        }

        if (datasource.canCache()) {
            // TODO: flush/update caches (also metadata and listing)
            // Not an issue for now since user indicators are not cached and they are are the
            //  only ones that can be added/edited/removed
            StatisticsHelper.flushDataFromCache(datasourceId, indicatorId, regionsetId, selectors);
        }
        JSONObject jobj = new JSONObject();
        JSONHelper.putValue(jobj, "id", indicatorId);
        ResponseHelper.writeResponse(params,jobj);
    }

    protected Map<String, IndicatorValue> parseIndicatorData(String data) throws ActionException {
        try {
            // read json as map with strings and doubles
            Map<String, Double> values = MAPPER.readValue(data, new TypeReference<Map<String, Double>>(){});
            // map doubles to IndicatorValueFloat
            return values.entrySet().stream()
                    .collect(Collectors
                            .toMap( e -> e.getKey(),
                                    e -> new IndicatorValueFloat(e.getValue())));
        } catch (IOException e) {
            throw new ActionParamsException("Invalid parameter value for key: " + PARAM_DATA + " - expected JSON object");
        }
    }
}
