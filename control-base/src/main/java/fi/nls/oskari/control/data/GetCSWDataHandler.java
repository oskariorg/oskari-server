package fi.nls.oskari.control.data;

import com.vividsolutions.jts.geom.Geometry;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.oskari.csw.service.CSWService;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.rating.RatingService;
import fi.nls.oskari.rating.RatingServiceMybatisImpl;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by TMIKKOLAINEN on 1.9.2014.
 */
@OskariActionRoute("GetCSWData")
public class GetCSWDataHandler extends ActionHandler {
    private static final String LANG_PARAM = "lang";
    private static final String UUID_PARAM = "uuid";
    // TODO get baseUrl from properties
    private String baseUrl = PropertyUtil.getOptional("service.metadata.url");
    private String metadataRatingType = PropertyUtil.getOptional("service.metadata.rating");
    private String licenseUrlPrefix = PropertyUtil.getOptional("search.channel.METADATA_CATALOGUE_CHANNEL.licenseUrlPrefix");
    public static final String KEY_LICENSE = "license";
    public static final String KEY_ONLINERESOURCES = "onlineResources";
    public static final String KEY_URL = "url";
    public static final String KEY_IDENTIFICATIONS = "identifications";
    public static final String KEY_ENVELOPES = "envelopes";
    public static final String KEY_SRS = "srs";
    public static final String KEY_GEOM = "geom";
    public static final String KEY_SOUTHBOUNDLATITUDE = "southBoundLatitude";
    public static final String KEY_WESTBOUNDLONGITUDE = "westBoundLongitude";
    public static final String KEY_EASTBOUNDLONGITUDE = "eastBoundLongitude";
    public static final String KEY_NORTHBOUNDLATITUDE = "northBoundLatitude";

    /*ratings*/
    public static final String KEY_RATING = "score";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_ADMIN_RATING = "latestAdminRating";

    private final RatingService ratingService = new RatingServiceMybatisImpl();

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        if (baseUrl == null) {
            throw new ActionException("Service not configured.");
        }
        final String uuid = params.getRequiredParam(UUID_PARAM);
        // TODO use default lang if not found?
        final String lang = params.getRequiredParam(LANG_PARAM);
        CSWIsoRecord record;
        CSWService service;
        try {
            service = new CSWService(baseUrl);
        } catch (Exception e) {
            throw new ActionException("Failed to initialize CSWService:" + e.getMessage());
        }
        try {
            record = service.getRecordById(uuid, lang);
        } catch (Exception e) {
            throw new ActionException("Failed to query service: " + e.getMessage());
        }
        JSONObject result;
        if (record != null) {
            result = record.toJSON();
        } else {
            result = new JSONObject();
        }

        addGeometry(result, params);

        if (licenseUrlPrefix != null) {
            addLicenseUrl(result);
        }

        if (metadataRatingType != null && uuid != null) {
            addRatings(result, uuid);
        }

        ResponseHelper.writeResponse(params, result);
    }

    private void addLicenseUrl(JSONObject result) {
        // Check OnlineResource URL: if matches securitymanagerin url -> setup KEY_LICENSE = service url
        JSONArray onlineResources = JSONHelper.getJSONArray(result, KEY_ONLINERESOURCES);
        if (onlineResources != null) {
            for (int i = 0; i < onlineResources.length(); i++) {
                String url = JSONHelper.getStringFromJSON(JSONHelper.getJSONObject(onlineResources, i), KEY_URL, null);
                if(licenseUrlPrefix != null && url != null && url.startsWith(licenseUrlPrefix)) {
                    JSONHelper.putValue(result, KEY_LICENSE, url);
                    break;
                }
            }
        }
    }

    private void addGeometry(JSONObject result, ActionParameters params) {
        try {
            JSONArray identifications = JSONHelper.getJSONArray(result, KEY_IDENTIFICATIONS);
            JSONObject jsonObj = JSONHelper.getJSONObject(identifications, 0);
            JSONArray envelopes = JSONHelper.getJSONArray(jsonObj, KEY_ENVELOPES);
            String geom = getWKT(JSONHelper.getJSONObject(envelopes, 0), params.getHttpParam(KEY_SRS));
            JSONHelper.putValue(result, KEY_GEOM, geom);
        } catch(Exception e) {
        }
    }

    private void addRatings(JSONObject result, String uuid) {

        String[] rating = ratingService.getAverageRatingFor(metadataRatingType, uuid);
        if(rating != null && !rating[1].equals("0")) {
            JSONHelper.putValue(result, KEY_RATING, rating[0]);
            JSONHelper.putValue(result, KEY_AMOUNT, rating[1]);
        }

        String adminRole = Role.getAdminRole().getName();
        String adminRating = ratingService.findLatestAdminRating(metadataRatingType, uuid, adminRole);
        JSONHelper.putValue(result, KEY_ADMIN_RATING, adminRating);
    }
    private String getWKT(JSONObject item, final String targetSRS) {
        String sourceSRS = WKTHelper.PROJ_EPSG_4326;
        // check if we have values
        if((JSONHelper.get(item, KEY_SOUTHBOUNDLATITUDE) == null) ||
                (JSONHelper.get(item, KEY_WESTBOUNDLONGITUDE) == null) ||
                (JSONHelper.get(item, KEY_EASTBOUNDLONGITUDE) == null) ||
                (JSONHelper.get(item, KEY_NORTHBOUNDLATITUDE) == null)) {
            return null;
        }
        // transform points to map projection and create a WKT bbox
        try {
            Point p1 = null;
            Point p2 = null;
            if (ProjectionHelper.isFirstAxisNorth(CRS.decode(sourceSRS))) {
                p1 = ProjectionHelper.transformPoint(JSONHelper.get(item, KEY_SOUTHBOUNDLATITUDE).toString(), JSONHelper.get(item, KEY_WESTBOUNDLONGITUDE).toString(), sourceSRS, targetSRS);
                p2 = ProjectionHelper.transformPoint(JSONHelper.get(item, KEY_NORTHBOUNDLATITUDE).toString(), JSONHelper.get(item, KEY_EASTBOUNDLONGITUDE).toString(), sourceSRS, targetSRS);
            } else {
                p1 = ProjectionHelper.transformPoint(JSONHelper.get(item, KEY_WESTBOUNDLONGITUDE).toString(), JSONHelper.get(item, KEY_SOUTHBOUNDLATITUDE).toString(), sourceSRS, targetSRS);
                p2 = ProjectionHelper.transformPoint(JSONHelper.get(item, KEY_EASTBOUNDLONGITUDE).toString(), JSONHelper.get(item, KEY_NORTHBOUNDLATITUDE).toString(), sourceSRS, targetSRS);
            }
            return WKTHelper.getBBOX(p1.getLon(), p1.getLat(), p2.getLon(), p2.getLat());
        } catch(Exception e){
        }
        return null;
    }


}
