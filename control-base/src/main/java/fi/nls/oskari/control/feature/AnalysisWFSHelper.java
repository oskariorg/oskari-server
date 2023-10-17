package fi.nls.oskari.control.feature;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.cache.ComputeOnceCache;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.map.analysis.service.AnalysisDataService;
import fi.nls.oskari.map.analysis.service.AnalysisDbService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.geojson.GeoJSONFeatureCollection;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.PermissionType;
import org.oskari.permissions.model.ResourceType;
import org.oskari.service.user.UserLayerService;
import org.oskari.service.wfs.client.CachingOskariWFSClient;
import org.oskari.service.wfs.client.OskariWFSClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Oskari
public class AnalysisWFSHelper extends UserLayerService {

    public static final String PROP_ANALYSIS_BASELAYER_ID = "analysis.baselayer.id";
    public static final String PREFIX_ANALYSIS = "analysis_";

    protected static final String ATTR_GEOMETRY = "geometry";
    private static final String ATTR_LAYER_ID = "analysis_id";

    private static final List<String> HIDDEN_PROPERTIES = Arrays.asList(ATTR_LAYER_ID, "created", "bbox", "uuid");

    private FilterFactory ff;
    private int analysisLayerId;
    private AnalysisDbService service;
    private ComputeOnceCache<Set<String>> permissionsCache;
    private OskariWFSClient wfsClient = new CachingOskariWFSClient();

    public AnalysisWFSHelper() {
        init();
    }

    public void init() {
        this.ff = CommonFactoryFinder.getFilterFactory();
        this.analysisLayerId = PropertyUtil.getOptional(PROP_ANALYSIS_BASELAYER_ID, -2);

        // One minute cache to get most of the requests from going to db but also a workaround the issue that:
        // FIXME: this is not the right place to cache permissions.
        long expireInOneMinute = 60L * 1000L;
        permissionsCache = CacheManager.getCache(getClass().getName(),() -> new ComputeOnceCache<>(100, expireInOneMinute));
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

        // caching for permissions
        final Set<String> permissions = getPermissionsForUser(user);
        return permissions.contains("analysis+" + layerId);
    }

    private Set<String> getPermissionsForUser(User user) {
        return permissionsCache.get(Long.toString(user.getId()),
                __ ->
                        OskariComponentManager.getComponentOfType(PermissionService.class).getResourcesWithGrantedPermissions(
                                ResourceType.analysislayer, user, PermissionType.VIEW_PUBLISHED));
    }

    protected Analysis getLayer(int id) {
        if (service == null) {
            // might cause problems with timing of components being initialized if done in init/constructor
            service = OskariComponentManager.getComponentOfType(AnalysisDbService.class);
        }
        return service.getAnalysisById(id);
    }
    protected OskariLayer getBaseLayer() {
        return AnalysisDataService.getBaseLayer();
    }

    public SimpleFeatureCollection postProcess(SimpleFeatureCollection sfc) throws Exception {
        List<SimpleFeature> fc = new ArrayList<>();
        SimpleFeatureType schema;

        try (SimpleFeatureIterator it = sfc.features()) {
            if (!it.hasNext()) {
                return sfc;
            }
            SimpleFeature ftr = it.next();
            schema = createType(sfc.getSchema(), ftr);
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
            List<AttributeDescriptor> attributes = schema.getAttributeDescriptors();
            attributes.stream().forEach(attr ->
                    builder.set(attr.getLocalName(), ftr.getAttribute(attr.getLocalName())));
            fc.add(builder.buildFeature(ftr.getID()));

            while (it.hasNext()) {
                SimpleFeature f = it.next();
                attributes.stream().forEach(attr ->
                        builder.set(attr.getLocalName(), f.getAttribute(attr.getLocalName())));
                fc.add(builder.buildFeature(f.getID()));
            }
        }

        return new GeoJSONFeatureCollection(fc, schema);
    }

    private SimpleFeatureType createType(SimpleFeatureType schema, SimpleFeature f) {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName(schema.getName());
        typeBuilder.setDefaultGeometry(schema.getGeometryDescriptor().getLocalName());
        f.getFeatureType().getAttributeDescriptors().stream()
                .filter(attr -> isVisibleProperty(attr.getLocalName()))
                .forEach(attr -> typeBuilder.add(attr));
        return typeBuilder.buildFeatureType();
    }

    private boolean isVisibleProperty(String name) {
        return HIDDEN_PROPERTIES.stream().noneMatch(propName -> propName.equals(name));
    }

    @Override
    public SimpleFeatureCollection getFeatures(String layerId, OskariLayer layer, ReferencedEnvelope bbox, CoordinateReferenceSystem crs) throws ServiceException {
        try {
            Filter filter = this.getWFSFilter(layerId, bbox);
            SimpleFeatureCollection sfc = wfsClient.getFeatures(layer, bbox, crs, filter);
            return this.postProcess(sfc);
        } catch(Exception e) {
            throw new ServiceException("Failed to get features. ", e);
        }
    }
}
