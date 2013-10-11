package fi.nls.oskari.control.myplaces;

import java.util.ArrayList;
import java.util.List;

import fi.nls.oskari.annotation.OskariActionRoute;
import org.json.JSONArray;
import fi.mml.map.mapwindow.service.db.MyPlacesService;
import fi.mml.map.mapwindow.service.db.MyPlacesServiceIbatisImpl;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("GetPublishedMyPlaceLayers")
public class GetPublishedMyPlaceLayersHandler extends ActionHandler {

    private static final MyPlacesService myPlaceService = new MyPlacesServiceIbatisImpl();
    private final String PARAM_USERS = "USERS";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        final String users = params.getHttpParam(PARAM_USERS); 
        final List<MyPlaceCategory> myPlaces = new ArrayList<MyPlaceCategory>();
        if (users == null) {
            myPlaces.addAll(myPlaceService.findAll());
        } else {
            // TODO: return subset containing only listed users
        }
        final JSONArray rootArray = MyPlaceLayersUtils.generateMyPlaceJSON(myPlaces);
        ResponseHelper.writeResponse(params, rootArray);
    }
    
}
