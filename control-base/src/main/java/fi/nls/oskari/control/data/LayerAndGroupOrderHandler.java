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
    private static final String NODE_TYPE_LAYER = "layer";
    private static final String NODE_TYPE_GROUP = "group";
	
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
        newGroupOrderMap = new HashMap<>();
    	newLayerOrderMap = new HashMap<>();
        try {
        	//Get the dragged node id. Can be either a layer id or a group id.
            int nodeId = orderJSON.getInt(KEY_NODE_ID);
            //Get the new index of the dragged node.
            int nodeIndex = orderJSON.getInt(KEY_NODE_INDEX);
            int nodePrevIndex = -1; 
            //Variable for the next index.
            int nextIndex = 0;
            //The node's old group.
            int oldGroupId = orderJSON.getInt(KEY_OLD_GROUP_ID);
            //The node's new group.
            int targetGroupId = orderJSON.getInt(KEY_TARGET_GROUP_ID);
            //The node's type
            String type = orderJSON.getString(KEY_NODE_TYPE);
            //Boolean flag to inform us later whether we want to change the group of the node.
            boolean changeGroup = false;
            if(oldGroupId != targetGroupId) {
            	changeGroup = true;
            }
            OskariLayer nodeLayer = null;
            MaplayerGroup nodeGroup = null;
            //Check if the node we dragged was either a layer or a group.
            if(NODE_TYPE_LAYER.equals(type)) {
            	nodeLayer = oskariLayerService.find(nodeId);
            	nodePrevIndex = (nodeLayer.getOrderNumber() == null) ? nodePrevIndex : nodeLayer.getOrderNumber();
            } else {
            	//Bypass cache here so that we can get the real previous index from the database.
            	oskariMapLayerGroupService.flushCache();
            	nodeGroup = oskariMapLayerGroupService.find(nodeId);
            	nodePrevIndex = (nodeGroup.getOrderNumber() == null) ? nodePrevIndex : nodeGroup.getOrderNumber();
            }
            //Get the layers and groups under the target group.
            List<MaplayerGroup> groups = oskariMapLayerGroupService.findByParentId(targetGroupId);
            List<Integer> layers = oskariMapLayerGroupService.findMaplayersByGroup(targetGroupId);
            //The largest index possible is actually the combined size of layers and groups.
            final int largestIndex = getLargestOrderNumber(layers, groups);
            //Update ordering for all the layers and groups under the target group.
            while(nextIndex <= largestIndex) {
            	getOrderingForOtherLayersAndGroups(targetGroupId, nextIndex, nodeId, nodeIndex, nodePrevIndex, type);
            	++nextIndex;
            }
            updateOrdering(nodeLayer, nodeGroup, nodeIndex, changeGroup, oldGroupId, targetGroupId);
        } catch (JSONException e) {
        	log.warn(e);
            throw new ActionException("Failed to read request!");
        }
        ResponseHelper.writeResponse(params, orderJSON);
    }
    /**
     * Updates the ordering and group if necessary for the given nodeLayer or nodeGroup depending which one is null. And also updates the ordering of the found layers and/or groups under the new targetGroupId.
     * @param nodeLayer
     * @param nodeGroup
     * @param nodeIndex
     * @param changeGroup
     * @param oldGroupId
     * @param targetGroupId
     */
    protected void updateOrdering(OskariLayer nodeLayer, MaplayerGroup nodeGroup, int nodeIndex, boolean changeGroup, int oldGroupId, int targetGroupId) {
    	Iterator<Entry<Integer, Integer>> newLayerOrderMapIterator = newLayerOrderMap.entrySet().iterator();
        while (newLayerOrderMapIterator.hasNext()) {
            Map.Entry<Integer, Integer> pair = newLayerOrderMapIterator.next();
            OskariLayer layer = oskariLayerService.find(pair.getKey());
            layer.setOrderNumber(pair.getValue());
            oskariLayerService.updateOrder(layer);
        }
        Iterator<Entry<Integer, Integer>> newGroupOrderMapIterator = newGroupOrderMap.entrySet().iterator();
        while (newGroupOrderMapIterator.hasNext()) {
            Map.Entry<Integer, Integer> pair = newGroupOrderMapIterator.next();
            MaplayerGroup group = oskariMapLayerGroupService.find(pair.getKey());
            group.setOrderNumber(pair.getValue());
            oskariMapLayerGroupService.updateOrder(group);
        }
        if(nodeLayer != null) {
        	nodeLayer.setOrderNumber(nodeIndex);
        	oskariLayerService.updateOrder(nodeLayer);
        	if(changeGroup) {
        		oskariLayerService.updateGroup(nodeLayer.getId(), oldGroupId, targetGroupId);
        	}
        } else if(nodeGroup != null) {
        	nodeGroup.setOrderNumber(nodeIndex);
        	oskariMapLayerGroupService.updateOrder(nodeGroup);
        	if(changeGroup) {
        		oskariMapLayerGroupService.updateGroupParent(nodeGroup.getId(), targetGroupId);
        	}
        }
    }
    /**
     * Helper method to get ordering for the other layers and/or groups under targetGroupId group. 
     * @param targetGroupId
     * @param nextIndex
     * @param nodeId
     * @param nodeIndex
     * @param nodePrevIndex
     * @param type
     */
    protected void getOrderingForOtherLayersAndGroups(int targetGroupId, int nextIndex, int nodeId, int nodeIndex, int nodePrevIndex, String type) {
    	//This way we can assure that the layers and groups are handled in correct order.
    	OskariLayer layer = this.getLayerByOrderNumber(targetGroupId, nextIndex, nodeId, type);
    	MaplayerGroup group = this.getGroupByOrderNumber(targetGroupId, nextIndex, nodeId, type);
    	if(layer != null) {
    		findIfLayerOrderingShouldChange(layer, nodeIndex, nodePrevIndex);
    	} else if(group != null) {
    		findIfGroupOrderingShouldChange(group, nodeIndex, nodePrevIndex);
    	}
    }
    /**
     * Helper method to find out if layer order number should be changed.
     * @param layer
     * @param nodeIndex
     * @param nodePrevIndex
     */
    protected void findIfLayerOrderingShouldChange(OskariLayer layer, int nodeIndex, int nodePrevIndex) {
    	int layerIndex = (layer.getOrderNumber() == null) ? -1 : layer.getOrderNumber();
		if(layerIndex == nodeIndex) {
			if(layerIndex < nodePrevIndex) {
				newLayerOrderMap.put(layer.getId(), layerIndex+1);
			} else if(layerIndex > nodePrevIndex) {
				newLayerOrderMap.put(layer.getId(), layerIndex-1);
			}
		} else if(layerIndex > nodeIndex && layerIndex <= nodePrevIndex) {
			newLayerOrderMap.put(layer.getId(), layerIndex+1);
		} else if(layerIndex < nodeIndex && layerIndex >= nodePrevIndex) {
			newLayerOrderMap.put(layer.getId(), layerIndex-1);
		}
    }
    /**
     * Helper method to find out if group order number shoul be changed.
     * @param group
     * @param nodeIndex
     * @param nodePrevIndex
     */
    protected void findIfGroupOrderingShouldChange(MaplayerGroup group, int nodeIndex, int nodePrevIndex) {
    	int groupIndex = (group.getOrderNumber() == null) ? -1 : group.getOrderNumber();
		if(groupIndex == nodeIndex) {
			if(groupIndex < nodePrevIndex) {
				newGroupOrderMap.put(group.getId(), groupIndex+1);
			} else if(groupIndex > nodePrevIndex) {
    			newGroupOrderMap.put(group.getId(), groupIndex-1);
			}
		} else if(groupIndex > nodeIndex && groupIndex <= nodePrevIndex) {
			 newGroupOrderMap.put(group.getId(), groupIndex+1);
		} else if(groupIndex < nodeIndex && groupIndex >= nodePrevIndex) {
			newGroupOrderMap.put(group.getId(), groupIndex-1);
		}
    }
    /**
     * Find out the largest order number given the layers and groups.
     * @param layers the layers to search for the largest order number from
     * @param groups the groups to search for the largest order number from
     * @return int largest order number
     */
    protected int getLargestOrderNumber(List<Integer> layers, List<MaplayerGroup> groups) {
    	int retOrderNumber = 0;
    	for(Integer layerId : layers) {
    		OskariLayer layer = oskariLayerService.find(layerId);
    		int layerIndex = (layer.getOrderNumber() == null) ? -1 : layer.getOrderNumber(); 
    		if(layerIndex > retOrderNumber) {
    			retOrderNumber = layerIndex;
    		}
    	}
    	for(MaplayerGroup group : groups) {
    		int groupIndex = (group.getOrderNumber() == null) ? -1 : group.getOrderNumber(); 
    		if(groupIndex > retOrderNumber) {
    			retOrderNumber = groupIndex;
    		}
    	}
    	return retOrderNumber;
    }
    /**
     * Get the possible layer whose order number we are going to overwrite. Null if not found.
     * @param layerIds Layers to search
     * @param orderNumber The new index to be overwritten
     * @return OskariLayer matching the parameters
     */
    protected OskariLayer getLayerByOrderNumber(Integer targetGroupId, int orderNumber, int nodeId, String type) {
    	List<Integer> layers = oskariMapLayerGroupService.findMaplayersByGroup(targetGroupId);
    	OskariLayer retLayer = null;
    	for(Integer layerId : layers) {
    		OskariLayer layer = oskariLayerService.find(layerId);
    		//Check if the given order number matches this iteration's layer order number.
			//If we are switching layer's order we want to assure we don't get the actual layer here.
    		if((!NODE_TYPE_LAYER.equals(type) || (layer.getId() != nodeId)) &&
    				(
						layer.getOrderNumber() == null ||
    					(
    						layer.getOrderNumber() != null && 
    						(
								layer.getOrderNumber().compareTo(orderNumber) == 0
							)
						)
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
    protected MaplayerGroup getGroupByOrderNumber(int targetGroupId, int orderNumber, int nodeId, String type) {
    	List<MaplayerGroup> groups = oskariMapLayerGroupService.findByParentId(targetGroupId);
    	MaplayerGroup retGroup = null;
    	for(MaplayerGroup group : groups) {
    		//Check if the given order number matches this iteration's group order number.
    		//Also if we are switching group's order we want to assure we don't get the actual group here.
    		if((!NODE_TYPE_GROUP.equals(type) || (group.getId() != nodeId)) &&
    				(
    					group.getOrderNumber() == null ||
    					(
    						group.getOrderNumber() != null && 
    						(
								group.getOrderNumber().compareTo(orderNumber) == 0
							)
						)
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
