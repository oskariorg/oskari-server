package fi.nls.oskari.map.data.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import fi.nls.oskari.log.LogFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geojson.geom.GeometryJSON;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Geometry;

import com.vividsolutions.jts.util.GeometricShapeFactory;
import fi.mml.portti.service.ogc.executor.GetFeaturesWorker;
import fi.mml.portti.service.ogc.executor.WFSResponseCapsule;
import fi.mml.portti.service.ogc.executor.WfsExecutorService;
import fi.nls.oskari.domain.map.wfs.FeatureType;
import fi.nls.oskari.domain.map.wfs.SelectedFeatureType;
import fi.nls.oskari.domain.map.wfs.WFSLayer;
import fi.nls.oskari.log.Logger;

public class WFSFeatureService {

    private final int PARAM_CIRCLE_PNTS = 40;
    private final String OSKARI_GEOM_POINT = "POINT";
    private final String OSKARI_GEOM_LINE = "LINE";
    private final String OSKARI_GEOM_POLYGON = "POLYGON";
    private final String OSKARI_GEOM_CIRCLE = "CIRCLE";
    private final String OSKARI_GEOM_RECTANGLE = "RECTANGLE";
    private final String GT_GEOM_POINT = "POINT";
    private final String GT_GEOM_LINESTRING = "LINESTRING";
    private final String GT_GEOM_POLYGON = "POLYGON";

    private Logger log = LogFactory.getLogger(WFSFeatureService.class);
    // GetGeoPointDataService

    public JSONArray getWFSFeatures(final int zoom, JSONObject geojs,
            final int layerId) {
        final double lat = 0.0;
        final double lon = 0.0;
        return getWFSFeatures(lat, lon, zoom, geojs, layerId);
    }

    public JSONArray getWFSFeatures(final double lat, final double lon,
            final int zoom, JSONObject geojs, final int layerId) {
        final WFSLayer wfsLayer = WFSCompatibilityHelper.getLayer(layerId);
        final List<Future<WFSResponseCapsule>> futures = new ArrayList<Future<WFSResponseCapsule>>();
        final Map<String, FeatureType> futureFeatureTypes = new HashMap<String, FeatureType>();

        final Coordinate coord = new Coordinate(lon, lat);

        final GeometryFactory gf = JTSFactoryFinder.getGeometryFactory(GeoTools
                .getDefaultHints());

        final GeometricShapeFactory gsf = new GeometricShapeFactory(gf);
        final double distance = 2 * Math.pow(2, (12 - zoom));
        gsf.setSize(distance);
        gsf.setCentre(coord);
        gsf.setNumPoints(10);
        final Polygon circle1 = gsf.createCircle();
        Polygon searchGeom = null;
        if (geojs != null && geojs.has("features")) {
            // Use geojson geometry and process search Polygon in all cases

            searchGeom = getSearchPolygon(gf, gsf, geojs, distance);

        } else {
            searchGeom = circle1;
        }
        if (searchGeom == null)
            searchGeom = circle1;
        // gsf.setSize(1.5 * Math.pow(2, (12 - zoom)));
        // Polygon circle2 = gsf.createCircle();

        final FilterFactory2 filterF = CommonFactoryFinder
                .getFilterFactory2(GeoTools.getDefaultHints());

        for (final SelectedFeatureType selFt : wfsLayer
                .getSelectedFeatureTypes()) {
            final FeatureType ft = selFt.getFeatureType();
            final String bboxparm = ft.getBboxParameterName();

            // Dwithin is not supported on arcgis
            // Filter filter =
            // filterF.dwithin(filterF.property(bboxparm),
            // filterF.literal(point),
            // Math.pow(2, (12 - zoom)), "m");

            // Beyond is not supported on arcgis
            // Filter filter =
            // filterF.not(filterF.beyond(filterF.property(bboxparm),
            // filterF.literal(point),
            // Math.pow(2, (12 - zoom)), "m"));

            // Works except for area features
            // Filter filter =
            // filterF.or(filterF.intersects(filterF.property(bboxparm),
            // filterF.literal(circle1)),
            // filterF.within(filterF.property(bboxparm),
            // filterF.literal(circle2)));
            final Filter filter = filterF.intersects(
                    filterF.property(bboxparm), filterF.literal(searchGeom));

            final GetFeaturesWorker worker = new GetFeaturesWorker(
                    GetFeaturesWorker.PARSER_TYPE_JSON_OBJECT, selFt, filter,
                    true);
            final Future<WFSResponseCapsule> future = WfsExecutorService
                    .schedule(worker);
            futures.add(future);
            futureFeatureTypes.put(future.toString(), ft);
        }

        final JSONArray features = new JSONArray();
        for (final Future<WFSResponseCapsule> future : futures) {
            try {
                final WFSResponseCapsule capsule = future.get();
                final FeatureType ft = futureFeatureTypes
                        .get(future.toString());
                if (ft == null) {
                    log
                            .warn(
                                    "Error in logic, could not find FeatureType for future",
                                    future);
                } else {
                    features.put(capsule.getJsonObject());
                }

            } catch (Exception e) {
                log.error(e, "Failed to process Feature", future);
            }
        }

        return features;
    }

    /**
     * Creates search polygon geometry
     * Input geometry is in geoJSON object and type is {POINT | LINE | POLYGON |
     * CIRCLE | RECTANGLE}
     * 
     * @param gf
     * @param gsf
     * @param geojs
     *            geoJSON data for to create search geometry
     * @param defdistance
     *            Default buffer distance around geometry
     * @return
     */
    private Polygon getSearchPolygon(GeometryFactory gf,
            GeometricShapeFactory gsf, JSONObject geojs, double defdistance) {
        Polygon searchGeom = null;
        LineString lineGeom = null;
        Point pointGeom = null;
        try {

            JSONArray gfeatures = (JSONArray) geojs.get("features");
            JSONObject gfeature = (JSONObject) gfeatures.get(0);
            JSONObject geometry = (JSONObject) gfeature.get("geometry");
            GeometryJSON g = new GeometryJSON(3);
            JSONObject properties = (JSONObject) gfeature.get("properties");
            String geojs_type = properties.optString("geom_type", "")
                    .toUpperCase();
            String sdistance = properties.optString("buffer_radius", "0");
            double distance = Double.parseDouble(sdistance);
            if (distance == 0)
                distance = defdistance;
            String geom_type = geometry.getString("type").toUpperCase();
            if (geom_type.equals(GT_GEOM_POLYGON)) {

                searchGeom = g.readPolygon(geometry.toString());

            } else if (geom_type.equals(GT_GEOM_LINESTRING)) {

                if (geojs_type.equals(OSKARI_GEOM_RECTANGLE)) {

                    lineGeom = g.readLine(geometry.toString());

                    Coordinate[] exterior = new Coordinate[5];
                    exterior[0] = lineGeom.getCoordinateN(0);
                    exterior[1] = new Coordinate(exterior[0].x, exterior[2].y);
                    exterior[2] = lineGeom.getCoordinateN(1);
                    exterior[3] = new Coordinate(exterior[2].x, exterior[0].y);
                    exterior[4] = exterior[0];

                    LinearRing shell = gf.createLinearRing(exterior);

                    searchGeom = gf.createPolygon(shell, null);
                } else {
                    lineGeom = g.readLine(geometry.toString());
                    Geometry gtgeom = (Geometry) lineGeom;
                    searchGeom = (Polygon) gtgeom.buffer(distance);
                }

            } else if (geom_type.equals(GT_GEOM_POINT)) {

                if (geojs_type.equals(OSKARI_GEOM_POINT)
                        || geojs_type.isEmpty()) {
                    pointGeom = g.readPoint(geometry.toString());

                    gsf.setSize(distance);
                    gsf.setCentre(pointGeom.getCoordinate());
                    gsf.setNumPoints(10);
                    searchGeom = gsf.createCircle();
                } else if (geojs_type.equals(OSKARI_GEOM_CIRCLE)) {

                    pointGeom = g.readPoint(geometry.toString());

                    gsf.setSize(distance);
                    gsf.setCentre(pointGeom.getCoordinate());
                    gsf.setNumPoints(PARAM_CIRCLE_PNTS);
                    searchGeom = gsf.createCircle();
                }
            }
            return searchGeom;
        } catch (Exception e) {
            log.error(e, "Failed to process Polygon via geoJson");
        }
        return null;
    }
}
