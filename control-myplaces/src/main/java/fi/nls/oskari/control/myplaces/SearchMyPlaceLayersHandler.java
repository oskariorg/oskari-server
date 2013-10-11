package fi.nls.oskari.control.myplaces;

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

@OskariActionRoute("FreeFindFromMyPlaceLayers")
public class SearchMyPlaceLayersHandler extends ActionHandler {

    private static final String PARAM_SEARCH_KEY = "searchKey";
    private static final MyPlacesService myPlaceService = new MyPlacesServiceIbatisImpl();

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
       
        final String searchKey = params.getHttpParam(PARAM_SEARCH_KEY, "");
        final List<MyPlaceCategory> myPlaces = myPlaceService.getMyPlaceLayersBySearchKey(searchKey);
        final JSONArray rootArray = MyPlaceLayersUtils.generateMyPlaceJSON(myPlaces);
        ResponseHelper.writeResponse(params, rootArray.toString());
    }

}
