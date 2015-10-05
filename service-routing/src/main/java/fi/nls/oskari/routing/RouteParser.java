package fi.nls.oskari.routing;

import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Initial parsing for WMTS capabilities in a way that admin-layerselector can interpret it.
 */
public class RouteParser {


    private static final Logger LOG = LogFactory.getLogger(RouteParser.class);

    public JSONObject parseGeoJson(Itinerary params, String targetSRS) {

        LOG.debug("----------------------Trying to parse geoJson");

        JSONObject featureCollection = new JSONObject();

        List<Leg> legs = params.getLegs();

        try {
            featureCollection.put("type", "FeatureCollection");
            JSONArray featureList = new JSONArray();

            for (Leg leg: legs) {
                JSONObject line = new JSONObject();
                line.put("type", "LineString");

                LegGeometry legGeom = leg.getLegGeometry();
                String encodedPolyLine = legGeom.getPoints();
                JSONArray coordinates = decode(encodedPolyLine, targetSRS);

                line.put("coordinates", coordinates);
                JSONObject feature = new JSONObject();
                feature.put("type", "Feature");
                feature.put("geometry", line);

                JSONObject properties = new JSONObject();
                properties.put("transportType", leg.getMode());
                feature.put("properties", properties);

                featureList.put(feature);
            }
            featureCollection.put("features", featureList);

        } catch (JSONException e) {
            LOG.error("can't save json object: " + e.toString());
        }

        return featureCollection;
    }

    private static JSONArray decode(String pointString, String targetSRS) {
        double lat = 0;
        double lon = 0;

        int strIndex = 0;
        JSONArray coordinates = new JSONArray();
        try {
            final String currentSRS = PropertyUtil.get("routing.srs");

            while (strIndex < pointString.length()) {
                int[] rLat = decodeSignedNumberWithIndex(pointString, strIndex);
                lat = lat + rLat[0] * 1e-5;
                strIndex = rLat[1];

                int[] rLon = decodeSignedNumberWithIndex(pointString, strIndex);
                lon = lon + rLon[0] * 1e-5;
                strIndex = rLon[1];

                Point coordsInAppSRS = ProjectionHelper.transformPoint(lat, lon, currentSRS, targetSRS);
                JSONArray coordinate = new JSONArray("[" + coordsInAppSRS.getLonToString() + "," + coordsInAppSRS.getLatToString() + "]");
                coordinates.put(coordinate);
            }
        } catch (JSONException e){
            LOG.error("can't get points: " + e.toString());
        }

        return coordinates;
    }

    private static int[] decodeSignedNumberWithIndex(String value, int index) {
        int[] r = decodeNumberWithIndex(value, index);
        int sgn_num = r[0];
        if ((sgn_num & 0x01) > 0) {
            sgn_num = ~(sgn_num);
        }
        r[0] = sgn_num >> 1;
        return r;
    }
    private static int[] decodeNumberWithIndex(String value, int index) {

        if (value.length() == 0)
            throw new IllegalArgumentException("string is empty");

        int num = 0;
        int v = 0;
        int shift = 0;

        do {
            v = value.charAt(index++) - 63;
            num |= (v & 0x1f) << shift;
            shift += 5;
        } while (v >= 0x20);

        return new int[] { num, index };
    }


    public JSONObject parseRoute (Route params) {

        JSONObject instructions = new JSONObject();
        /*
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
*/
        return instructions;
    }



}
