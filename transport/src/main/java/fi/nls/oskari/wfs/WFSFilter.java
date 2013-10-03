package fi.nls.oskari.wfs;

import com.vividsolutions.jts.geom.*;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.pojo.Location;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.pojo.WFSLayerStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.operation.MathTransform;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * WFS geotools filter creation
 * 
 * Gives out filter as XML for WFS requests.
 */
public class WFSFilter {
    public static final String GT_GEOM_POINT = "POINT";
    public static final String GT_GEOM_LINESTRING = "LINESTRING";
    public static final String GT_GEOM_POLYGON = "POLYGON";
    public static final int CIRCLE_POINTS_COUNT = 10;

    public static final String ANALYSIS_PREFIX = "analysis_";
    public static final String ANALYSIS_ID_FIELD = "analysis_id";

    private static final Logger log = LogFactory.getLogger(WFSFilter.class);

    private static final FilterFactory2 ff = CommonFactoryFinder
            .getFilterFactory2(null);
    private static final GeometryFactory gf = JTSFactoryFinder
            .getGeometryFactory(null);
    private static final GeometricShapeFactory gsf = new GeometricShapeFactory(
            gf);

    private final WFSLayerStore layer;
    private final SessionStore session;
    private final MathTransform transform;
    private final double defaultBuffer;

    private Filter filter;
    private String xml;

    /**
     * Constructs a filter for WFS request payload (XML)
     * 
     * Filter types: bbox (location|tile), coordinate (map click), geojson
     * (custom filter), highlight (feature filter)
     * 
     * @param layer
     * @param session
     * @param bounds
     */
    public WFSFilter(final WFSLayerStore layer, final SessionStore session,
            final List<Double> bounds, final MathTransform transform) {
        this.layer = layer;
        this.session = session;
        this.transform = transform;
        this.defaultBuffer = 2 * Math.pow(2, (12 - session.getLocation()
                .getZoom())); // TODO: scale with CRS! (now made for EPSG:3067)

        // TODO: transforms to all filters
        if (session.getMapClick() != null) {
            log.debug("Filter: Map click");
            initCoordinateFilter();
        } else if (session.getFilter() != null
                && session.getFilter().getFeatures() != null) {
            log.debug("Filter: Select tool");
            initGeoJSONFilter();
        } else if (session.getLayers().get(layer.getLayerId())
                .getHighlightedFeatureIds() != null) {
            log.debug("Filter: Feature highlight");
            initFeatureFilter();
        } else if (bounds != null) {
            log.debug("Filter: Grid tile");
            Location location = new Location(session.getLocation().getSrs());
            location.setBbox(bounds);
            this.filter = ff.bbox(ff.property(layer.getGMLGeometryProperty()),
                    location.getTransformEnvelope(layer.getSRSName(), true));
            // Analysis id
            Filter anal = getAnalysisIdFilter();
            if (anal != null)
                this.filter = ff.and(this.filter, anal);
            
        } else if (session.getLocation() != null) {
            log.debug("Filter: Location");
            this.filter = ff.bbox(ff.property(layer.getGMLGeometryProperty()),
                    session.getLocation().getTransformEnvelope(
                            layer.getSRSName(), true));
            // Analysis id
            Filter anal = getAnalysisIdFilter();
            if (anal != null)
                this.filter = ff.and(this.filter, anal);

        } else {
            log.error("Filter couldn't be created");
        }

        if (this.filter != null) {
            initXML();
        }
    }

    /**
     * Gets filter as XML
     * 
     * @return xml
     */
    public String getXML() {
        // remove namespacing
        if (this.xml.contains("urn:x-ogc:def:crs:")) {
            this.xml = this.xml.replace("urn:x-ogc:def:crs:", "");
        }
        // replace # => : if Arc 9.3 server (using GML2 separator)
        if (this.layer.isGML2Separator()) {
            this.xml = this.xml.replace("epsg.xml#", "epsg.xml:");
        }
        return this.xml;
    }

    /**
     * Inits XML String
     */
    private void initXML() {
        // configuration that makes correct XML elements (v1_1 uses exterior and
        // bbox envelope works)
        Configuration configuration = new org.geotools.filter.v1_1.OGCConfiguration();
        Encoder encoder = new Encoder(configuration);
        try {
            this.xml = encoder.encodeAsString(this.filter,
                    org.geotools.filter.v1_1.OGC.Filter);
        } catch (IOException e) {
            log.error(e, "Encoding filter to String (xml) failed");
        }
    }

    /**
     * Inits filter for map click (coordinate)
     */
    private void initCoordinateFilter() {
        gsf.setSize(defaultBuffer);
        gsf.setCentre(session.getMapClick());
        gsf.setNumPoints(CIRCLE_POINTS_COUNT);

        Polygon polygon = gsf.createCircle();

        // transform
        if (this.transform != null) {
            log.debug("transforming mapClick", session.getMapClick());
            try {
                polygon = (Polygon) JTS.transform(polygon, this.transform);
            } catch (Exception e) {
                log.error(e, "Transforming failed");
            }
        }

        Filter filter = ff.intersects(ff.property(layer
                .getGMLGeometryProperty()), ff.literal(polygon));

        // Analysis id
        Filter anal = getAnalysisIdFilter();
        if (anal != null)
            filter = ff.and(filter, anal);

        this.filter = filter;
    }

    /**
     * Inits filter for select tool (geojson features)
     */
    private void initGeoJSONFilter() {
        this.filter = null; // reset

        Polygon polygon = null;
        Filter tmpFilter = null;

        JSONArray features = (JSONArray) session.getFilter().getFeatures();
        try {
            for (int i = 0; i < features.length(); i++) {
                polygon = null;
                tmpFilter = null;
                JSONObject feature = (JSONObject) features.get(i);
                JSONObject geometry = (JSONObject) feature.get("geometry");

                JSONObject properties = (JSONObject) feature.get("properties");
                String sdistance = properties.optString("buffer_radius", "0");
                double distance = Double.parseDouble(sdistance);
                if (distance == 0) {
                    distance = defaultBuffer;
                }

                String geomType = geometry.getString("type").toUpperCase();
                GeometryJSON geom = new GeometryJSON(3);
                if (geomType.equals(GT_GEOM_POLYGON)) {
                    polygon = geom.readPolygon(geometry.toString());
                } else if (geomType.equals(GT_GEOM_LINESTRING)) {
                    LineString lineGeom = geom.readLine(geometry.toString());
                    Geometry gtgeom = (Geometry) lineGeom;
                    polygon = (Polygon) gtgeom.buffer(distance);
                } else if (geomType.equals(GT_GEOM_POINT)) {
                    Point pointGeom = geom.readPoint(geometry.toString());
                    gsf.setSize(distance);
                    gsf.setCentre(pointGeom.getCoordinate());
                    // IF oskari point (10)
                    gsf.setNumPoints(CIRCLE_POINTS_COUNT);
                    // IF oskari circle (40)
                    // gsf.setNumPoints(40);
                    polygon = gsf.createCircle();
                }

                // transform
                if (this.transform != null) {
                    try {
                        polygon = (Polygon) JTS.transform(polygon,
                                this.transform);
                    } catch (Exception e) {
                        log.error(e, "Transforming failed");
                    }
                }

                tmpFilter = ff.intersects(ff.property(layer
                        .getGMLGeometryProperty()), ff.literal(polygon));

                if (this.filter == null) { // first
                    this.filter = tmpFilter;
                } else { // if many filters, combine with or
                    this.filter = ff.or(this.filter, tmpFilter);
                }
                // Analysis id
                Filter anal = getAnalysisIdFilter();
                if (anal != null)
                    this.filter = ff.and(this.filter, anal);
            }
        } catch (JSONException e) {
            log.error(e, "Reading geojson data failed");
        } catch (Exception e) {
            log.error(e, "Generating geometries from geojson failed");
        }
    }

    /**
     * Inits filter for highlight feature (featureIds)
     */
    private void initFeatureFilter() {
        Set<FeatureId> fids = new HashSet<FeatureId>();
        List<String> featureIds = session.getLayers().get(layer.getLayerId())
                .getHighlightedFeatureIds();
        for (String fid : featureIds) {
            fids.add(ff.featureId(fid));
        }
        this.filter = ff.id(fids);
    }

    /**
     * Creates WFS analysis id filter, if layer begins with "analysis_"
     * @return analysis_id equal WFS filter
     */
    private Filter getAnalysisIdFilter() {
        Filter anal = null;
        log.debug("Layer id "+this.layer.getLayerId());
        if (this.layer.getLayerId().indexOf(ANALYSIS_PREFIX) > -1) {
            // add analysis_id filter
            String[] values = this.layer.getLayerId().split("_");
            if (values.length > 0)
                anal = ff.equal(ff.property(ANALYSIS_ID_FIELD), ff
                        .literal(values[values.length - 1]), false);
        }
        return anal;
    }
}
