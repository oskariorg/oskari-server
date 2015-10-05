package fi.nls.oskari.control.myplaces;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyPlaceLayersUtils {
    
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String LAYERS = "layers";
    
    
    public static JSONArray generateMyPlaceJSON(List<MyPlaceCategory> myPlaces) throws ActionException {
        final JSONArray rootArray = new JSONArray();
        
        final Map<String, JSONObject> userNames = new HashMap<String, JSONObject>();
        
        for(final MyPlaceCategory mp: myPlaces) {
            
            try {
                
                JSONObject userNode;
                if (userNames.containsKey(mp.getPublisher_name())) {
                    userNode = userNames.get(mp.getPublisher_name());
                } else {
                    userNode = new JSONObject();
                    userNode.put(ID, mp.getUuid());
                    userNode.put(NAME, mp.getPublisher_name());
                    rootArray.put(userNode);
                    userNames.put(mp.getPublisher_name(), userNode);
                }
                
                JSONArray layersArray;
                if (userNode.has(LAYERS)) {
                    layersArray = (JSONArray)userNode.get(LAYERS);
                } else {
                    layersArray = new JSONArray();
                    userNode.put("layers", layersArray);
                }
                
                final JSONObject layer = new JSONObject();
                layer.put(ID, mp.getId());
                layer.put(NAME, mp.getCategory_name());
                
                layersArray.put(layer);
                    
            } catch (JSONException e) {
                throw new ActionException("Unable to create published myplaces layer listing", e);
            }
        }
        return rootArray;
    }
}
