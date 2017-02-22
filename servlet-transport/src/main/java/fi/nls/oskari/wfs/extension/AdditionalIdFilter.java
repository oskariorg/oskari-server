package fi.nls.oskari.wfs.extension;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.wfs.WFSFilter;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.work.JobType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.operation.MathTransform;

import java.util.List;

public class AdditionalIdFilter extends WFSFilter {
    private static final Logger LOG = LogFactory.getLogger(AdditionalIdFilter.class);

    private static final FilterFactory2 ff = WFSFilter.getFilterFactory2();

    private String layerPrefix = "";
    private String layerIdField = "";
    private String layerUidField = "";


    public AdditionalIdFilter(String layerPrefix, String layerIdField, String layerUidField) {
        super();

        this.layerPrefix = layerPrefix;
        this.layerIdField = layerIdField;
        this.layerUidField = layerUidField;
    }

    /**
     * Init a filter for WFS request payload (XML)
     * <p/>
     * Filter types: bbox (location|tile), coordinate (map click), geojson
     * (custom filter), highlight (feature filter)
     *
     * @param type
     * @param layer
     * @param session
     * @param bounds
     * @param transform
     */
    @Override
    public String create(final JobType type, final WFSLayerStore layer, final SessionStore session,
                         final List<Double> bounds, final MathTransform transform) {
        if (type == null || layer == null || session == null) {
            LOG.error("Parameters not set (type, layer, session)", type, layer, session);
            return null;
        }
        super.create(type, layer, session, bounds, transform, false);

        Filter filter = getFilter(type, session, bounds);

        // Include myplaces/analysis/userlayer id to basefilter
        if (type == JobType.MAP_CLICK) {
            Filter idFilter = initIdFilter(layer, session);
            filter = ff.and(filter, idFilter);

        } else if (type == JobType.GEOJSON) {
            Filter idFilter = initIdFilter(layer, session);
            filter = ff.and(filter, idFilter);

        } else if (type == JobType.NORMAL) {
            // Analysis id
            Filter idFilter = initIdFilter(layer, session);
            filter = ff.and(filter, idFilter);

        } else if (type == JobType.PROPERTY_FILTER) {
            Filter idFilter = initIdFilter(layer, session);
            filter = ff.and(filter, idFilter);

        } else {
            LOG.error("Failed to create a filter (invalid type)");
        }

        return createXML(filter, null);
    }

    /**
     * Creates WFS analysis id filter
     *
     * @param layerId
     * @return id equal WFS filter
     */
    public Filter initIdFilter(String layerId, String uid) {
        if (layerId == null || !layerId.startsWith(layerPrefix)) {
            LOG.error("Failed to create analysis id filter (not an analysis layer)", layerId);
            return null;
        }

        // create analysis id filter
        String[] values = layerId.split("_");
        String id = values[values.length - 1]; // TODO: add to redis?
        Filter idFilter = ff.equal(ff.property(layerIdField), ff.literal(id), false);
        Filter uidFilter = ff.equal(ff.property(layerUidField), ff.literal(uid), false);
        return ff.and(idFilter, uidFilter);

    }

    /**
     * Uses the stored layer's creator's uuid if the layer is published and the uuid is available.
     * Otherwise uses the uuid from the session.
     *
     * @param layer
     * @param session
     * @return id and uuid filter of type 'AND'
     */
    private Filter initIdFilter(final WFSLayerStore layer, final SessionStore session) {
        if (layer.isPublished() && layer.getUuid() != null) {
            return initIdFilter(layer.getLayerId(), layer.getUuid());
        } else {
            return initIdFilter(layer.getLayerId(), session.getUuid());
        }
    }
}
