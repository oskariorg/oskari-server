package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.oskari.csw.service.CSWService;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.map.geometry.WKTHelper;
import fi.nls.oskari.rating.RatingService;
import fi.nls.oskari.rating.RatingServiceMybatisImpl;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by TMIKKOLAINEN on 1.9.2014.
 */
@OskariActionRoute("GetCSWData")
public class GetCSWDataHandler extends ActionHandler {
    private final Logger log = LogFactory.getLogger(this.getClass());
    
    private static final String LANG_PARAM = "lang";
    private static final String UUID_PARAM = "uuid";
    private final String baseUrl = PropertyUtil.getOptional("service.metadata.url");
    private final String imgUrl = PropertyUtil.getOptional("service.metadata.imgurl");
    private final String metadataRatingType = PropertyUtil.getOptional("service.metadata.rating");
    private final String licenseUrlPrefix = PropertyUtil.getOptional("search.channel.METADATA_CATALOGUE_CHANNEL.licenseUrlPrefix");
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

    /* ratings */
    public static final String KEY_RATING = "score";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_ADMIN_RATING = "latestAdminRating";

    private final RatingService ratingService = new RatingServiceMybatisImpl();

    /* images */
    private static final String PROPERTY_IMAGE_PREFIX = "search.channel.METADATA_CATALOGUE_CHANNEL.image.url.";
    private final Map<String, String> imageURLs = new HashMap<String, String>();
    
    @Override
    public void init() {
        super.init();
        final List<String> imageKeys = PropertyUtil.getPropertyNamesStartingWith(PROPERTY_IMAGE_PREFIX);
        final int imgPrefixLen = PROPERTY_IMAGE_PREFIX.length();
        for(String key : imageKeys) {
            final String langCode = key.substring(imgPrefixLen);
            imageURLs.put(langCode, PropertyUtil.get(key));
        }
    }
    
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
            prefixImageFilenames(record, uuid, lang);
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
    
    private void prefixImageFilenames(CSWIsoRecord record, final String uuid, final String locale) {
        // This only works for GN2 for paikkatietohakemisto.fi
        // GN2-style: http://geonetwork.nls.fi/geonetwork/srv/fi/resources.get.uuid?access=public&uuid=7ac131b9-a307-4aa1-b27a-009e91f6bd45&fname=Pohjak_Ylihrm_s.png
        // GN3-style: http://www.paikkatietohakemisto.fi/geonetwork/srv/api/records/7ac131b9-a307-4aa1-b27a-009e91f6bd45/attachments/Pohjak_Ylihrm_s.png
        String url = imageURLs.get(locale);        
        if (url == null) {
            url = PropertyUtil.get(PROPERTY_IMAGE_PREFIX + "en");
        }
        String prefix = url + "&uuid=" + uuid + "&fname=";
        List<CSWIsoRecord.Identification> is = record.getIdentifications();
        for (CSWIsoRecord.Identification i : is) {
            List<CSWIsoRecord.BrowseGraphic> gs = i.getBrowseGraphics();
            for (CSWIsoRecord.BrowseGraphic g : gs) {
                String fname = g.getFileName();
                final boolean replaceImageURL = fname != null
                        && !fname.isEmpty()
                        && !fname.startsWith("http://");

                if (replaceImageURL) {
                    g.setFileName(prefix + fname);
                }
            }
        }
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
        if ((JSONHelper.get(item, KEY_SOUTHBOUNDLATITUDE) == null) ||
                (JSONHelper.get(item, KEY_WESTBOUNDLONGITUDE) == null) ||
                (JSONHelper.get(item, KEY_EASTBOUNDLONGITUDE) == null) ||
                (JSONHelper.get(item, KEY_NORTHBOUNDLATITUDE) == null)) {
            return null;
        }
        // transform points to map projection and create a WKT bbox
        try {

            Point p1 = ProjectionHelper.transformPoint(JSONHelper.get(item, KEY_WESTBOUNDLONGITUDE).toString(), JSONHelper.get(item, KEY_SOUTHBOUNDLATITUDE).toString(), sourceSRS, targetSRS);
            Point p2 = ProjectionHelper.transformPoint(JSONHelper.get(item, KEY_EASTBOUNDLONGITUDE).toString(), JSONHelper.get(item, KEY_NORTHBOUNDLATITUDE).toString(), sourceSRS, targetSRS);
            if (p1 != null && p2 != null) {
                return WKTHelper.getBBOX(p1.getLon(), p1.getLat(), p2.getLon(), p2.getLat());
            }
            return WKTHelper.getBBOX(p1.getLon(), p1.getLat(), p2.getLon(), p2.getLat());
        } catch (Exception e) {
        }
        return null;
    }


}
