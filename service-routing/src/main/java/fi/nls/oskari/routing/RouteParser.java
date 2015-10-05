package fi.nls.oskari.routing;

import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Initial parsing for WMTS capabilities in a way that admin-layerselector can interpret it.
 */
public class RouteParser {


    private static final Logger LOG = LogFactory.getLogger(RouteParser.class);

    public JSONObject parseGeoJson(Route params, String targetSRS) {

        LOG.debug("----------------------Trying to parse geoJson");

        JSONObject featureCollection = new JSONObject();

        List<Leg> legs = params.getLegs();

        try {
            featureCollection.put("type", "FeatureCollection");
            JSONArray featureList = new JSONArray();

            for (Leg leg: legs) {
                JSONObject line = new JSONObject();
                line.put("type", "LineString");

                List<Shape> shapes = leg.getShape();

                JSONArray coordinates = new JSONArray();
                for (Shape shape : shapes) {
                    //transform coordinates to the original system
                    String currentSRS = PropertyUtil.get("routing.srs");
                    Point coordsInAppSRS = ProjectionHelper.transformPoint(shape.getY(), shape.getX(), currentSRS, targetSRS);

                    JSONArray coordinate = new JSONArray("[" + coordsInAppSRS.getLonToString() + "," + coordsInAppSRS.getLatToString() + "]");
                    coordinates.put(coordinate);
                }

                line.put("coordinates", coordinates);
                JSONObject feature = new JSONObject();
                feature.put("type", "Feature");
                feature.put("geometry", line);

                JSONObject properties = new JSONObject();
                properties.put("transportType", leg.getType());
                feature.put("properties", properties);

                featureList.put(feature);
            }
            featureCollection.put("features", featureList);

        } catch (JSONException e) {
            LOG.error("can't save json object: " + e.toString());
        }
        return featureCollection;
    }

    public JSONObject parseRoute (Route params) {

        JSONObject instructions = new JSONObject();
        JSONArray legList = new JSONArray();

        try {
            Double routeLength = params.getLength();
            Number routeDuration = params.getDuration();

            instructions.put("length", routeLength);
            instructions.put("duration", routeDuration);

            List<Leg> legs = params.getLegs();

            for (Leg leg : legs) {
                JSONObject routeLeg = new JSONObject();

                Integer legLength = leg.getLength();
                routeLeg.put("length", legLength);

                Number legDuration = leg.getDuration();
                routeLeg.put("duration", legDuration);

                // transport type can be "walk" or number
                String legTransportType = leg.getType();
                routeLeg.put("type", legTransportType);

                String lineCode = leg.getCode();
                if (lineCode != null) {
                    routeLeg.put("code", lineCode);
                }
                legList.put(routeLeg);
            }
            instructions.put("legs", legList);
        } catch (JSONException e) {
            LOG.error("couldn,t parse instructions" + e.toString());
        }

        return instructions;
    }



}
