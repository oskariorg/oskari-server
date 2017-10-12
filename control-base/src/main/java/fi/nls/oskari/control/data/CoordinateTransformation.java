package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.DefaultPointTransformer;
import fi.nls.oskari.map.geometry.PointTransformer;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.StringTokenizer;

import static fi.nls.oskari.control.ActionConstants.*;

/**
 * Transforms coordinates from projection to another. Transformation class can be configured
 * with property 'projection.library.class' (defaults to fi.nls.oskari.map.geometry.ProjectionHelper).
 *
 * Takes 'lan', 'lot', 'srs' and 'targetSRS' parameters and returns a JSONObject with transformed result:
 * {
 *     lan: 123,
 *     lot : 456,
 *     srs : "EPSG:789"
 * }
 */
@OskariActionRoute("CoordinateTransformation")
public class CoordinateTransformation extends ActionHandler {

    private static final Logger LOG = LogFactory.getLogger(CoordinateTransformation.class);
    private static final String URL = "http://193.166.24.38/coordtrans/CoordTrans";

    static final String SOURCE_CRS = "sourceCrs";
    static final String HEIGHT_CRS = "heightCrs";
    static final String TARGET_CRS = "targetCrs";
    static final String COORDINATES = "coords";
    static final String HEIGHT = "height";

    private PointTransformer service = null;

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void handleAction(final ActionParameters params)
            throws ActionException {

        //http://193.166.24.38/coordtrans/CoordTrans
        //?sourceCRS=EPSG:2393,EPSG:5717&targetCRS=EPSG:4937&coords=6674658,3363923,23.5;6674739,3363820,36.3
        final StringBuffer request = new StringBuffer();
        request.append("?sourceCRS=" + params.getRequiredParam(SOURCE_CRS));
        String heightCrs = params.getHttpParam(HEIGHT_CRS);
        if (heightCrs != null && !heightCrs.isEmpty()) {
            request.append("," + heightCrs);
        }
        request.append("&targetCRS=" + params.getRequiredParam(TARGET_CRS));
        request.append("&coords=");

        final String coordinates = params.getRequiredParam(COORDINATES);
        try {
            final JSONArray coordinateArray = new JSONArray(coordinates);
            for (int i = 0; i < coordinateArray.length(); ++i) {
                JSONObject coordinate = (JSONObject) coordinateArray.get(i);
                request.append(coordinate.get(PARAM_LON));
                request.append(","+coordinate.get(PARAM_LAT));
                if (heightCrs != null && !heightCrs.isEmpty()) {
                    request.append(","+coordinate.get(HEIGHT));
                }
                request.append(";");
            }
            HttpURLConnection conn = IOHelper.getConnection(URL + request.toString());
            String result = IOHelper.readString(conn);

            StringTokenizer coordinatesTokenizer = new StringTokenizer(result.toString(), ";");
            JSONArray response = new JSONArray();
            while(coordinatesTokenizer.hasMoreElements()) {
                StringTokenizer coordinateTokenizer = new StringTokenizer(
                        coordinatesTokenizer.nextToken(), ",");
                while(coordinateTokenizer.hasMoreElements()) {
                    JSONObject coordinateObject = new JSONObject();
                    coordinateObject.put(PARAM_LON, coordinateTokenizer.nextElement());
                    coordinateObject.put(PARAM_LAT, coordinateTokenizer.nextElement());
                    if (heightCrs != null && !heightCrs.isEmpty()) {
                        coordinateObject.put(HEIGHT, coordinateTokenizer.nextElement());
                    }
                    response.put(coordinateObject);
                }
            }

            ResponseHelper.writeResponse(params, response);

        } catch (Exception e) {
            throw new ActionParamsException(e.getMessage());
        }
    }
}
