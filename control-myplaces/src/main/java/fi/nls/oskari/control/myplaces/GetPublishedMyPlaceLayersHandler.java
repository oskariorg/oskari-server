package fi.nls.oskari.control.myplaces;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

@OskariActionRoute("GetPublishedMyPlaceLayers")
public class GetPublishedMyPlaceLayersHandler extends ActionHandler {

    private MyPlacesService myPlaceService = null;
    private final String PARAM_USERS = "USERS";

    public void init() {
        myPlaceService = OskariComponentManager.getComponentOfType(MyPlacesService.class);
    }
    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final String users = params.getHttpParam(PARAM_USERS); 
        final List<MyPlaceCategory> myPlaces = new ArrayList<MyPlaceCategory>();
        if (users == null) {
            myPlaces.addAll(myPlaceService.getCategories());
        } else {
            // TODO: return subset containing only listed users
        }
        final JSONArray rootArray = MyPlaceLayersUtils.generateMyPlaceJSON(myPlaces);
        ResponseHelper.writeResponse(params, rootArray);
    }
    
}
