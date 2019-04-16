package fi.nls.oskari.control.feature;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.oskari.service.user.UserLayerService;

import java.util.Arrays;

@Oskari
public class AnalysisWFSHelper extends UserLayerService {

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

    public int getBaselayerId() {
        return userlayerLayerId;
    }

    public boolean isUserContentLayer(String layerId) {
        return layerId.startsWith(PREFIX_ANALYSIS);
    }

    public int parseId(String layerId) {
        return Integer.parseInt(layerId.substring(layerId.lastIndexOf('_') + 1));
    }

    public Filter getWFSFilter(String analysisLayerId, String uuid, ReferencedEnvelope bbox) {
        int layerId = parseId(analysisLayerId);
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

    public boolean hasPermission(String id, User user) {
        return true;
    }

}
