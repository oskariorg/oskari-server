package fi.nls.oskari.control.feature;

import fi.mml.portti.domain.permissions.Permissions;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.map.analysis.service.AnalysisDbService;
import fi.nls.oskari.map.analysis.service.AnalysisDbServiceMybatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.oskari.service.user.UserLayerService;
import org.oskari.service.util.ServiceFactory;

import java.util.Arrays;
import java.util.Set;

@Oskari
public class AnalysisWFSHelper extends UserLayerService {

    public static final String PROP_ANALYSIS_BASELAYER_ID = "analysis.baselayer.id";
    public static final String PREFIX_ANALYSIS = "analysis_";

    protected static final String ATTR_GEOMETRY = "geometry";
    private static final String ATTR_LAYER_ID = "analysis_id";

    private FilterFactory ff;
    private int analysisLayerId;
    private AnalysisDbService service;

    public AnalysisWFSHelper() {
        init();
    }

    public void init() {
        this.ff = CommonFactoryFinder.getFilterFactory();
        this.analysisLayerId = PropertyUtil.getOptional(PROP_ANALYSIS_BASELAYER_ID, -2);
    }

    public int getBaselayerId() {
        return analysisLayerId;
    }

    public boolean isUserContentLayer(String layerId) {
        return layerId.startsWith(PREFIX_ANALYSIS);
    }

    public int parseId(String layerId) {
        return Integer.parseInt(layerId.substring(layerId.lastIndexOf('_') + 1));
    }

    public Filter getWFSFilter(String analysisLayerId, ReferencedEnvelope bbox) {
        int layerId = parseId(analysisLayerId);
        Expression _layerId = ff.property(ATTR_LAYER_ID);

        Filter userlayerIdEquals = ff.equals(_layerId, ff.literal(layerId));

        Filter bboxFilter = ff.bbox(ATTR_GEOMETRY,
                bbox.getMinX(), bbox.getMinY(),
                bbox.getMaxX(), bbox.getMaxY(),
                CRS.toSRS(bbox.getCoordinateReferenceSystem()));

        return ff.and(Arrays.asList(userlayerIdEquals, bboxFilter));
    }

    public boolean hasViewPermission(String id, User user) {
        int layerId = parseId(id);
        final Analysis layer = getLayer(layerId);
        if (layer == null) {
            return false;
        }
        if (layer.isOwnedBy(user.getUuid())) {
            return true;
        }
        // TODO: caching
        final Set<String> permissions = ServiceFactory.getPermissionsService().getResourcesWithGrantedPermissions(
                AnalysisLayer.TYPE, user, Permissions.PERMISSION_TYPE_VIEW_PUBLISHED);
        return permissions.contains("analysis+" + layerId);
    }

    private Analysis getLayer(int id) {
        if (service == null) {
            // might cause problems with timing of components being initialized if done in init/constructor
            service = new AnalysisDbServiceMybatisImpl();
        }
        // TODO: caching
        return service.getAnalysisById(id);
    }
}
