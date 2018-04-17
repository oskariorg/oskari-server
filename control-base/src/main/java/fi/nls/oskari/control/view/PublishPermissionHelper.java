package fi.nls.oskari.control.view;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.nls.oskari.analysis.AnalysisHelper;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.map.analysis.service.AnalysisDbService;
import fi.nls.oskari.map.analysis.service.AnalysisDbServiceMybatisImpl;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.permission.domain.Permission;
import fi.nls.oskari.permission.domain.Resource;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.ConversionHelper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.map.userlayer.service.UserLayerDbService;
import org.oskari.service.util.ServiceFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by SMAKINEN on 17.8.2015.
 */
public class PublishPermissionHelper {

    private static final Logger LOG = LogFactory.getLogger(PublishPermissionHelper.class);

    private MyPlacesService myPlaceService = null;
    private AnalysisDbService analysisService = null;
    private UserLayerDbService userLayerService = null;
    private OskariLayerService layerService = null;
    private PermissionsService permissionsService = null;

    private static final String PREFIX_MYPLACES = "myplaces_";
    private static final String PREFIX_ANALYSIS = "analysis_";
    private static final String PREFIX_USERLAYER = "userlayer_";

    public void init() {
        if (myPlaceService == null) {
            setMyPlacesService(OskariComponentManager.getComponentOfType(MyPlacesService.class));
        }

        if (analysisService == null) {
            setAnalysisService(new AnalysisDbServiceMybatisImpl());
        }

        if (userLayerService == null) {
            setUserLayerService(OskariComponentManager.getComponentOfType(UserLayerDbService.class));
        }

        if (permissionsService == null) {
            setPermissionsService(ServiceFactory.getPermissionsService());
        }

        if (layerService == null) {
            setOskariLayerService(ServiceFactory.getMapLayerService());
        }
    }

    public void setMyPlacesService(final MyPlacesService service) {
        myPlaceService = service;
    }

    public void setAnalysisService(final AnalysisDbService service) {
        analysisService = service;
    }

    public void setUserLayerService(final UserLayerDbService service) {
        userLayerService = service;
    }

    public void setPermissionsService(final PermissionsService service) {
        permissionsService = service;
    }
    public void setOskariLayerService(final OskariLayerService service) {
        layerService = service;
    }

    public void setupDrawPermission(final String drawLayerId, final User user) throws ActionException {
        if(!myPlaceService.canModifyCategory(user, drawLayerId)) {
            throw new ActionDeniedException("Trying to publish another users layer as drawlayer!");
        }
        Resource resource = myPlaceService.getResource(drawLayerId);
        if(resource.hasPermission(user, myPlaceService.PERMISSION_TYPE_DRAW)) {
            // clear up any previous DRAW permissions
            resource.removePermissionsOfType(myPlaceService.PERMISSION_TYPE_DRAW);
        }
        try {
            // add DRAW permission for all roles currently in the system
            for(Role role: UserService.getInstance().getRoles()) {
                final Permission perm = new Permission();
                perm.setExternalType(Permissions.EXTERNAL_TYPE_ROLE);
                perm.setExternalId("" + role.getId());
                perm.setType(myPlaceService.PERMISSION_TYPE_DRAW);
                resource.addPermission(perm);
            }
        } catch (Exception e) {
            LOG.error(e, "Error generating DRAW permissions for myplaces layer");
        }
        permissionsService.saveResourcePermissions(resource);
    }


    JSONArray getPublishableLayers(final JSONArray selectedLayers, final User user) throws ActionException {
        if(selectedLayers == null || user == null) {
            throw new ActionParamsException("Could not get selected layers");
        }
        final JSONArray filteredList = new JSONArray();
        LOG.debug("Selected layers:", selectedLayers);

        String userUuid = user.getUuid();
        try {
            for (int i = 0; i < selectedLayers.length(); ++i) {
                JSONObject layer = selectedLayers.getJSONObject(i);
                final String layerId = layer.getString("id");
                if (layerId.startsWith(PREFIX_MYPLACES)) {
                    // check publish right for published myplaces layer
                    if (hasRightToPublishMyPlaceLayer(layerId, userUuid, user.getScreenname())) {
                        filteredList.put(layer);
                    }
                } else if (layerId.startsWith(PREFIX_ANALYSIS)) {
                    // check publish right for published analysis layer
                    if (hasRightToPublishAnalysisLayer(layerId, user)) {
                        filteredList.put(layer);
                    }
                } else if (layerId.startsWith(PREFIX_USERLAYER)) {
                    // check publish rights for user layer
                    if (hasRightToPublishUserLayer(layerId, user)) {
                        filteredList.put(layer);
                    }
                } else if (hasRightToPublishLayer(layerId, user)) {
                    // check publish right for normal layer
                    filteredList.put(layer);
                }
            }
        } catch (Exception e) {
            LOG.error(e, "Error parsing myplaces layers from published layers", selectedLayers);
        }
        LOG.debug("Filtered layers:", filteredList);
        return filteredList;
    }

    private boolean hasRightToPublishMyPlaceLayer(final String layerId, final String userUuid, final String publisherName) {
        final long categoryId = ConversionHelper.getLong(layerId.substring(PREFIX_MYPLACES.length()), -1);
        if (categoryId == -1) {
            LOG.warn("Error parsing layerId:", layerId);
            return false;
        }
        final List<Long> publishedMyPlaces = new ArrayList<Long>();
        publishedMyPlaces.add(categoryId);
        final List<MyPlaceCategory> myPlacesLayers = myPlaceService.getMyPlaceLayersById(publishedMyPlaces);
        for (MyPlaceCategory place : myPlacesLayers) {
            if (place.isOwnedBy(userUuid)) {
                myPlaceService.updatePublisherName(categoryId, userUuid, publisherName); // make it public
                // IMPORTANT! delete layer data from redis so transport will get updated layer data
                JedisManager.del(WFSLayerConfiguration.KEY + layerId);
                return true;
            }
        }
        LOG.warn("Found my places layer in selected that isn't users own or isn't published any more! LayerId:", layerId, "User UUID:", userUuid);
        return false;
    }


    private boolean hasRightToPublishAnalysisLayer(final String layerId, final User user) {
        final long analysisId = AnalysisHelper.getAnalysisIdFromLayerId(layerId);
        if(analysisId == -1) {
            return false;
        }
        final Analysis analysis = analysisService.getAnalysisById(analysisId);
        if (!analysis.getUuid().equals(user.getUuid())) {
            LOG.warn("Found analysis layer in selected that isn't users own! LayerId:", layerId, "User UUID:", user.getUuid(), "Analysis UUID:", analysis.getUuid());
            return false;
        }

        final Set<String> permissionsList = permissionsService.getResourcesWithGrantedPermissions(
                AnalysisLayer.TYPE, user, Permissions.PERMISSION_TYPE_PUBLISH);
        LOG.debug("Analysis layer publish permissions", permissionsList);
        final String permissionKey = "analysis+"+analysis.getId();

        LOG.debug("PublishPermissions:", permissionsList);
        boolean hasPermission = permissionsList.contains(permissionKey);
        if (hasPermission) {
            // write publisher name for analysis
            analysisService.updatePublisherName(analysisId, user.getUuid(), user.getScreenname());
            // IMPORTANT! delete layer data from redis so transport will get updated layer data
            JedisManager.del(WFSLayerConfiguration.KEY + layerId);
        } else {
            LOG.warn("Found analysis layer in selected that isn't publishable any more! Permissionkey:", permissionKey, "User:", user);
        }
        return hasPermission;
    }

    private boolean hasRightToPublishUserLayer(final String layerId, final User user) {
        final long id = ConversionHelper.getLong(layerId.substring(PREFIX_USERLAYER.length()), -1);
        if (id == -1) {
            LOG.warn("Error parsing layerId:", layerId);
            return false;
        }
        final UserLayer userLayer = userLayerService.getUserLayerById(id);
        if (userLayer.isOwnedBy(user.getUuid())) {
            userLayerService.updatePublisherName(id, user.getUuid(), user.getScreenname());
            // IMPORTANT! delete layer data from redis so transport will get updated layer data
            JedisManager.del(WFSLayerConfiguration.KEY + layerId);
            return true;
        } else {
            return false;
        }
    }

    private boolean hasRightToPublishLayer(final String layerId, final User user) {
        // layerId might be external so don't use it straight up
        final OskariLayer layer = layerService.find(layerId);
        if (layer == null) {
            LOG.warn("Couldn't find layer with id:", layerId);
            return false;
        }
        final Long id = Long.valueOf(layer.getId());
        final List<Long> list = new ArrayList<>();
        list.add(id);
        final Map<Long, List<Permissions>> map = permissionsService.getPermissionsForLayers(list, Permissions.PERMISSION_TYPE_PUBLISH);
        List<Permissions> permissions = map.get(id);
        boolean hasPermission = permissionsService.permissionGrantedForRolesOrUser(
                user, permissions, Permissions.PERMISSION_TYPE_PUBLISH);
        if (!hasPermission) {
            LOG.warn("User tried to publish layer with no publish permission. LayerID:", layerId, "- User:", user);
        }
        return hasPermission;
    }
}
