package fi.nls.oskari.control.layer;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.log.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import fi.mml.portti.domain.permissions.WFSLayerPermissionsStore;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.util.ConversionHelper;

@OskariActionRoute("GetLayerIds")
public class GetLayerIds extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetLayerIds.class);
    
    private static final String LAYER_IDS = "layerIds";
    private static final String ID = "id";
    
    
    private static PermissionsService permissionsService = new PermissionsServiceIbatisImpl();
    
    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        
        String result = null;

        String jsessionid = params.getRequest().getSession().getId();

        // check cache
	    boolean cache = ConversionHelper.getBoolean(params.getHttpParam("no-cache"), false);
	    if(cache) {
	    	result = WFSLayerPermissionsStore.getCache(jsessionid);
	    	log.debug("permissions cache:", result);
	        if(result != null) {
	        	ResponseHelper.writeResponse(params, result);
	        	return;
	        }
	    }
        
        List<Map<String,Object>> listOfLayers = permissionsService.getListOfMaplayerIdsForViewPermissionByUser(params.getUser(), true);
        ObjectMapper mapper = new ObjectMapper();
        
        try {
            List<Integer> idList = new ArrayList<Integer>();
            for (Map<String,Object> entry : listOfLayers) {
                idList.add(Integer.parseInt(String.valueOf(entry.get(ID))));
            }
            Map<String,List<Integer>> layerIds = new HashMap<String,List<Integer>>();
            layerIds.put(LAYER_IDS, idList);
            result = mapper.writeValueAsString(layerIds);
            
            // put to cache
            log.debug("saving session:", jsessionid);
	        WFSLayerPermissionsStore permissions = WFSLayerPermissionsStore.setJSON(result);
	        permissions.save(jsessionid);
            
        } catch (JsonGenerationException e) {
            log.error(e);
            result = "{\"error\":\"" +e.toString() +"\"}";
        } catch (JsonMappingException e) {
            log.error(e);
            result = "{\"error\":\"" +e.toString() +"\"}";
        } catch (IOException e) {
            log.error(e);
            result = "{\"error\":\"" +e.toString() +"\"}";
        }
       
        ResponseHelper.writeResponse(params, result);
    }
}
