package fi.nls.oskari.wfs.extension;

/**
 * WFS geotools filters for Analysis
 *
 * Gives out filter as XML for WFS requests.
 */
public class MyPlacesFilter extends AdditionalIdFilter {
    public static final String MY_PLACES_BASE_LAYER_ID = "myplaces.baselayer.id";
    public static final String MY_PLACES_PREFIX = "myplaces_";
    public static final String MY_PLACES_ID_FIELD = "myplaces_id";

    public MyPlacesFilter() {
        super(MY_PLACES_PREFIX, MY_PLACES_ID_FIELD);
    }
}
