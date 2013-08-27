package fi.nls.oskari.control.wfs;

import fi.mml.portti.service.ogc.handler.FlowModel;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("QUERY_FIND_RAW_DATA_TO_TABLE")
public class GetGridJSONHandler extends FlowModelHandler {

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        try {

            final String actionKey = params.getRequest().getParameter(KEY_ACTION);

            final FlowModel flowModel = createFlowModelFlowModel(params);
            // FIXME: tear down this internal processActions mapping
            processActions(actionKey, flowModel);

            ResponseHelper.writeResponse(params, flowModel.getRootJson());
        } catch (Exception e) {
            // Do not let real cause to be shown to end user
            throw new ActionException("Failed to create JSON", e);
        }
    }
}
