package fi.nls.oskari.myplaces.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MyPlace;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.myplaces.service.MyPlacesFeaturesService;
import fi.nls.oskari.myplaces.service.wfst.MyPlacesFeaturesServiceWFST;
import fi.nls.oskari.myplaces.service.wfst.MyPlacesFeaturesWFSTRequestBuilder;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("MyPlacesFeatures")
public class MyPlacesFeaturesHandler extends RestActionHandler {

    private final static Logger LOG = LogFactory.getLogger(MyPlacesFeaturesHandler.class);

    private static final String PARAM_FEATURES = "features";
    private static final String PARAM_CRS = "crs";
    private static final String PARAM_LAYER_ID = "layerId";
    private static final String JSKEY_DELETED = "deleted";

    private MyPlacesService service;
    private MyPlacesFeaturesService featureService;

    @Override
    public void init() {
        super.init();
        service = OskariComponentManager.getComponentOfType(MyPlacesService.class);
        featureService = new MyPlacesFeaturesServiceWFST();
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final String crs = params.getHttpParam(PARAM_CRS, "EPSG:3067");
        final String layerId = params.getHttpParam(PARAM_LAYER_ID);

        try {
            final JSONObject featureCollection;
            if (layerId != null && !layerId.isEmpty()) {
                LOG.debug("Get MyPlaces by layer id, uuid:", user.getUuid(),
                        "layerId:", layerId, "crs:", crs);
                long categoryId = Long.parseLong(layerId);
                if (!service.canModifyCategory(user, categoryId)) {
                    throw new ActionDeniedException(
                            "Tried to GET features from category: " + categoryId);
                }
                featureCollection = featureService.getFeaturesByCategoryId(categoryId, crs);
            } else {
                LOG.debug("Get MyPlaces by user uuid, uuid:", user.getUuid(),
                        "crs:", crs);
                featureCollection = featureService.getFeaturesByUserId(user.getUuid(), crs);
            }

            ResponseHelper.writeResponse(params, featureCollection);
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to get features");
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final String crs = params.getHttpParam(PARAM_CRS, "EPSG:3067");
        final List<MyPlace> places = readMyPlaces(params, false);
        checkUserCanUseModifyCategories(user, places);
        for (MyPlace place : places) {
            place.setUuid(user.getUuid());
        }

        long[] ids;
        try {
            ids = featureService.insert(places);
            LOG.info("Inserted MyPlaces:", ids);
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to insert features");
        }

        try {
            ResponseHelper.writeResponse(params, featureService.getFeaturesByMyPlaceId(ids, crs));
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to get features after insert");
        }
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final String crs = params.getHttpParam(PARAM_CRS, "EPSG:3067");
        final List<MyPlace> places = readMyPlaces(params, true);
        checkUserCanModifyPlaces(user, places);
        for (MyPlace place : places) {
            place.setUuid(user.getUuid());
        }

        long[] ids = places.stream().mapToLong(MyPlace::getId).toArray();

        try {
            LOG.debug("Updating MyPlaces:", ids);
            int updated = featureService.update(places);
            LOG.info("Updated", updated, "/", places.size());
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to update features");
        }

        try {
            ResponseHelper.writeResponse(params, featureService.getFeaturesByMyPlaceId(ids, crs));
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to get features after update");
        }
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final String featureIds = params.getRequiredParam(PARAM_FEATURES);
        final long[] ids = Arrays.stream(featureIds.split(","))
                .mapToLong(Long::parseLong)
                .toArray();
        for (long id : ids) {
            if (!service.canModifyPlace(user, id)) {
                throw new ActionDeniedException("Tried to delete place: " + id);
            }
        }

        int deleted;
        try {
            LOG.debug("Deleting MyPlaces:", ids);
            deleted = featureService.delete(ids);
            LOG.info("Deleted", deleted, "/", ids.length);
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to delete features");
        }

        JSONObject response = new JSONObject();
        JSONHelper.putValue(response, JSKEY_DELETED, deleted);
        ResponseHelper.writeResponse(params, response);
    }

    private List<MyPlace> readMyPlaces(ActionParameters params, boolean checkId)
            throws ActionException {
        try {
            final String payload;
            try (InputStream in = params.getRequest().getInputStream()) {
                payload = IOHelper.readString(in);
            } catch (IOException e) {
                throw new ActionException("IOException occured");
            }
            return MyPlacesFeaturesWFSTRequestBuilder.parseMyPlaces(payload, checkId);
        } catch (JSONException e) {
            throw new ActionParamsException("Invalid input", e);
        }
    }

    private void checkUserCanUseModifyCategories(User user, List<MyPlace> places)
            throws ActionDeniedException {
        long[] uniqueCategoryIds = places.stream()
                .mapToLong(MyPlace::getCategoryId)
                .distinct()
                .toArray();
        for (long categoryId : uniqueCategoryIds) {
            if (!service.canModifyCategory(user, categoryId)) {
                throw new ActionDeniedException(
                        "Tried to insert feature into category: " + categoryId);
            }
        }
    }

    private void checkUserCanModifyPlaces(User user, List<MyPlace> places)
            throws ActionDeniedException {
        for (MyPlace place : places) {
            if (!service.canModifyPlace(user, place.getId())) {
                throw new ActionDeniedException("User: " + user.getId() +
                        " tried to modify place: " + place.getId());
            }
        }
    }

}
