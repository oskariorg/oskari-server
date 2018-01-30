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
     * @param params
     * @throws ActionException
     */
    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        checkForAdminPermission(params);
        log.debug("Updating layer/group order");
        JSONObject orderJSON = getOrderJSON(params.getRequest());
        try {
        	//Get the dragged node id. Can be either a layer id or a group id.
            int nodeId = orderJSON.getInt(KEY_NODE_ID);
            //Get the new index of the dragged node.
            int nodeIndex = orderJSON.getInt(KEY_NODE_INDEX);
            //Variable for the nex index.
            int nextIndex = nodeIndex++;
            //The node has an old group.
            int oldGroupId = orderJSON.getInt(KEY_OLD_GROUP_ID);
            //The node has a new group.
            int targetGroupId = orderJSON.getInt(KEY_TARGET_GROUP_ID);
            boolean changeGroup = false;
            if(oldGroupId != targetGroupId) {
            	changeGroup = true;
            }
            OskariLayer nodeLayer = null;
            MaplayerGroup nodeGroup = null;
            //Check if the node we dragged was either a layer or a group.
            if("layer".equals(orderJSON.getString(KEY_NODE_TYPE))) {
            	nodeLayer = oskariLayerService.find(nodeId);
            } else {
            	nodeGroup = oskariMapLayerGroupService.find(nodeId);
            }
            //FIXME: Reassure that the layers and groups are handled in order.
            List<MaplayerGroup> groups = oskariMapLayerGroupService.findByParentId(targetGroupId);
            for(MaplayerGroup group : groups) {
            	log.debug(group);
            	int oldGroupOrderNumber = group.getOrderNumber();
            	if(oldGroupOrderNumber <= nodeIndex) {
            		group.setOrderNumber(nextIndex);
            		nextIndex++;
            	}
            }
            List<OskariLayer> layers = oskariLayerService.findAllByGroupId(targetGroupId);
            for(OskariLayer layer : layers) {
            	log.debug(layer);
//            	if(layer.getOrderNumber() <= nodeIndex) {
//            		layer.setOrderNumber(nextIndex);
//            		nextIndex++;
//            	}
            }
            //TODO: TALLENNA 
            if(nodeLayer != null) {
            	nodeLayer.setOrderNumber(nodeIndex);
            	//nodeLayer.setGroup(targetGroupId);
            	//oskariLayerService.updateOrder(nodeLayer);
            } else if(nodeGroup != null) {
            	nodeGroup.setOrderNumber(nodeIndex);
            	//nodeGroup.setGroup(targetGroupId);
            	//oskariMapLayerGroupService.updateOrder(nodeGroup);
            }
        } catch (JSONException e) {
        	log.warn(e);
            throw new ActionException("Failed to read request!");
        }
//        final int id = params.getRequiredParamInt(PARAM_ID);
//        final MaplayerGroup theme = oskariMapLayerGroupService.find(id);
//        populateFromRequest(params, theme);
//        oskariMapLayerGroupService.update(theme);
//        ResponseHelper.writeResponse(params, theme.getAsJSON());
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
