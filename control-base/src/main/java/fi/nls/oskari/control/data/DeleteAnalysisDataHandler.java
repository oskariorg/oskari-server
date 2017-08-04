package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.map.analysis.service.AnalysisDbService;
import fi.nls.oskari.map.analysis.service.AnalysisDbServiceMybatisImpl;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

/**
 * Deletes analysis data if it belongs to current user.
 * Expects to get analysis id as http parameter "id".
 */
@OskariActionRoute("DeleteAnalysisData")
public class DeleteAnalysisDataHandler extends ActionHandler {

    private final static String PARAM_ID = "id";
    //private final static Logger log = LogFactory.getLogger(DeleteAnalysisDataHandler.class);

    private AnalysisDbService analysisDataService = null;

    public void setAnalysisDataService(final AnalysisDbService service) {
        analysisDataService = service;
    }

    @Override
    public void init() {
        super.init();
        if(analysisDataService == null) {
            setAnalysisDataService(new AnalysisDbServiceMybatisImpl());
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        if(params.getUser().isGuest()) {
            throw new ActionDeniedException("Session expired");
        }
        final long id = ConversionHelper.getLong(params.getHttpParam(PARAM_ID), -1);
        if(id == -1) {
            throw new ActionParamsException("Parameter missing or non-numeric: " + PARAM_ID + "=" + params.getHttpParam(PARAM_ID));
        }

        final Analysis analysis = analysisDataService.getAnalysisById(id);
        if(analysis == null) {
            throw new ActionParamsException("Analysis id didn't match any analysis: " + id);
        }
        if(!analysis.isOwnedBy(params.getUser().getUuid())) {
            throw new ActionDeniedException("Analysis belongs to another user");
        }

        try {
            // remove analysis
            analysisDataService.deleteAnalysis(analysis);
            // write static response to notify success {"result" : "success"}
            ResponseHelper.writeResponse(params, JSONHelper.createJSONObject("result", "success"));
        } catch (ServiceException ex) {
            throw new ActionException("Error deleting analysis", ex);
        }
    }
}