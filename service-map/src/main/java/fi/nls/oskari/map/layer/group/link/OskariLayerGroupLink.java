package fi.nls.oskari.map.layer.group.link;

public class OskariLayerGroupLink {

    public static final int DEFAULT_ORDER_NUMBER = 1000000;

    private final int layerId;
    private final int groupId;
    private final int orderNumber;

    public OskariLayerGroupLink(int layerId, int groupId) {
        this(layerId, groupId, DEFAULT_ORDER_NUMBER);
    }

    public OskariLayerGroupLink(int layerId, int groupId, int orderNumber) {
        this.layerId = layerId;
        this.groupId = groupId;
        this.orderNumber = orderNumber;
    }

    public int getLayerId() {
        return layerId;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

}
