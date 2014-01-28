package fi.nls.oskari.control.wfs;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.domain.permissions.TermsOfUseUser;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.mml.portti.service.db.permissions.TermsOfUseUserService;
import fi.mml.portti.service.db.permissions.TermsOfUseUserServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceIbatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// This is not used for now and needs migrating to the new wfs and maplayer structures
@OskariActionRoute("GetFeatureDataXML")
public class FeatureDataDownloadHandler extends ActionHandler {

    private static PermissionsService permissionsService = new PermissionsServiceIbatisImpl();
    private static TermsOfUseUserService termsOfUseUserService = new TermsOfUseUserServiceIbatisImpl();

    private static final OskariLayerService mapLayerService = new OskariLayerServiceIbatisImpl();

    public static final String KEY_LAYERID = "layerId";
    public static final String KEY_DL_PERM = "downloadPermission";
    public static final String KEY_TOU_URL = "termsOfUseAccept";
    public static final String KEY_LICENSE = "license";
    public static final String KEY_LANGUAGE = "Language";
    public static final String KEY_DL_PARM = "downloadUrlParameters";
    public static final String PREFIX_TOU_DL = "featuredata.download.termsofuse.url.";
    public static final String PREFIX_WFS = "wfs+";
    public static final String SUFFIX_ACCEPT = "_accept";
    public static final String STR_NO = "no";
    public static final String STR_TRUE = "true";

    // TODO: check the logic here, seems a bit off, no response on some paths/errors
    public void handleAction(final ActionParameters params) {

        JSONObject root = new JSONObject();
        String layerId = params.getRequest().getParameter(KEY_LAYERID);

        User user = params.getUser();
        List<String> downloadPermissions = permissionsService
                .getResourcesWithGrantedPermissions(
                        Permissions.RESOURCE_TYPE_MAP_LAYER, user,
                        Permissions.PERMISSION_TYPE_DOWNLOAD);
        if (downloadPermissions == null)
            throw new RuntimeException("Could not get download permissions");

        // 1. Check permissions
        if (!isPermission(downloadPermissions, layerId)) {
            try {
                root.put(KEY_DL_PERM, STR_NO);
            } catch (JSONException jsonex) {
                throw new RuntimeException("Could not set dl perms in JSON");
            }
            ResponseHelper.writeResponse(params,  root.toString());
            return; // TODO: No permission
        }

        // 2. Check if agreed to download license
        if (!isTermsOfUseAgreed(user.getId(), params.getRequest().getParameter(KEY_TOU_URL))) {
            Locale locale = params.getLocale();
            
            String language = locale.getLanguage();
            
            String url;
            try {
                url = PropertyUtil.get(PREFIX_TOU_DL + language);
            } catch (Exception e) {
                throw new RuntimeException("Could not get URL");
            }
            if (url == null)
                throw new RuntimeException("Could not get URL");

            JSONObject permissionCheck = new JSONObject();
            try {
                permissionCheck.put(KEY_TOU_URL, url);
            } catch (JSONException jsonex) {
                throw new RuntimeException("Could not set URL in JSON");
            }
            try {
                root.put(KEY_LICENSE, permissionCheck);
            } catch (JSONException jsonex) {
                throw new RuntimeException("Could not set license in JSON");
            }

            ResponseHelper.writeResponse(params, root.toString());

            return;
        }

        // 3. Check use license
        try {
            if (!isUseLicenseAgreed(params)) {
                return;
            }
        } catch (JSONException jsonex) {
            throw new RuntimeException("Could not check license ack");
        }

        String minX = params.getRequest().getParameter("bbox_min_x");
        String minY = params.getRequest().getParameter("bbox_min_y");
        String maxX = params.getRequest().getParameter("bbox_max_x");
        String maxY = params.getRequest().getParameter("bbox_max_y");
        String mapWidth = params.getRequest().getParameter("map_width");
        String mapHeight = params.getRequest().getParameter("map_heigh");

        minX = Jsoup.clean(minX, Whitelist.none());
        minY = Jsoup.clean(minY, Whitelist.none());
        maxX = Jsoup.clean(maxX, Whitelist.none());
        maxY = Jsoup.clean(maxY, Whitelist.none());
        mapWidth = Jsoup.clean(mapWidth, Whitelist.none());
        mapHeight = Jsoup.clean(mapHeight, Whitelist.none());

        String xmlCallParameters =
        // "&_MapFull2_WAR_map2portlet_fi.mml.baseportlet.CMD=xml.jsp" +
        "&flow_pm_wfsLayerId=" + layerId + "&flow_pm_bbox_min_x=" + minX
                + "&flow_pm_bbox_min_y=" + minY + "&flow_pm_bbox_max_x=" + maxX
                + "&flow_pm_bbox_max_y=" + maxY + "&flow_pm_map_width="
                + mapWidth + "&flow_pm_map_heigh=" + mapHeight
                + "&actionKey=GET_XML_DATA";
        // xmlCallParameters =
        // "&flow_pm_wfsLayerId=131&flow_pm_bbox_min_x=420714&flow_pm_bbox_min_y=6908629&flow_pm_bbox_max_x=421790&flow_pm_bbox_max_y=6909331&flow_pm_map_width=538&flow_pm_map_heigh=351&actionKey=GET_XML_DATA";

        try {
            root.put(KEY_DL_PARM, xmlCallParameters);
        } catch (JSONException jsonex) {
            throw new RuntimeException("Could not set URL params in JSON");
        }
        ResponseHelper.writeResponse(params, root.toString());
    }

    public static boolean isPermission(final List<String> downloadPermissions,
            String layerId) {
        final OskariLayer ml = mapLayerService.find(Integer.parseInt(layerId));
        final String wmsName = ml.getName();
        for (String id : downloadPermissions) {
            if (id.equals(PREFIX_WFS + wmsName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTermsOfUseAgreed(final long userId,
            final String keyToUrl) {

        TermsOfUseUser termsOfUseUser = termsOfUseUserService
                .find((int) userId);

        if (termsOfUseUser != null)
            return true;

        if (keyToUrl != null) {
            termsOfUseUser = new TermsOfUseUser();
            termsOfUseUser.setTermsOfUseLicenseId(1);
            termsOfUseUser.setUserid((int) userId);
            termsOfUseUser.setUpdate_time(Calendar.getInstance().getTime());
            termsOfUseUserService.insert(termsOfUseUser);
            return true;
        }

        return false;
    }

    public static boolean isUseLicenseAgreed(final ActionParameters params)
            throws JSONException {
        return false;
        /*
        final int wfsLayerId = Integer.parseInt(params.getRequest()
                .getParameter(KEY_LAYERID));

        final WFSLayer wfsLayer = WFSCompatibilityHelper.getLayer(wfsLayerId);

        final List<SelectedFeatureType> featureTypes = wfsLayer
                .getSelectedFeatureTypes();

        final HttpSession ps = params.getRequest().getSession(true);

        String lang = params.getRequest().getParameter(KEY_LANGUAGE);
        if (lang == null)
            lang = PropertyUtil.getDefaultLanguage();

        for (SelectedFeatureType featureType : featureTypes) {
            String id = String.valueOf(featureType.getId());

            if (params.getRequest().getParameter(id + SUFFIX_ACCEPT) != null)
                ps.setAttribute(id, STR_TRUE);

            if (ps.getAttribute(id) == null) {
                JSONObject root = new JSONObject();
                JSONObject json = new JSONObject();
                JSONObject licenseJson = new JSONObject(
                        featureType.getLicenseJson());

                json.put(id + SUFFIX_ACCEPT, licenseJson.get(lang));
                root.put(KEY_LICENSE, json);
                ResponseHelper.writeResponse(params, root.toString());
                return false;
            }
        }
        return true;
       */
    }
}
