package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.map.publish.domain.TermsOfUse;
import fi.nls.oskari.map.publish.service.PublishTermsOfUseService;
import fi.nls.oskari.map.publish.service.PublishTermsOfUseServiceIbatisImpl;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("HasAcceptedPublishedTermsOfUse")
public class HasAcceptedPublishedTermsOfUseHandler extends ActionHandler {

    private PublishTermsOfUseService service = new PublishTermsOfUseServiceIbatisImpl();
    
    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final TermsOfUse tou = service.findByUserId(params.getUser().getId());
        ResponseHelper.writeResponse(params, (tou != null && tou.isAgreed()));
    }
}
