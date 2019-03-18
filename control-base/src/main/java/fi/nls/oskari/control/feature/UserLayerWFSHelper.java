package fi.nls.oskari.control.feature;

import java.util.Arrays;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.PropertyUtil;

public class UserLayerWFSHelper {

    public static final String PROP_USERLAYER_BASELAYER_ID = "userlayer.baselayer.id";

    private static final String PREFIX_USERLAYER = "userlayer_";
    private static final String USERLAYER_ATTR_GEOMETRY = "oskari:geometry";
    private static final String USERLAYER_ATTR_USER_LAYER_ID = "oskari:user_layer_id";
    private static final String USERLAYER_ATTR_UUID = "oskari:uuid";
    private static final String USERLAYER_ATTR_PUBLISHER_NAME = "oskari:publisher_name";

    private FilterFactory ff;
    private int userlayerLayerId;

    public UserLayerWFSHelper() {
        init();
    }

    public void init() {
        this.ff = CommonFactoryFinder.getFilterFactory();
        this.userlayerLayerId = PropertyUtil.getOptional(PROP_USERLAYER_BASELAYER_ID, -2);
    }

    public int getUserlayerLayerId() {
        return userlayerLayerId;
    }

    public boolean isUserlayerLayer(OskariLayer layer) {
        return layer.getId() == userlayerLayerId;
    }

    public boolean isUserlayerLayer(String layerId) {
        return layerId.startsWith(PREFIX_USERLAYER);
    }

    public int getUserlayerId(String layerId) {
        return Integer.parseInt(layerId.substring(PREFIX_USERLAYER.length()));
    }

    public Filter getFilter(int userlayerId, String uuid, ReferencedEnvelope bbox) {
        Expression _userlayerId = ff.property(USERLAYER_ATTR_USER_LAYER_ID);
        Expression _uuid = ff.property(USERLAYER_ATTR_UUID);
        Expression _publisherName = ff.property(USERLAYER_ATTR_PUBLISHER_NAME);

        Filter userlayerIdEquals = ff.equals(_userlayerId, ff.literal(userlayerId));

        Filter uuidEquals = ff.equals(_uuid, ff.literal(uuid));

        Filter publisherNameNotNull = ff.not(ff.isNull(_publisherName));
        Filter publisherNameNotEmpty = ff.notEqual(_publisherName, ff.literal(""));
        Filter publisherNameNotNullNotEmpty = ff.and(publisherNameNotNull, publisherNameNotEmpty);

        Filter uuidEqualsOrPublished = ff.or(uuidEquals, publisherNameNotNullNotEmpty);

        Filter bboxFilter = ff.bbox(USERLAYER_ATTR_GEOMETRY,
                bbox.getMinX(), bbox.getMinY(),
                bbox.getMaxX(), bbox.getMaxY(),
                CRS.toSRS(bbox.getCoordinateReferenceSystem()));

        return ff.and(Arrays.asList(userlayerIdEquals, uuidEqualsOrPublished, bboxFilter));
    }

}
