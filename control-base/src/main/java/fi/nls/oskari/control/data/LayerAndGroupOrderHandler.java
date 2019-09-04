package fi.nls.oskari.control.data;

import fi.nls.oskari.control.*;
import fi.nls.oskari.utils.AuditLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLink;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkService;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkServiceMybatisImpl;

/**
 * CRUD for layer and group order handling. Methods require admin user.
 */
@OskariActionRoute("LayerAndGroupOrder")
public class LayerAndGroupOrderHandler extends RestActionHandler {

    private static final Logger log = LogFactory.getLogger(LayerAndGroupOrderHandler.class);

    private static final String KEY_PARENT = "parent";
    private static final String KEY_ORDERS = "orders";
    private static final String KEY_TYPE = "type";
    private static final String KEY_ID = "id";
    private static final String KEY_OLD_PARENT = "oldParent";
    private static final String TYPE_LAYER = "layer";

    private OskariMapLayerGroupService groupService;
    private OskariLayerGroupLinkService linkService;

    public void setGroupService(OskariMapLayerGroupService groupService) {
        this.groupService = groupService;
    }

    public void setLinkService(final OskariLayerGroupLinkService linkService) {
        this.linkService = linkService;
    }

    @Override
    public void init() {
        // setup service if it hasn't been initialized
        if (groupService == null) {
            setGroupService(new OskariMapLayerGroupServiceIbatisImpl());
        }
        if (linkService == null) {
            setLinkService(new OskariLayerGroupLinkServiceMybatisImpl());
        }
    }

    /**
     * Handles updating the order and group of the given node i.e. layer or group.
     *
     * @param params
     * @throws ActionException
     */
    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireAdminUser();
        log.debug("Updating layer/group order");
        JSONObject orderJSON = params.getPayLoadJSON();
        try {
            int parentID = orderJSON.getInt(KEY_PARENT);
            JSONArray orders = orderJSON.getJSONArray(KEY_ORDERS);
            // change main groups orders
            if (parentID == -1) {
                updateGroupOrder(orders);
            } else {
                updateLayerAndGroupOrders(orders, parentID);
            }
            AuditLog.user(params.getClientIp(), params.getUser())
                    .withMsg("Changed order")
                    .updated(AuditLog.ResourceType.MAPLAYER_GROUP);
        } catch (JSONException e) {
            log.warn(e);
            throw new ActionParamsException("Cannot save orders!");
        }
    }

    /**
     * Update group and layer orders
     *
     * @param orders
     */
    protected void updateLayerAndGroupOrders(final JSONArray orders, final int parentID) throws JSONException {
        for (int i = 0; i < orders.length(); i++) {
            JSONObject order = orders.getJSONObject(i);
            String type = order.getString(KEY_TYPE);
            int id = order.getInt(KEY_ID);
            if (TYPE_LAYER.equals(type)) {
                int groupId = order.has(KEY_OLD_PARENT) ? order.getInt(KEY_OLD_PARENT) : parentID;
                OskariLayerGroupLink old = new OskariLayerGroupLink(id, groupId);
                OskariLayerGroupLink link = new OskariLayerGroupLink(id, parentID, i);
                linkService.replace(old, link);
            } else {
                MaplayerGroup currentGroup = groupService.find(id);
                currentGroup.setOrderNumber(i);
                groupService.updateOrder(currentGroup);
                if (order.has(KEY_OLD_PARENT)) {
                    groupService.updateGroupParent(id, parentID);
                }
            }
        }
    }

    /**
     * Update main group orders
     *
     * @param orders
     * @throws JSONException
     */
    protected void updateGroupOrder(final JSONArray orders) throws JSONException {
        for (int i = 0; i < orders.length(); i++) {
            JSONObject order = orders.getJSONObject(i);
            int groupId = order.getInt(KEY_ID);
            MaplayerGroup currentGroup = groupService.find(groupId);
            currentGroup.setOrderNumber(i);
            groupService.updateOrder(currentGroup);
        }
    }
}
