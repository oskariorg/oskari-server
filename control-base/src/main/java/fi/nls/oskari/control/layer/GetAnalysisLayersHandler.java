package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.map.analysis.service.AnalysisDataService;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

/**
 * Get WMS map layers
 */
@OskariActionRoute("GetAnalysisLayers")
public class GetAnalysisLayersHandler extends ActionHandler {

    final static String LANGUAGE_ATTRIBUTE = "lang";

    private AnalysisDataService analysisDataService = new AnalysisDataService();


    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        try {
            final String lang = params.getHttpParam(LANGUAGE_ATTRIBUTE, params
                    .getLocale().getLanguage());

            final JSONObject layers = analysisDataService.getListOfAllAnalysisLayers(
                    params.getUser().getUuid(), lang);

            ResponseHelper.writeResponse(params, layers);
        } catch (Exception e) {
            throw new ActionException(
                    "Couldn't request Analysis data service - get analysis layers", e);
        }
    }


}
