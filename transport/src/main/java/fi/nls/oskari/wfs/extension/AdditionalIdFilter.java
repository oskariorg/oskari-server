package fi.nls.oskari.wfs.extension;

import com.vividsolutions.jts.geom.Coordinate;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.GeoJSONFilter;
import fi.nls.oskari.pojo.Location;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.pojo.WFSLayerStore;
import fi.nls.oskari.wfs.WFSFilter;
import fi.nls.oskari.work.WFSMapLayerJob;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.operation.MathTransform;

import java.util.List;

public class AdditionalIdFilter extends WFSFilter {
    private static final Logger log = LogFactory.getLogger(AdditionalIdFilter.class);

    private static final FilterFactory2 ff = WFSFilter.getFilterFactory2();

    private String layerPrefix = "";
    private String layerIdField = "";

    public AdditionalIdFilter(String layerPrefix, String layerIdField) {
        super();

        this.layerPrefix = layerPrefix;
        this.layerIdField = layerIdField;
    }

    /**
     * Init a filter for WFS request payload (XML)
     *
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
    public String create(final WFSMapLayerJob.Type type, final WFSLayerStore layer, final SessionStore session,
                         final List<Double> bounds, final MathTransform transform) {
        if(type == null || layer == null || session == null) {
            log.error("Parameters not set (type, layer, session)", type, layer, session);
            return null;
        }
        super.create(type, layer, session, bounds, transform, false);

        Filter filter = null;
        if(type == WFSMapLayerJob.Type.HIGHLIGHT) {
            log.debug("Filter: highlight");
            List<String> featureIds = session.getLayers().get(layer.getLayerId()).getHighlightedFeatureIds();
            filter = super.initFeatureIdFilter(featureIds);
        } else if(type == WFSMapLayerJob.Type.MAP_CLICK) {
            log.debug("Filter: map click");
            super.setDefaultBuffer(session.getMapScales().get((int) session.getLocation().getZoom()));
            Coordinate coordinate = session.getMapClick();
            filter = super.initCoordinateFilter(coordinate);

            // Analysis id
            Filter idFilter = initIdFilter(layer.getLayerId());
            filter = ff.and(filter, idFilter);

        } else if(type == WFSMapLayerJob.Type.GEOJSON) {
            log.debug("Filter: GeoJSON");
            super.setDefaultBuffer(session.getMapScales().get((int) session.getLocation().getZoom()));
            GeoJSONFilter geoJSONFilter = session.getFilter();
            filter = super.initGeoJSONFilter(geoJSONFilter);

            // Analysis id
            Filter idFilter = initIdFilter(layer.getLayerId());
            filter = ff.and(filter, idFilter);

        } else if(type == WFSMapLayerJob.Type.NORMAL) {
            log.debug("Filter: normal");
            Location location;
            if(bounds != null) {
                location = new Location(session.getLocation().getSrs());
                location.setBbox(bounds);
            } else {
                location = session.getLocation();
            }
            filter = super.initEnlargedBBOXFilter(location, layer);

            // Analysis id
            Filter idFilter = initIdFilter(layer.getLayerId());
            filter = ff.and(filter, idFilter);

        } else {
            log.error("Failed to create a filter (invalid type)");
        }

        return createXML(filter);
    }

    /**
     * Creates WFS analysis id filter
     *
     * @param layerId
     *
     * @return id equal WFS filter
     */
    public Filter initIdFilter(String layerId) {
        if(layerId == null || !layerId.startsWith(layerPrefix)) {
            log.error("Failed to create analysis id filter (not an analysis layer)", layerId);
            return null;
        }

        // create analysis id filter
        String[] values = layerId.split("_");
        String id = values[values.length - 1]; // TODO: add to redis?
        Filter idFilter = ff.equal(ff.property(layerIdField), ff.literal(id), false);

        return idFilter;
    }
}
