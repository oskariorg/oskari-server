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

import java.util.List;

@OskariActionRoute("FreeFindFromMyPlaceLayers")
public class SearchMyPlaceLayersHandler extends ActionHandler {

    private static final String PARAM_SEARCH_KEY = "searchKey";
    private MyPlacesService myPlaceService = null;

    public void init() {
        myPlaceService = OskariComponentManager.getComponentOfType(MyPlacesService.class);
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
       
        final String searchKey = params.getHttpParam(PARAM_SEARCH_KEY, "");
        final List<MyPlaceCategory> myPlaces = myPlaceService.getMyPlaceLayersBySearchKey(searchKey);
        final JSONArray rootArray = MyPlaceLayersUtils.generateMyPlaceJSON(myPlaces);
        ResponseHelper.writeResponse(params, rootArray.toString());
    }

}
