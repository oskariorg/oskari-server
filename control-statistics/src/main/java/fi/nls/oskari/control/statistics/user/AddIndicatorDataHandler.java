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

    private StatisticalIndicatorService indicatorService;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void init() {
        super.init();
        if (indicatorService == null) {
            indicatorService = OskariComponentManager.getComponentOfType(StatisticalIndicatorService.class);
        }
    }

    private static String PARAM_DATA = "data";

    public void handleAction(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();

        int datasourceId = params.getRequiredParamInt(StatisticsHelper.PARAM_DATASOURCE_ID);
        StatisticalDatasourcePlugin datasource = StatisticalDatasourcePluginManager.getInstance().getPlugin(datasourceId);
        if(datasource == null) {
            throw new ActionParamsException("Invalid datasource:" + datasourceId);
        }
        if(!datasource.canModify(params.getUser())) {
            throw new ActionDeniedException("User doesn't have permission to modify datasource:" + datasourceId);
        }
        String id = params.getRequiredParam(ActionConstants.PARAM_ID);
        StatisticalIndicator indicator = datasource.getIndicator(params.getUser(), id);
        if(indicator == null) {
            // indicator removed or is not owned by this user
            throw new ActionParamsException("Requested invalid indicator:" + id);
        }

        // This is just an addition to the existing model, NOT the whole model
        JSONObject selectors = params.getHttpParamAsJSON(StatisticsHelper.PARAM_SELECTORS);
        StatisticalIndicatorDataModel model = StatisticsHelper.getIndicatorDataModel(selectors);

        // add the layer if it doesn't exist yet (only if linked to the datasource)
        int regionsetId = params.getRequiredParamInt(StatisticsHelper.PARAM_REGIONSET);
        DatasourceLayer layer = datasource.getSource().getLayers().stream()
                .filter(x -> x.getMaplayerId() == regionsetId)
                .findFirst()
                .orElseThrow(() -> new ActionParamsException("Invalid regionset: " + regionsetId));
        if(indicator.getLayer(layer.getMaplayerId()) == null) {
            indicator.addLayer(layer);
        }
        try {
            indicator.setDataModel(model);
            Map<String, IndicatorValue> data = parseIndicatorData(params.getHttpParam(PARAM_DATA));
            if (data != null) {
                datasource.saveIndicatorData(indicator, regionsetId, data, params.getUser());
            }
            StatisticsHelper.flushDataFromCache(datasourceId, id, regionsetId, selectors);
        } catch (Exception ex) {
            throw new ActionException("Couldn't save data", ex);
        }

        JSONObject jobj = new JSONObject();
        JSONHelper.putValue(jobj, "id", id);
        ResponseHelper.writeResponse(params,jobj);
    }

    protected Map<String, IndicatorValue> parseIndicatorData(String data) throws ActionException {
        if (data == null) {
            return null;
        }
        try {
            // read json as map with strings and doubles
            Map<String, Double> values = MAPPER.readValue(data, new TypeReference<Map<String, Double>>(){});
            // map doubles to IndicatorValueFloat
            return values.entrySet().stream()
                    .collect(Collectors
                            .toMap( e -> e.getKey(),
                                    e -> new IndicatorValueFloat(e.getValue())));
        } catch (IOException e) {
            throw new ActionParamsException("Invalid parameter value for key: " + StatisticsHelper.PARAM_SELECTORS + " - expected JSON object");
        }
    }
}
