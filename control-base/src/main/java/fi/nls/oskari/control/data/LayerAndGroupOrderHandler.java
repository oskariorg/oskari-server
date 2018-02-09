package fi.nls.oskari.control.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.ResponseHelper;

/**
 * CRUD for layer and group order handling. Methods require admin user.
 */
@OskariActionRoute("LayerAndGroupOrder")
public class LayerAndGroupOrderHandler extends RestActionHandler {
	private static Logger log = LogFactory.getLogger(LayerAndGroupOrderHandler.class);
	
    private static final String KEY_PARENT = "parent";
    private static final String KEY_ORDERS = "orders";
    private static final String KEY_TYPE = "type";
    private static final String KEY_ID = "id";
    private static final String KEY_OLD_PARENT = "oldParent";
    private static final String TYPE_LAYER = "layer";
	
	private OskariMapLayerGroupService oskariMapLayerGroupService;
	private OskariLayerService oskariLayerService;
	
	/**
	 * Variable to keep track of the new order of the groups.
	 */
	private HashMap<Integer, Integer> newGroupOrderMap = new HashMap<>();
	/**
	 * Variable to keep track of the new order of the layers.
	 */
	private HashMap<Integer, Integer> newLayerOrderMap = new HashMap<>(); 

    public void setOskariMapLayerGroupService(final OskariMapLayerGroupService service) {
        oskariMapLayerGroupService = service;
    }
    
    public void setOskariLayerService(final OskariLayerService service) {
    	oskariLayerService = service;
    }

    @Override
    public void init() {
        // setup service if it hasn't been initialized
        if(oskariMapLayerGroupService == null) {
            setOskariMapLayerGroupService(new OskariMapLayerGroupServiceIbatisImpl());
        }
        // setup service if it hasn't been initialized
        if(oskariLayerService == null) {
        	setOskariLayerService(new OskariLayerServiceIbatisImpl());
        }
        if(newGroupOrderMap == null) {
        	newGroupOrderMap = new HashMap<>();
        }
        if(newLayerOrderMap == null) {
        	newLayerOrderMap = new HashMap<>();
        }
    }

    /**
     * Handles updating the order and group of the given node i.e. layer or group.
     * @param params
     * @throws ActionException
     */
    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        checkForAdminPermission(params);
        log.debug("Updating layer/group order");
        JSONObject orderJSON = getOrderJSON(params.getRequest());


		try {
			int parentID = orderJSON.getInt(KEY_PARENT);
			JSONArray orders = orderJSON.getJSONArray(KEY_ORDERS);

			// change main groups orders
			if(parentID == -1){
				for(int i=0;i<orders.length();i++){
					JSONObject order = orders.getJSONObject(i);
					int groupId = order.getInt(KEY_ID);
					MaplayerGroup currentGroup = oskariMapLayerGroupService.find(groupId);
					currentGroup.setOrderNumber(i);
					oskariMapLayerGroupService.updateOrder(currentGroup);
				}
			} else {
				for(int i=0;i<orders.length();i++) {
					JSONObject order = orders.getJSONObject(i);
					String type = order.getString(KEY_TYPE);
					int id = order.getInt(KEY_ID);
					if(TYPE_LAYER.equals(type)) {
                        if(order.has(KEY_OLD_PARENT)) {
                            oskariLayerService.updateGroup(id, order.getInt(KEY_OLD_PARENT), parentID);
                        }
					    oskariLayerService.updateOrder(id, parentID, i);
					} else {
						MaplayerGroup currentGroup = oskariMapLayerGroupService.find(id);
						currentGroup.setOrderNumber(i);
						oskariMapLayerGroupService.updateOrder(currentGroup);
						if(order.has(KEY_OLD_PARENT)){
						    oskariMapLayerGroupService.updateGroupParent(id,parentID);
                        }
					}
				}
			}



        } catch (JSONException e) {
        	log.warn(e);
            throw new ActionException("Failed to read request!");
        }

        ResponseHelper.writeResponse(params, orderJSON);
    }

    
    /**
     * Read JSON from request
     * @param req
     * @return
     * @throws ActionException
     */
    protected JSONObject getOrderJSON(HttpServletRequest req) throws ActionException {
        try (InputStream in = req.getInputStream()) {
            final byte[] json = IOHelper.readBytes(in);
            final String jsonString = new String(json, StandardCharsets.UTF_8);
            return new JSONObject(jsonString);
        } catch (IOException e) {
            log.warn(e);
            throw new ActionException("Failed to read request!");
        } catch (IllegalArgumentException | JSONException e) {
            log.warn(e);
            throw new ActionException("Invalid request!");
        }
    }
    /**
     * Commonly used with
     * @param params
     * @throws ActionException
     */
    private void checkForAdminPermission(ActionParameters params) throws ActionException {
        if(!params.getUser().isAdmin()) {
            throw new ActionDeniedException("Session expired");
        }
    }
}
