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

import fi.nls.oskari.control.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
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


    public void setOskariMapLayerGroupService(final OskariMapLayerGroupService service) {
        oskariMapLayerGroupService = service;
    }

    public void setOskariLayerService(final OskariLayerService service) {
        oskariLayerService = service;
    }

    @Override
    public void init() {
        // setup service if it hasn't been initialized
        if (oskariMapLayerGroupService == null) {
            setOskariMapLayerGroupService(new OskariMapLayerGroupServiceIbatisImpl());
        }
        // setup service if it hasn't been initialized
        if (oskariLayerService == null) {
            setOskariLayerService(new OskariLayerServiceIbatisImpl());
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
                if (order.has(KEY_OLD_PARENT)) {
                    oskariLayerService.updateGroup(id, order.getInt(KEY_OLD_PARENT), parentID);
                }
                oskariLayerService.updateOrder(id, parentID, i);
            } else {
                MaplayerGroup currentGroup = oskariMapLayerGroupService.find(id);
                currentGroup.setOrderNumber(i);
                oskariMapLayerGroupService.updateOrder(currentGroup);
                if (order.has(KEY_OLD_PARENT)) {
                    oskariMapLayerGroupService.updateGroupParent(id, parentID);
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
            MaplayerGroup currentGroup = oskariMapLayerGroupService.find(groupId);
            currentGroup.setOrderNumber(i);
            oskariMapLayerGroupService.updateOrder(currentGroup);
        }
    }
}
