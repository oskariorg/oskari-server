package fi.nls.oskari.wfs.extension;

import com.vividsolutions.jts.geom.*;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.pojo.GeoJSONFilter;
import fi.nls.oskari.pojo.Location;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.pojo.WFSLayerStore;
import fi.nls.oskari.wfs.WFSFilter;
import fi.nls.oskari.work.WFSMapLayerJob;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.operation.MathTransform;

import java.util.List;

/**
 * WFS geotools filters for Analysis
 *
 * Gives out filter as XML for WFS requests.
 */
public class AnalysisFilter extends WFSFilter {
    private static final Logger log = LogFactory.getLogger(AnalysisFilter.class);

    public static final String ANALYSIS_PREFIX = "analysis_";
    public static final String ANALYSIS_ID_FIELD = "analysis_id";

    private static final FilterFactory2 ff = WFSFilter.getFilterFactory2();

    private WFSLayerStore layer;

    public AnalysisFilter() {
        super();
    }

    /**
     * Init a filter for WFS request payload (XML)
     *
     * Filter types: bbox (location|tile), coordinate (map click), geojson
     * (custom filter), highlight (feature filter)
     *
     * @param layer
     * @param session
     * @param bounds
     */
    public void init(final WFSMapLayerJob.Type type, final WFSLayerStore layer, final SessionStore session,
                     final List<Double> bounds, final MathTransform transform) {
        super.init(type, layer, session, bounds, transform);
        this.layer = super.getWFSLayerStore();
    }
    /**
     * Initializes coordinate filter (map click)
     *
     * @param coordinate
     */
    @Override
    public Filter initCoordinateFilter(Coordinate coordinate) {
        Filter filter = super.initCoordinateFilter(coordinate);

        // Analysis id
        Filter anal = getAnalysisIdFilter();
        if (anal != null)
            filter = ff.and(filter, anal);

        return filter;
    }

    /**
     * Inits filter for select tool (geojson features)
     */
    @Override
    // TODO: MAYBE DOESN'T WORK CORRECTLY =/
    public Filter initGeoJSONFilter(GeoJSONFilter geoJSONFilter) {
        Filter filter = super.initGeoJSONFilter(geoJSONFilter);

        // Analysis id
        Filter anal = getAnalysisIdFilter();
        if (anal != null)
            filter = ff.and(filter, anal);

        return filter;
    }

    /**
     * Initializes bounding box filter (normal)
     *
     * @param location
     */
    @Override
    public Filter initBBOXFilter(Location location) {
        Filter filter = super.initBBOXFilter(location);

        // Analysis id
        Filter anal = getAnalysisIdFilter();
        if (anal != null)
            filter = ff.and(filter, anal);

        return filter;
    }

    /**
     * Creates WFS analysis id filter, if layer begins with "analysis_"
     *
     * @return analysis_id equal WFS filter
     */
    public Filter getAnalysisIdFilter() {
        log.debug("Layer id " + layer.getLayerId());
        if(!layer.getLayerId().startsWith(ANALYSIS_PREFIX)) {
            log.error("Failed to create analysis id filter (not an analysis layer)", layer.getLayerId());
            return null;
        }

        // add analysis_id filter
        Filter anal = null;
        String[] values = layer.getLayerId().split("_");
        String analysisId = values[values.length - 1];
        anal = ff.equal(ff.property(ANALYSIS_ID_FIELD), ff.literal(analysisId), false);

        return anal;
    }
}
