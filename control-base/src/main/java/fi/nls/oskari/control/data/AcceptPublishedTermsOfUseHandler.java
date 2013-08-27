package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.map.publish.service.PublishTermsOfUseService;
import fi.nls.oskari.map.publish.service.PublishTermsOfUseServiceIbatisImpl;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("AcceptPublishedTermsOfUse")
public class AcceptPublishedTermsOfUseHandler  extends ActionHandler {

    private PublishTermsOfUseService service = new PublishTermsOfUseServiceIbatisImpl();

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        if(!params.getUser().isGuest()) {
            final int id = service.setUserAgreed(params.getUser().getId());
            ResponseHelper.writeResponse(params, id != -1);            
        }
        else {
            ResponseHelper.writeResponse(params, false);
        }
    }

}