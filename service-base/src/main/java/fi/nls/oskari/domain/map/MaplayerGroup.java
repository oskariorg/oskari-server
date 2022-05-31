package fi.nls.oskari.domain.map;

import fi.nls.oskari.util.JSONHelper;

import org.json.JSONObject;

public class MaplayerGroup extends JSONLocalizedName {
    private int id;
    private int parentId;
    private boolean selectable;
    private int orderNumber = -1;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public JSONObject getAsJSON() {
        final JSONObject me = new JSONObject();
        if (id > 0) {
            JSONHelper.putValue(me, "id", id);
        }

        JSONHelper.putValue(me, "locale", getLocale());
        JSONHelper.putValue(me, "selectable", this.isSelectable());
        JSONHelper.putValue(me, "parentId", this.getParentId());
        JSONHelper.putValue(me, "orderNumber", this.getOrderNumber());
        return me;
    }

    public JSONObject getAsJSON(String language) {
        final JSONObject me = new JSONObject();
        if (id > 0) {
            JSONHelper.putValue(me, "id", id);
        }
        JSONHelper.putValue(me, "name", getName(language));
        JSONHelper.putValue(me, "desc", getLocalizedValue(language, LOCALE_DESCRIPTION));
        JSONHelper.putValue(me, "selectable", this.isSelectable());
        JSONHelper.putValue(me, "parentId", this.getParentId());
        JSONHelper.putValue(me, "orderNumber", this.getOrderNumber());
        return me;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }
}
