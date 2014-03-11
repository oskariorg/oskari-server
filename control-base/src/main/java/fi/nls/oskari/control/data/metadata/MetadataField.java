package fi.nls.oskari.control.data.metadata;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 11.3.2014
 * Time: 14:00
 * To change this template use File | Settings | File Templates.
 */
public enum MetadataField {

    TYPE("type", true), // type
    SERVICE_TYPE("serviceType", true), // serviceType
    SERVICE_NAME("serviceName", "Title"), // Title
    ORGANIZATION("organization", "orgName"), // orgName
    COVERAGE("coverage", new CoverageHandler()), // CataloguePopulatorService.populateVillageSelects(locale)
    INSPIRE_THEME("inspireTheme", new InspireThemeHandler()),
    KEYWORD("keyword"), // keyword
    TOPIC("topic", "topicCategory"); // topicCategory

    private String name = null;
    private boolean multi = false;
    private MetadataFieldHandler handler = null;


    private MetadataField(String name, final String property) {
        this(name, new MetadataFieldHandler(property));
    }

    private MetadataField(String name) {
        this(name, false);
    }

    private MetadataField(String name, boolean isMulti) {
        this(name, isMulti, null);
    }

    private MetadataField(String name, final MetadataFieldHandler handler) {
        this(name, false, handler);
    }
    private MetadataField(String name, boolean isMulti, MetadataFieldHandler handler) {
        this.name = name;
        this.multi = isMulti;

        if(handler == null) {
            handler = new MetadataFieldHandler(name);
        }
        handler.setMetadataField(this);
        this.handler = handler;
    }
    public boolean isMulti() {
        return multi;
    }
    public String getName() {
        return name;
    }
    public MetadataFieldHandler getHandler() {
        return handler;
    }
}
