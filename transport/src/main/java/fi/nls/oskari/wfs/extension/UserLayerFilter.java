package fi.nls.oskari.wfs.extension;

/**
 * WFS geotools filters for user layer data
 *
 * Gives out filter as XML for WFS requests.
 */
public class UserLayerFilter extends AdditionalIdFilter {
    public static final String USERLAYER_BASE_LAYER_ID = "userlayer.baselayer.id";
    public static final String USERLAYER_PREFIX = "userlayer_";
    public static final String USERLAYER_ID_FIELD = "user_layer_id";
    public static final String USERLAYER_UID_FIELD = "uuid";

    public UserLayerFilter() {
        super(USERLAYER_PREFIX, USERLAYER_ID_FIELD, USERLAYER_UID_FIELD);
    }

    /**
     * Defines a radius factor of point sizes for filtering
     *
     * @return factor
     */
    @Override
    public double getSizeFactor() {
        return 5.0;
    }

}
