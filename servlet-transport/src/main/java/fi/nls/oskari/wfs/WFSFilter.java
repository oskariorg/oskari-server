package fi.nls.oskari.wfs;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.GeometricShapeFactory;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.pojo.GeoJSONFilter;
import fi.nls.oskari.pojo.Location;
import fi.nls.oskari.pojo.PropertyFilter;
import fi.nls.oskari.pojo.SessionStore;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.work.JobType;
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
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.operation.MathTransform;

import java.io.IOException;
import java.util.ArrayList;
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
    public static final double M_TO_DEGREE = 8.96861E-06d;
    public static final String DEGREE_UNIT = "°";

    private static final Logger LOG = LogFactory.getLogger(WFSFilter.class);

    private static final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
    private static final GeometryFactory gf = JTSFactoryFinder.getGeometryFactory(null);
    private static final GeometricShapeFactory gsf = new GeometricShapeFactory(gf);

    private WFSLayerStore layer;
    private MathTransform transform;
    private String unit;
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
     * Create a filter for WFS request payload (XML)
     *
     * Filter types: bbox (location|tile), coordinate (map click), geojson
     * (custom filter), highlight (feature filter)
     *
     * @param type
     * @param layer
     * @param session
     * @param bounds
     * @param transform
     *
     * @return xml
     */
    public String create(final JobType type, final WFSLayerStore layer, final SessionStore session,
                     final List<Double> bounds, final MathTransform transform) {
        return create(type, layer, session, bounds, transform, true);
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
     * @param transform
     * @param createFilter
     *
     * @return xml
     */
    public String create(final JobType type, final WFSLayerStore layer, final SessionStore session,
                     final List<Double> bounds, final MathTransform transform, boolean createFilter) {
        if(type == null || layer == null || session == null) {
            LOG.error("Parameters not set (type, layer, session)", type, layer, session);
            throw new ServiceRuntimeException("Parameters not set (type, layer, session) - layer: "+layer.getLayerId());
        }
        this.layer = layer;
        this.transform = transform;
        this.unit = session.getLocation().getCrsForMap().getCoordinateSystem().getAxis(0).getUnit().toString();

        if(createFilter) {
            Filter filter = getFilter(type, session, bounds);
            String longSrsName = layer.isLongSrsName(session.getLocation().getSrs()) ?
                    ProjectionHelper.longSyntaxEpsg(session.getLocation().getSrs()) : null;
            return createXML(filter, longSrsName);
        }
        return null;
    }

    public Filter getFilter(final JobType type, final SessionStore session, final List<Double> bounds) {

        Filter filter = null;
        if(type == JobType.HIGHLIGHT) {
            LOG.debug("Filter: highlight");
            List<String> featureIds = session.getLayers().get(layer.getLayerId()).getHighlightedFeatureIds();
            filter = initFeatureIdFilter(featureIds);
        } else if(type == JobType.MAP_CLICK) {
            LOG.debug("Filter: map click");
            // scale based default buffer doesn't work so well with non-metric units -> prefer geojson filter
            setDefaultBuffer(session.getMapScales().get((int) session.getLocation().getZoom()));
            GeoJSONFilter geoJSONFilter = session.getFilter();
            if(geoJSONFilter != null && geoJSONFilter.getGeoJSON() != null) {
                LOG.info("Using geojson filter for map click", geoJSONFilter.getGeoJSON());
                filter = initGeoJSONFilter(geoJSONFilter,  session.getLocation().getSrs(), layer);
            } else {
                LOG.info("Using coordinate filter for map click", session.getMapClick());
                Coordinate coordinate = session.getMapClick();
                filter = initCoordinateFilter(coordinate, session.getLocation().getSrs(), layer);
            }
        } else if(type == JobType.GEOJSON) {
            LOG.debug("Filter: GeoJSON");
            // scale based default buffer doesn't work so well with non-metric units -> prefer geojson filter
            setDefaultBuffer(session.getMapScales().get((int) session.getLocation().getZoom()));
            GeoJSONFilter geoJSONFilter = session.getFilter();
            filter = initGeoJSONFilter(geoJSONFilter, session.getLocation().getSrs(), layer);
        }else if(type == JobType.PROPERTY_FILTER) {
            LOG.debug("Filter: Property filter");
            filter = initPropertyFilter(session, bounds, layer);
        } else if(type == JobType.NORMAL) {
            LOG.debug("Filter: normal");
            Location location;
            if(bounds != null) {
                location = new Location(session.getLocation().getSrs());
                location.setBbox(bounds);
            } else {
                location = session.getLocation();
            }

            filter = initEnlargedBBOXFilter(location, layer);
        } else {
            LOG.error("Failed to create a filter (invalid type)");
            throw new ServiceRuntimeException("Failed to create a filter (invalid type) - layer: "+layer.getLayerId());
        }
        return filter;
    }

    /**
     * Inits XML String
     *
     * @param filter
     * @param srsName  if !null, use long srsName syntax
     *
     * @return xml
     */
    public String createXML(Filter filter, String srsName) {
        if(filter == null) {
            LOG.error("Failed to create XML for the filter (null)");
            return null;
        }

        // configuration that makes correct XML elements (v1_1 uses exterior and bbox envelope works)
        Configuration configuration = new org.geotools.filter.v1_1.OGCConfiguration();
        Encoder encoder = new Encoder(configuration);
        try {
            this.xml = encoder.encodeAsString(filter, org.geotools.filter.v1_1.OGC.Filter);
        } catch (IOException e) {
            LOG.error(e, "Encoding filter to String (xml) failed");
            throw new ServiceRuntimeException("Encoding filter to String (xml) failed - layer: "+layer.getLayerId(), e.getCause());
        }

        // Encoding is not valid - remove namespacing or set long SrsName  - this is not a nice solution
        if (this.xml.contains("urn:x-ogc:def:crs:")) {
            if(srsName != null){
                this.xml = this.xml.replace("urn:x-ogc:def:crs:EPSG:", "urn:ogc:def:crs:EPSG::");
            } else {
                this.xml = this.xml.replace("urn:x-ogc:def:crs:", "");
            }
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
        LOG.debug("Default buffer size", mapScale * CONVERSION_FACTOR);
        this.defaultBuffer = mapScale * CONVERSION_FACTOR;
        if(this.unit != null && this.unit.equals(DEGREE_UNIT)){
            this.defaultBuffer =  this.defaultBuffer * M_TO_DEGREE;
        }
    }

    /**
     * Initializes feature filter (highlight)
     *
     * @param featureIds
     *
     * @return filter
     */
    public Filter initFeatureIdFilter(List<String> featureIds) {
        if(featureIds == null || featureIds.size() == 0) {
            LOG.error("Failed to create feature filter (missing feature ids)");
            throw new ServiceRuntimeException("Failed to create feature filter (missing feature ids)");
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
     *
     * @return filter
     */
    public Filter initCoordinateFilter(Coordinate coordinate, String srs, WFSLayerStore layer) {
        if (coordinate == null || this.defaultBuffer == 0.0d) {
            LOG.error("Failed to create coordinate filter (coordinate or default buffer is unset)");
            throw new ServiceRuntimeException("Failed to create coordinate filter (coordinate or default buffer is unset)");
        }

        gsf.setSize(getSizeFactor()*this.defaultBuffer);
        gsf.setCentre(coordinate);
        gsf.setNumPoints(CIRCLE_POINTS_COUNT);

        Polygon polygon = gsf.createCircle();

        // transform
        if (this.transform != null) {
            LOG.debug("transforming mapClick", coordinate);
            try {
                polygon = (Polygon) JTS.transform(polygon, this.transform);
            } catch (Exception e) {
                LOG.error(e, "Transforming failed");
                throw new ServiceRuntimeException("Transforming failed for coordinate filter", e.getCause());
            }
        }
        // is reverseXY or long srs name
        polygon = swapXY(polygon, srs, layer);
        Filter filter = ff.intersects(ff.property(layer
                .getGMLGeometryProperty()), ff.literal(polygon));

        System.out.println("coordinate filter success");
        return filter;
    }

    /**
     * Inits filter for select tool (geojson features)
     *
     * @param geoJSONFilter
     *
     * @return filter
     */
    public Filter initGeoJSONFilter(GeoJSONFilter geoJSONFilter, String srs, WFSLayerStore layer) {
        if(geoJSONFilter == null || geoJSONFilter.getFeatures() == null || this.defaultBuffer == 0.0d) {
            LOG.error("Failed to create geoJSON filter (invalid JSON or default buffer unset)");
            return null;
        }
        String targetGeometryProperty = layer.getGMLGeometryProperty();
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
                    polygon = (Polygon) lineGeom.buffer(distance);
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
                        LOG.error(e, "Transforming failed");
                        throw new ServiceRuntimeException("Transforming failed for geojson filter", e.getCause());
                    }
                }
                // is reverseXY or long srs name
                polygon = swapXY(polygon, srs, layer);
                tmpFilter = ff.intersects(ff.property(targetGeometryProperty), ff.literal(polygon));

                geometryFilters.add(tmpFilter);
            }
        } catch (JSONException e) {
            LOG.error(e, "Reading geojson data failed");
            throw new ServiceRuntimeException("Reading geojson data failed for geojson filter", e.getCause());
        } catch (Exception e) {
            LOG.error(e, "Generating geometries from geojson failed");
            throw new ServiceRuntimeException("Generating geometries from geojson failed for geojson filter", e.getCause());
        }

        if(geometryFilters.size() > 1) {
            filter = ff.or(geometryFilters);
        } else {
            filter = tmpFilter;
        }

        return filter;
    }
    /**
     * Inits filter for property filter select
     *
     * @param session  WFS transport service session data
     * @param bounds  Map bounds
     * @param layer   WFS layer metadata
     *
     * @return filter
     */
    public Filter initPropertyFilter( final SessionStore session,
                                     final List<Double> bounds, final WFSLayerStore layer) {

        PropertyFilter propertyFilter = session.getPropertyFilter();
        Location location;
        if(bounds != null) {
            location = new Location(session.getLocation().getSrs());
            location.setBbox(bounds);
        } else {
            location = session.getLocation();
        }
        if(propertyFilter == null ) {
            LOG.error("Failed to create property filter (invalid JSON for property filter)");
            throw new ServiceRuntimeException("Failed to create property filter (invalid JSON for property filter)");
        }
        // Bbox filter is always on
        Filter filter = initBBOXFilter(location, layer, true);
        if(filter == null ) {
            LOG.error("Failed to create bbox filter for property filter");
            return null;
        }
        // parse property filters
        Filter propertyFilters = WFSFilterBuilder.parseWfsJsonFilter(propertyFilter.getPropertyFilter(), null, null);
        if( propertyFilters != null ) {
            filter = ff.and(filter, propertyFilters);
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
     * @param forRequest true; filter will be used for wfs service request / false: for features
     * @return filter
     */
    public static Filter initBBOXFilter(Location location, WFSLayerStore layer, boolean forRequest) {
        if(location == null || layer == null) {
            LOG.error("Failed to create BBOX filter (location or layer is unset)");
            return null;
        }
        // Use layer.getSRSName() for srsName, if transform must be supported
        String srsName = location.getSrs();
        ReferencedEnvelope envelope = location.getEnvelope();
        envelope = location.getTransformEnvelope(envelope, srsName, true);
        // is reverseXY or use long srs name
        if(forRequest){
            envelope = swapXY(envelope, srsName, layer);
        }
        srsName = layer.isLongSrsName(srsName) ? ProjectionHelper.longSyntaxEpsg(srsName) : srsName;
        if(envelope == null){
            throw new ServiceRuntimeException(
                    "Failed to create BBOX filter - layer: " + layer.getLayerId() + " Crs: " + srsName );
        }
        Filter filter = ff.bbox(ff.property(layer.getGMLGeometryProperty()),
                envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY(), srsName);

        return filter;
    }

    /**
     * Initializes enlarged bounding box filter (normal)
     *
     * @param location
     *
     * @return filter
     */
    public static Filter initEnlargedBBOXFilter(Location location, WFSLayerStore layer) {
        if(location == null || layer == null) {
            LOG.error("Failed to create BBOX filter (location or layer is unset)");
            return null;
        }
        // Use layer.getSRSName() for srsName, if transform must be supported
        String srsName = location.getSrs();
        ReferencedEnvelope enlargedEnvelope = location.getEnlargedEnvelope();
        enlargedEnvelope = location.getTransformEnvelope(enlargedEnvelope, srsName, true);
        // is reverseXY or long srs name
        enlargedEnvelope = swapXY(enlargedEnvelope, srsName, layer);
        if(enlargedEnvelope == null){
            throw new ServiceRuntimeException(
                    "Failed to create enlarged BBOX filter - layer: " + layer.getLayerId() + " Crs: " + srsName );
        }
        srsName = layer.isLongSrsName(srsName) ? ProjectionHelper.longSyntaxEpsg(srsName) : srsName;
        Filter filter = ff.bbox(ff.property(layer.getGMLGeometryProperty()),
                enlargedEnvelope.getMinX(), enlargedEnvelope.getMinY(), enlargedEnvelope.getMaxX(),enlargedEnvelope.getMaxY(), srsName);

        return filter;
    }

    /**
     * Swap xy order, if reverseXY setup in layer attributes
     * Set srsName in long syntax, if longSrsName setup in layer attributes
      * @param enlargedEnvelope
     * @param srsName
     * @param layer
     * @return
     */
    public static  ReferencedEnvelope swapXY(ReferencedEnvelope enlargedEnvelope, String srsName, WFSLayerStore layer) {
        enlargedEnvelope = layer.isReverseXY(srsName) ?
                new ReferencedEnvelope(enlargedEnvelope.getMinY(), enlargedEnvelope.getMaxY(),enlargedEnvelope.getMinX(), enlargedEnvelope.getMaxX(),
                        enlargedEnvelope.getCoordinateReferenceSystem()) : enlargedEnvelope;
        enlargedEnvelope = layer.isLongSrsName(srsName) ?
                new ReferencedEnvelope(enlargedEnvelope.getMinX(), enlargedEnvelope.getMaxX(),enlargedEnvelope.getMinY(),
                        enlargedEnvelope.getMaxY(), ProjectionHelper.longSyntaxCrs(srsName)) : enlargedEnvelope;
        return enlargedEnvelope;
    }

    /**
     *  Swap polygon xy order, if reverseXY setup in layer attributes
     * @param polygon
     * @param srsName
     * @param layer
     * @return
     */
    public static Polygon swapXY(Polygon polygon, String srsName, WFSLayerStore layer) {
        if(layer.isReverseXY(srsName)){
            ProjectionHelper.flipFeatureYX(polygon);
        }
        return polygon;
    }
}
