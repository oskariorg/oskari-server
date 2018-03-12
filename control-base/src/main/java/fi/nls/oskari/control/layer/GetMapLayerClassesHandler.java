package fi.nls.oskari.control.layer;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.service.util.ServiceFactory;

import java.util.List;

/**
 * Lists all map layer groups
 */
@OskariActionRoute("GetMapLayerGroups")
public class GetMapLayerClassesHandler extends ActionHandler {

    private DataProviderService service = ServiceFactory.getDataProviderService();
    private static final Logger log = LogFactory.getLogger(GetMapLayerClassesHandler.class);

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        try {
            final List<DataProvider> groups = service.findAll();
            final JSONArray list = new JSONArray();
            for(DataProvider group : groups) {
                list.put(group.getAsJSON());
            }
            final JSONObject result = new JSONObject();
            JSONHelper.putValue(result, "organization", list);
            ResponseHelper.writeResponse(params, result);
        } catch (Exception e) {
            throw new ActionException("Layer group listing failed", e );
        }
    }
}
