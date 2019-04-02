package fi.nls.oskari.control.feature;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;

import java.util.Arrays;

public class AnalysisWFSHelper {

    public static final String PROP_ANALYSIS_BASELAYER_ID = "analysis.baselayer.id";
    public static final String PREFIX_ANALYSIS = "analysis_";

    protected static final String ATTR_GEOMETRY = "geometry";
    private static final String ATTR_LAYER_ID = "analysis_id";
    private static final String ATTR_UUID = "uuid";

    private FilterFactory ff;
    private int userlayerLayerId;
    //private static final AnalysisDbService analysisService = new AnalysisDbServiceMybatisImpl();

    public AnalysisWFSHelper() {
        init();
    }

    public void init() {
        this.ff = CommonFactoryFinder.getFilterFactory();
        this.userlayerLayerId = PropertyUtil.getOptional(PROP_ANALYSIS_BASELAYER_ID, -2);
    }

    public int getLayerId() {
        return userlayerLayerId;
    }

    public boolean isLayer(OskariLayer layer) {
        return layer.getId() == userlayerLayerId;
    }

    public boolean isLayer(String layerId) {
        return layerId.startsWith(PREFIX_ANALYSIS);
    }

    public int getId(String layerId) {
        return Integer.parseInt(layerId.substring(layerId.lastIndexOf('_') + 1));
    }

    public Filter getFilter(int layerId, String uuid, ReferencedEnvelope bbox) {
        Expression _layerId = ff.property(ATTR_LAYER_ID);
        Expression _uuid = ff.property(ATTR_UUID);

        Filter userlayerIdEquals = ff.equals(_layerId, ff.literal(layerId));

        Filter uuidEquals = ff.equals(_uuid, ff.literal(uuid));

        Filter bboxFilter = ff.bbox(ATTR_GEOMETRY,
                bbox.getMinX(), bbox.getMinY(),
                bbox.getMaxX(), bbox.getMaxY(),
                CRS.toSRS(bbox.getCoordinateReferenceSystem()));

        return ff.and(Arrays.asList(userlayerIdEquals, uuidEquals, bboxFilter));
    }
}
