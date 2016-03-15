package fi.nls.oskari.control.layer;

import fi.mml.portti.domain.permissions.WFSLayerPermissionsStore;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

@OskariActionRoute("DeleteLayerIds")
public class DeleteLayerIds extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(DeleteLayerIds.class);
    
    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        String jsessionid = params.getRequest().getSession().getId();
        
        // remove cache
        log.debug("deleting session:", jsessionid);
        WFSLayerPermissionsStore.destroy(jsessionid);
    }
}
