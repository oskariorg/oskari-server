package fi.nls.oskari.wfs;

import com.vividsolutions.jts.geom.*;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.pojo.GeoJSONFilter;
import fi.nls.oskari.pojo.Location;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.pojo.WFSLayerStore;
import fi.nls.oskari.wfs.extension.AnalysisFilter;
import fi.nls.oskari.work.WFSMapLayerJob;
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
    public static final double CONVERSION_FACTOR = 2.54/1200; // 12th of an inch

    private static final Logger log = LogFactory.getLogger(WFSFilter.class);

    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
    private static final GeometryFactory gf = JTSFactoryFinder.getGeometryFactory(null);
    private static final GeometricShapeFactory gsf = new GeometricShapeFactory(gf);

    private WFSMapLayerJob.Type type;
    private WFSLayerStore layer;
    private SessionStore session;
    private List<Double> bounds;
    private MathTransform transform;

    private double defaultBuffer;

    private String xml;

    /**
     * Empty constructor
     */
    public WFSFilter() {}

    /**
     * Gets FilterFactory2 (for extensions)
     *
     * @return ff
     */
    public static FilterFactory2 getFilterFactory2() {
        return ff;
    }

    /**
     * Gets WFSLayerStore (for extensions)
     *
     * @return layer
     */
    public WFSLayerStore getWFSLayerStore() {
        return layer;
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
        this.type = type;
        this.layer = layer;
        this.session = session;
        this.bounds = bounds;
        this.transform = transform;
        this.defaultBuffer = getDefaultBuffer(this.session.getMapScales().get((int)this.session.getLocation().getZoom()));

        Filter filter = null;
        if(this.type == WFSMapLayerJob.Type.HIGHLIGHT) {
            log.debug("Filter: highlight");
            List<String> featureIds = session.getLayers().get(layer.getLayerId()).getHighlightedFeatureIds();
            filter = initFeatureIdFilter(featureIds);
        } else if(this.type == WFSMapLayerJob.Type.MAP_CLICK) {
            log.debug("Filter: map click");
            Coordinate coordinate = session.getMapClick();
            filter =initCoordinateFilter(coordinate);
        } else if(this.type == WFSMapLayerJob.Type.GEOJSON) {
            log.debug("Filter: GeoJSON");
            GeoJSONFilter geoJSONFilter = session.getFilter();
            filter = initGeoJSONFilter(geoJSONFilter);
        } else if(this.type == WFSMapLayerJob.Type.NORMAL) {
            log.debug("Filter: normal");
            Location location;
            if(this.bounds != null) {
                location = new Location(session.getLocation().getSrs());
                location.setBbox(this.bounds);
            } else {
                location = session.getLocation();
            }
            filter = initBBOXFilter(location);
        } else {
            log.error("Failed to create a filter (invalid type)");
        }

        initXML(filter);
    }

    /**
     * Gets filter as XML
     * 
     * @return xml
     */
    public String getXML() {
        return this.xml;
    }

    /**
     * Inits XML String
     */
    private String initXML(Filter filter) {
        if(filter == null) {
            log.error("Failed to create XML for the filter (null)");
            return null;
        }

        // configuration that makes correct XML elements (v1_1 uses exterior and bbox envelope works)
        Configuration configuration = new org.geotools.filter.v1_1.OGCConfiguration();
        Encoder encoder = new Encoder(configuration);
        try {
            this.xml = encoder.encodeAsString(filter, org.geotools.filter.v1_1.OGC.Filter);
        } catch (IOException e) {
            log.error(e, "Encoding filter to String (xml) failed");
        }

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
     * Sets the default buffer.
     */
    public double getDefaultBuffer(double mapScale) {
        log.debug("Default buffer size", mapScale*CONVERSION_FACTOR);
        return mapScale * CONVERSION_FACTOR;
    }

    /**
     * Initializes feature filter (highlight)
     *
     * @param featureIds
     */
    public Filter initFeatureIdFilter(List<String> featureIds) {
        if(featureIds == null || featureIds.size() == 0) {
            log.error("Failed to create feature filter (missing feature ids)");
            return null;
        }

        Set<FeatureId> fids = new HashSet<FeatureId>();
        for (String fid : featureIds) {
            fids.add(ff.featureId(fid));
        }

        Filter filter = ff.id(fids);

        return filter;
    }

    /**
     * Initializes coordinate filter (map click)
     *
     * @param coordinate
     */
    public Filter initCoordinateFilter(Coordinate coordinate) {
        if (coordinate == null || this.defaultBuffer == 0.0d) {
            log.error("Failed to create coordinate filter (coordinate or default buffer is unset)");
            return null;
        }

        gsf.setSize(this.defaultBuffer);
        gsf.setCentre(coordinate);
        gsf.setNumPoints(CIRCLE_POINTS_COUNT);

        Polygon polygon = gsf.createCircle();

        // transform
        if (this.transform != null) {
            log.debug("transforming mapClick", coordinate);
            try {
                polygon = (Polygon) JTS.transform(polygon, this.transform);
            } catch (Exception e) {
                log.error(e, "Transforming failed");
            }
        }

        Filter filter = ff.intersects(ff.property(layer
                .getGMLGeometryProperty()), ff.literal(polygon));

        // Analysis id
        AnalysisFilter analysis = new AnalysisFilter();
        Filter anal = analysis.getAnalysisIdFilter();
        if (anal != null)
            filter = ff.and(filter, anal);

        return filter;
    }

    /**
     * Inits filter for select tool (geojson features)
     */
    public Filter initGeoJSONFilter(GeoJSONFilter geoJSONFilter) {
        if(geoJSONFilter == null || geoJSONFilter.getFeatures() == null || this.defaultBuffer == 0.0d) {
            log.error("Failed to create geoJSON filter (invalid JSON or default buffer unset)");
            return null;
        }
        Filter filter = null;

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
                    distance = this.defaultBuffer;
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

                if (filter == null) { // first
                    filter = tmpFilter;
                } else { // if many filters, combine with or
                    filter = ff.or(filter, tmpFilter);
                }
                // TODO: try to get the analysis filter to work outside the iteration
                // OTHERWISE: have to get the Filter as a param and add in the loop =/
                // Analysis id
                AnalysisFilter analysis = new AnalysisFilter();
                Filter anal = analysis.getAnalysisIdFilter();
                if (anal != null)
                    filter = ff.and(filter, anal);
            }
        } catch (JSONException e) {
            log.error(e, "Reading geojson data failed");
        } catch (Exception e) {
            log.error(e, "Generating geometries from geojson failed");
        }

        return filter;
    }

    /**
     * Initializes bounding box filter (normal)
     *
     * @param location
     */
    public Filter initBBOXFilter(Location location) {
        if(location == null || this.layer == null) {
            log.error("Failed to create BBOX filter (location or layer is unset)");
            return null;
        }

        Filter filter = ff.bbox(ff.property(layer.getGMLGeometryProperty()),
                location.getTransformEnvelope(layer.getSRSName(), true));

        // Analysis id
        AnalysisFilter analysis = new AnalysisFilter();
        Filter anal = analysis.getAnalysisIdFilter();
        if (anal != null)
            filter = ff.and(filter, anal);

        return filter;
    }
}
