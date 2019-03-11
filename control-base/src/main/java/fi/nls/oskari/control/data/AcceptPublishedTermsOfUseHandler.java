package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.map.publish.service.PublishTermsOfUseService;
import fi.nls.oskari.map.publish.service.PublishTermsOfUseServiceMybatisImpl;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("AcceptPublishedTermsOfUse")
public class AcceptPublishedTermsOfUseHandler  extends RestActionHandler {

    private PublishTermsOfUseService service = new PublishTermsOfUseServiceMybatisImpl();

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        if(!params.getUser().isGuest()) {
            ResponseHelper.writeResponse(params, service.setUserAgreed(params.getUser().getId()));
        }
        else {
            ResponseHelper.writeResponse(params, false);
        }
    }

}