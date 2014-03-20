package fi.nls.oskari.control.metadata;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 11.3.2014
 * Time: 14:00
 * To change this template use File | Settings | File Templates.
 */
public class MetadataField {
/*
    TYPE("type", true), // type
    SERVICE_TYPE("serviceType", true), // serviceType
    SERVICE_NAME("serviceName", "Title"), // Title
    ORGANIZATION("organization", "orgName"), // orgName
    COVERAGE("coverage", new CoverageHandler()),
    INSPIRE_THEME("inspireTheme", new InspireThemeHandler()),
    KEYWORD("keyword"), // keyword
    TOPIC("topic", "topicCategory"); // topicCategory
*/
    private String name = null;
    private boolean multi = false;
    private String property = null;
    private String filter = null;
    private String filterOp = null;
    private MetadataFieldHandler handler = null;
    private JSONArray shownIf = null;

    public static final String RESULT_KEY_ORGANIZATION = "organization";


    private MetadataField(String name, final String property) {
        this(name, new MetadataFieldHandler());
        this.property = property;
    }

    private MetadataField(String name) {
        this(name, false);
    }

    public MetadataField(String name, boolean isMulti) {
        this(name, isMulti, null);
    }

    private MetadataField(String name, final MetadataFieldHandler handler) {
        this(name, false, handler);
    }
    private MetadataField(String name, boolean isMulti, MetadataFieldHandler handler) {
        this.name = name;
        this.multi = isMulti;

        if(handler == null) {
            handler = new MetadataFieldHandler();
        }
        handler.setMetadataField(this);
        this.handler = handler;
    }

    /**
     * Single or multiple values
     * @return
     */
    public boolean isMulti() {
        return multi;
    }

    /**
     * Field name used by client
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * The property name used when getting options from CSW getDomain
     * @return
     */
    public String getProperty() {
        if(property == null) {
            return name;
        }
        return property;
    }

    /**
     * The name used when constructing CSW query filter
     * @return
     */
    public String getFilter() {
        if(filter == null) {
            return name;
        }
        return filter;
    }
    public void setFilter(String param) {
        filter = param;
    }

    public MetadataFieldHandler getHandler() {
        return handler;
    }

    public JSONArray getShownIf() {
        return shownIf;
    }

    public void setShownIf(String shownIf) {
        if(shownIf != null) {
            // TODO: maybe try catch errors or make JSONHelper create empty array on errors...
            this.shownIf = JSONHelper.createJSONArray(shownIf);
        }
    }

    /**
     * If null -> should default to  like operation
     * @return
     */
    public String getFilterOp() {
        return filterOp;
    }

    public void setFilterOp(String filterOp) {
        this.filterOp = filterOp;
    }
}
