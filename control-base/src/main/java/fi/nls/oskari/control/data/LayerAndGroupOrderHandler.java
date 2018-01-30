package fi.nls.oskari.control.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
	
    private static final String KEY_NODE_ID = "nodeId";
    private static final String KEY_NODE_INDEX = "nodeIndex";
    private static final String KEY_NODE_TYPE = "type";
    private static final String KEY_OLD_GROUP_ID = "oldGroupId";
    private static final String KEY_TARGET_GROUP_ID = "targetGroupId";
	
	private OskariMapLayerGroupService oskariMapLayerGroupService;
	private OskariLayerService oskariLayerService;

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
    }
    
    /**
     * Handles updating the order and group of the given node ie. layer or group.
     * FIXME: Remove sysouts!
     * @param params
     * @throws ActionException
     */
    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        checkForAdminPermission(params);
        log.info("Updating layer/group order");
        System.out.println("Updating layer/group order");
        JSONObject orderJSON = getOrderJSON(params.getRequest());
        try {
        	//Get the dragged node id. Can be either a layer id or a group id.
            int nodeId = orderJSON.getInt(KEY_NODE_ID);
            //Get the new index of the dragged node.
            int nodeIndex = orderJSON.getInt(KEY_NODE_INDEX);
            //Variable for the next index.
            int nextIndex = nodeIndex;
            //The node has an old group.
            int oldGroupId = orderJSON.getInt(KEY_OLD_GROUP_ID);
            //The node has a new group.
            int targetGroupId = orderJSON.getInt(KEY_TARGET_GROUP_ID);
            //The node has a type
            String type = orderJSON.getString(KEY_NODE_TYPE);
            //Boolean flag to inform us later whether we want to change the group of the node.
            boolean changeGroup = false;
            if(oldGroupId != targetGroupId) {
            	changeGroup = true;
            }
            OskariLayer nodeLayer = null;
            MaplayerGroup nodeGroup = null;
            //Check if the node we dragged was either a layer or a group.
            if("layer".equals(type)) {
            	System.out.println("LAYER");
            	nodeLayer = oskariLayerService.find(nodeId);
            } else {
            	System.out.println("GROUP");
            	nodeGroup = oskariMapLayerGroupService.find(nodeId);
            }
            //Get the layers and groups under the target group
            List<MaplayerGroup> groups = oskariMapLayerGroupService.findByParentId(targetGroupId);
            List<Integer> layers = oskariMapLayerGroupService.findMaplayersByGroup(targetGroupId);
            //The largest index possible is actually the combined size of layers and groups.
            final int largestIndex = groups.size() + layers.size();
            //Update ordering for all the layers and groups under the target group.
            while(nextIndex <= largestIndex) {
            	//This way we can assure that the layers and groups are handled in correct order.
            	OskariLayer layer = this.getLayerByOrderNumber(layers, nextIndex, nodeId, type);
            	MaplayerGroup group = this.getGroupByOrderNumber(groups, nextIndex, nodeId, type);
            	++nextIndex;
            	System.out.println("Index running wheee: "+nextIndex);
            	if(layer != null) {
            		layer.setOrderNumber(nextIndex);
            		System.out.println("oskariLayerService.updateOrder("+layer.getId() + ", " +layer.getOrderNumber()+")");
                	//oskariLayerService.updateOrder(layer);
            	} else if(group != null) {
            		group.setOrderNumber(nextIndex);
            		System.out.println("oskariMapLayerGroupService.updateOrder("+group.getId() + ", " +group.getOrderNumber()+")");
                	//oskariMapLayerGroupService.updateOrder(group);
            	}
            }
            if(nodeLayer != null) {
            	nodeLayer.setOrderNumber(nodeIndex);
            	System.out.println("oskariLayerService.updateOrder("+nodeLayer.getId() + ", " +nodeLayer.getOrderNumber()+")");
            	//oskariLayerService.updateOrder(nodeLayer);
            	if(changeGroup) {
            		System.out.println("oskariLayerService.updateGroup("+nodeLayer.getId() + ", " +oldGroupId+", "+targetGroupId+")");
            		//oskariLayerService.updateGroup(nodeLayer.getId(), oldGroupId, targetGroupId);
            	}
            } else if(nodeGroup != null) {
            	nodeGroup.setOrderNumber(nodeIndex);
            	System.out.println("oskariLayerService.updateOrder("+nodeGroup.getId() + ", " +nodeGroup.getOrderNumber()+")");
            	//oskariMapLayerGroupService.updateOrder(nodeGroup);
            	if(changeGroup) {
            		System.out.println("oskariMapLayerGroupService.updateGroupParent("+nodeGroup.getId() + ", "+targetGroupId+")");
            		//oskariMapLayerGroupService.updateGroupParent(nodeGroup.getId(), targetGroupId);
            	}
            }
        } catch (JSONException e) {
        	log.warn(e);
            throw new ActionException("Failed to read request!");
        }
        ResponseHelper.writeResponse(params, "{success: true}");
    }
    
    /**
     * Get the possible layer whose order number we are going to overwrite. Null if not found.
     * @param layerIds Layers to search
     * @param orderNumber The new index to be overwritten
     * @return OskariLayer matching the parameters
     */
    protected OskariLayer getLayerByOrderNumber(List<Integer> layerIds, int orderNumber, int nodeId, String type) {
    	OskariLayer retLayer = null;
    	for(Integer layerId : layerIds) {
    		OskariLayer layer = oskariLayerService.find(layerId);
    		//Check if the given order number matches this iteration's layer order number.
			//If we are switching layer's order we want to assure we don't get the actual layer here.
    		if(layer.getOrderNumber() == null ||
    					(
    						layer.getOrderNumber() != null && 
    						layer.getOrderNumber().compareTo(orderNumber) == 0 &&
    						(!"layer".equals(type) || ("layer".equals(type) && layer.getId() != nodeId))
						)
    				) {
				retLayer = layer;
    			break;
    		}
    	}
    	return retLayer;
    }
    
    /**
     * Get the possible group whose order number we are going to overwrite. Null if not found.
     * @param groups Groups to search
     * @param orderNumber The new index to be overwritten
     * @return MaplayerGroup matching the parameters
     */
    protected MaplayerGroup getGroupByOrderNumber(List<MaplayerGroup> groups, int orderNumber, int nodeId, String type) {
    	MaplayerGroup retGroup = null;
    	for(MaplayerGroup group : groups) {
    		//Check if the given order number matches this iteration's group order number.
    		//Also if we are switching group's order we want to assure we don't get the actual group here.
    		if(group.getOrderNumber() == null ||
    					(
    						group.getOrderNumber() != null && 
    						group.getOrderNumber().compareTo(orderNumber) == 0 &&
    						(!"group".equals(type) || ("group".equals(type) && group.getId() != nodeId))
						)
    				) {
    			retGroup = group;
    			break;
    		}
    	}
    	return retGroup;
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
            final JSONObject orderJSON = new JSONObject(jsonString);
            return orderJSON;
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
