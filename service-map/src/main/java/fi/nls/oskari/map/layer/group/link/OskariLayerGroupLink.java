package fi.nls.oskari.map.layer.group.link;

import java.util.HashMap;
import java.util.Map;

public class OskariLayerGroupLink {

    public static final int DEFAULT_ORDER_NUMBER = 1000000;

    private int layerId;
    private int groupId;
    private int orderNumber;

    public OskariLayerGroupLink() {
        this(-1, -1);
    }

    public OskariLayerGroupLink(int layerId, int groupId) {
        this(layerId, groupId, DEFAULT_ORDER_NUMBER);
    }

    public OskariLayerGroupLink(int layerId, int groupId, int orderNumber) {
        this.setLayerId(layerId);
        this.setGroupId(groupId);
        this.setOrderNumber(orderNumber);
    }

    public int getLayerId() {
        return layerId;
    }

    public void setLayerId(int layerId) {
        this.layerId = layerId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

}
