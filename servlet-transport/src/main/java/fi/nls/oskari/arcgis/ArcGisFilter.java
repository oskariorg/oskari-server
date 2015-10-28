package fi.nls.oskari.arcgis;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.GeoJSONFilter;
import fi.nls.oskari.pojo.Location;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.work.JobType;
import org.apache.commons.lang.StringUtils;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.operation.MathTransform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * WFS geotools filter creation
 * 
 * Gives out filter as XML for WFS requests.
 */
public class ArcGisFilter {
    public static final String GT_GEOM_POINT = "POINT";
    public static final String GT_GEOM_LINESTRING = "LINESTRING";
    public static final String GT_GEOM_POLYGON = "POLYGON";
    public static final int CIRCLE_POINTS_COUNT = 10;
    public static final double CONVERSION_FACTOR = 2.54/1200; // 12th of an inch

    private static final Logger log = LogFactory.getLogger(ArcGisFilter.class);

    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
    private static final GeometryFactory gf = JTSFactoryFinder.getGeometryFactory(null);
    private static final GeometricShapeFactory gsf = new GeometricShapeFactory(gf);

    private WFSLayerStore layer;
    private double defaultBuffer;

    private String xml;

    /**
     * Empty constructor
     */
    public ArcGisFilter() {}

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
     * Create a filter for WFS request payload (XML)
     *
     * Filter types: bbox (location|tile), coordinate (map click), geojson
     * (custom filter), highlight (feature filter)
     *
     * @param type
     * @param layer
     * @param session
     * @param bounds
     *
     * @return xml
     */
    public HashMap<String, String> create(final JobType type, final WFSLayerStore layer, final SessionStore session,
                     final List<Double> bounds) {
        return create(type, layer, session, bounds, true);
    }

    /**
     * Create a filter for WFS request payload (XML)
     *
     * Filter types: bbox (location|tile), coordinate (map click), geojson
     * (custom filter), highlight (feature filter)
     *
     * @param type
     * @param layer
     * @param session
     * @param bounds
     * @param createFilter
     *
     * @return xml
     */
    public HashMap<String, String> create(final JobType type, final WFSLayerStore layer, final SessionStore session,
                     final List<Double> bounds, boolean createFilter) {
        if(type == null || layer == null || session == null) {
            log.error("Parameters not set (type, layer, session)", type, layer, session);
            return null;
        }        
        
        String mapSrs = session.getLocation().getSrs();
        this.layer = layer;

        if(createFilter) {
            HashMap<String, String> filterMap = null;
            if(type == JobType.HIGHLIGHT) {
                log.debug("Filter: highlight");
                List<String> featureIds = session.getLayers().get(layer.getLayerId()).getHighlightedFeatureIds();
                //filter = initFeatureIdFilter(featureIds);
                filterMap = initFeatureIdFilter(featureIds);
            } else if(type == JobType.MAP_CLICK) {
                log.debug("Filter: map click");
                setDefaultBuffer(session.getMapScales().get((int) session.getLocation().getZoom()));
                Coordinate coordinate = session.getMapClick();
                //filter = initCoordinateFilter(coordinate);
                filterMap = initCoordinateFilter(coordinate, mapSrs);
            } else if(type == JobType.GEOJSON) {
                log.debug("Filter: GeoJSON");
                log.error("Unsupported filter GeoJSON");
                //setDefaultBuffer(session.getMapScales().get((int) session.getLocation().getZoom()));
                //GeoJSONFilter geoJSONFilter = session.getFilter();
                //filter = initGeoJSONFilter(geoJSONFilter);
            } else if(type == JobType.NORMAL) {
                log.debug("Filter: normal");
                Location location;
                if(bounds != null) {
                    location = new Location(mapSrs);
                    location.setBbox(bounds);
                } else {
                    location = session.getLocation();
                }

                //filter = initEnlargedBBOXFilter(location, layer);
                filterMap = initEnlargedBBOXFilter(location, layer);
            } else {
                log.error("Failed to create a filter (invalid type)");
            }

            return filterMap;
        }
        return null;
    }

    /**
     * Inits XML String
     *
     * @param filter
     *
     * @return xml
     */
    public String createXML(Filter filter) {
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
     * Gets the default buffer.
     *
     */
    public double getDefaultBuffer() {
        return this.defaultBuffer;
    }

    /**
     * Sets the default buffer.
     *
     * @param mapScale
     */
    public void setDefaultBuffer(double mapScale) {
        log.debug("Default buffer size", mapScale*CONVERSION_FACTOR);
        this.defaultBuffer = mapScale * CONVERSION_FACTOR;
    }

    /**
     * Initializes feature filter (highlight)
     *
     * @param featureIds
     *
     * @return filter
     */
    public HashMap<String, String> initFeatureIdFilter(List<String> featureIds) {
        if(featureIds == null || featureIds.size() == 0) {
            log.error("Failed to create feature filter (missing feature ids)");
            return null;
        }
        
        HashMap<String, String> filter = new HashMap<String, String>();
        
        filter.put("objectIds", StringUtils.join(featureIds, ", "));

//        Set<FeatureId> fids = new HashSet<FeatureId>();
//        for (String fid : featureIds) {
//            fids.add(ff.featureId(fid));
//        }
//
//        Filter filter = ff.id(fids);

        return filter;
    }

    /**
     * Initializes coordinate filter (map click)
     *
     * @param coordinate
     *
     * @return filter
     */
    public HashMap<String, String> initCoordinateFilter(Coordinate coordinate, String mapSrs) {
        if (coordinate == null || this.defaultBuffer == 0.0d) {
            System.out.println("coordinate filter fail");
            log.error("Failed to create coordinate filter (coordinate or default buffer is unset)");
            return null;
        }

        gsf.setSize(getSizeFactor()*this.defaultBuffer);
        gsf.setCentre(coordinate);
        gsf.setNumPoints(CIRCLE_POINTS_COUNT);

        Polygon polygon = gsf.createCircle();

//        // transform
//        if (this.transform != null) {
//            log.debug("transforming mapClick", coordinate);
//            try {
//                polygon = (Polygon) JTS.transform(polygon, this.transform);
//            } catch (Exception e) {
//                log.error(e, "Transforming failed");
//            }
//        }
        
        Envelope envelope = polygon.getEnvelopeInternal();
        
        HashMap<String, String> filter = new HashMap<String, String>();
        
        filter.put("geometry", envelope.getMinX() + "," + envelope.getMinY() + "," + envelope.getMaxX() + "," + envelope.getMaxY());
        filter.put("inSR", mapSrs);
        filter.put("geometryType", "esriGeometryEnvelope");
        filter.put("spatialRel", "esriSpatialRelIntersects");

//        Filter filter = ff.intersects(ff.property(layer
//                .getGMLGeometryProperty()), ff.literal(polygon));

//      Filter filter = ff.bbox(ff.property(layer.getGMLGeometryProperty()),
//      envelope);
        
        return filter;
    }

    /**
     * Inits filter for select tool (geojson features)
     *
     * @param geoJSONFilter
     *
     * @return filter
     */
    public Filter initGeoJSONFilter(GeoJSONFilter geoJSONFilter) {
        if(geoJSONFilter == null || geoJSONFilter.getFeatures() == null || this.defaultBuffer == 0.0d) {
            log.error("Failed to create geoJSON filter (invalid JSON or default buffer unset)");
            return null;
        }
        Filter filter = null;
        List<Filter> geometryFilters = new ArrayList<Filter>();
        Filter tmpFilter = null;
        Polygon polygon = null;

        JSONArray features = (JSONArray) geoJSONFilter.getFeatures();
        try {
            for (int i = 0; i < features.length(); i++) {
                polygon = null;
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

//                // transform
//                if (this.transform != null) {
//                    try {
//                        polygon = (Polygon) JTS.transform(polygon,
//                                this.transform);
//                    } catch (Exception e) {
//                        log.error(e, "Transforming failed");
//                    }
//                }

                tmpFilter = ff.intersects(ff.property(layer
                        .getGMLGeometryProperty()), ff.literal(polygon));

                geometryFilters.add(tmpFilter);
            }
        } catch (JSONException e) {
            log.error(e, "Reading geojson data failed");
        } catch (Exception e) {
            log.error(e, "Generating geometries from geojson failed");
        }

        if(geometryFilters.size() > 1) {
            filter = ff.or(geometryFilters);
        } else {
            filter = tmpFilter;
        }

        return filter;
    }

    /**
     * Defines a radius factor of point sizes for filtering
     *
     * @return factor
     */
    public double getSizeFactor() {
        return 1.0;
    }

    /**
     * Initializes bounding box filter (normal)
     *
     * @param location
     *
     * @return filter
     */
    private HashMap<String, String> initBBOXFilter(Location location, WFSLayerStore layer) {
        if(location == null || layer == null) {
            log.error("Failed to create BBOX filter (location or layer is unset)");
            return null;
        }

        ReferencedEnvelope envelope = location.getEnvelope();
        
        HashMap<String, String> filter = new HashMap<String, String>();
        
        filter.put("geometry", envelope.getMinX() + "," + envelope.getMinY() + "," + envelope.getMaxX() + "," + envelope.getMaxY());
        filter.put("inSR", location.getSrs());
        filter.put("geometryType", "esriGeometryEnvelope");
        filter.put("spatialRel", "esriSpatialRelIntersects");
        
//        Filter filter = ff.bbox(ff.property(layer.getGMLGeometryProperty()),
//                envelope);

        return filter;
    }
    
    public static Geometry initBBOXFilter(final Location location, final MathTransform transform) {
        if(location == null) {
            log.error("Failed to create BBOX filter (location or layer is unset)");
            return null;
        }

        ReferencedEnvelope envelope = location.getEnvelope();
        Coordinate[] coordinates = new Coordinate[5];
        coordinates[0] = new Coordinate(envelope.getMinX(), envelope.getMinY(), 0);
        coordinates[1] = new Coordinate(envelope.getMinX(), envelope.getMaxY(), 0);
        coordinates[2] = new Coordinate(envelope.getMaxX(), envelope.getMaxY(), 0);
        coordinates[3] = new Coordinate(envelope.getMaxX(), envelope.getMinY(), 0);
        coordinates[4] = new Coordinate(envelope.getMinX(), envelope.getMinY(), 0);
        LinearRing ring = new LinearRing(new CoordinateArraySequence(coordinates), gf);
        Geometry polygon = new Polygon(ring, null, gf);        
        
        if (transform == null)
        	return polygon;
        else {
            if(transform != null) {
                try {
                	polygon = JTS.transform(polygon, transform);                    
                } catch (Exception e) {
                    log.error(e, "Transform feature's geometry to user's CRS failed");
                }
            }
        }
        
        return polygon;
    }

    /**
     * Initializes enlarged bounding box filter (normal)
     *
     * @param location
     *
     * @return filter
     */
    private HashMap<String, String> initEnlargedBBOXFilter(Location location, WFSLayerStore layer) {
        if(location == null || layer == null) {
            log.error("Failed to create BBOX filter (location or layer is unset)");
            return null;
        }

        ReferencedEnvelope enlargedEnvelope = location.getEnlargedEnvelope();
        
        HashMap<String, String> filter = new HashMap<String, String>();
                
        filter.put("geometry", enlargedEnvelope.getMinX() + "," + enlargedEnvelope.getMinY() + "," + enlargedEnvelope.getMaxX() + "," + enlargedEnvelope.getMaxY());
        filter.put("inSR", location.getSrs());
        filter.put("geometryType", "esriGeometryEnvelope");
        filter.put("spatialRel", "esriSpatialRelIntersects");
        
//        Filter filter = ff.bbox(ff.property(layer.getGMLGeometryProperty()),
//                enlargedEnvelope);

        return filter;
    }
}
