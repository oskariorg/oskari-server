package org.oskari.control.myfeatures;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.oskari.control.myfeatures.dto.CreateMyFeaturesFeature;
import org.oskari.control.myfeatures.dto.UpdateMyFeaturesFeature;
import org.oskari.map.myfeatures.service.MyFeaturesService;
import org.oskari.user.User;
import org.oskari.util.ObjectMapperProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesFeature;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("MyFeaturesFeature")
public class MyFeaturesFeatureHandler extends RestActionHandler {

    public static final String PARAM_LAYER_ID = "layer_id";
    public static final String PARAM_ID = "id";

    private MyFeaturesService service;
    private ObjectMapper om;

    void setService(MyFeaturesService myFeaturesService) {
        this.service = Objects.requireNonNull(myFeaturesService);
    }

    void setObjectMapper(ObjectMapper om) {
        this.om = om;
    }

    @Override
    public void init() {
        if (service == null) {
            setService(OskariComponentManager.getComponentOfType(MyFeaturesService.class));
        }
        if (om == null) {
            setObjectMapper(ObjectMapperProvider.OM);
        }
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        UUID layerId = parseLayerId(params);
        long featureId = params.getHttpParam(PARAM_ID, Long.MIN_VALUE);
        User user = params.getUser();

        if (!canEdit(user, layerId)) {
            // "Public" reading through GetWFSFeatures
            throw new ActionDeniedException("User: " + user.getId() + " tried to read features from layer " + layerId);
        }

        List<MyFeaturesFeature> features;
        if (featureId == Long.MIN_VALUE) {
            features = service.getFeatures(layerId);
        } else {
            MyFeaturesFeature f = service.getFeature(layerId, featureId);
            features = f != null ? Collections.singletonList(f) : Collections.emptyList();
        }

        ResponseHelper.writeJsonResponse(params, om, features);
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        CreateMyFeaturesFeature createFeature = parsePayload(params, CreateMyFeaturesFeature.class);

        List<String> validationErrors = createFeature.validate();
        if (!validationErrors.isEmpty()) {
            throw new ActionParamsException(toJSONString(validationErrors));
        }

        MyFeaturesFeature feature = createFeature.toDomain(om);
        UUID layerId = createFeature.getLayerId();

        User user = params.getUser();
        if (!canEdit(user, layerId)) {
            throw new ActionDeniedException("User: " + user.getId() + " tried to insert feature to layer " + layerId);
        }

        service.createFeature(layerId, feature);

        ResponseHelper.writeJsonResponse(params, om, feature);
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        UpdateMyFeaturesFeature updateFeature = parsePayload(params, UpdateMyFeaturesFeature.class);

        List<String> validationErrors = updateFeature.validate();
        if (!validationErrors.isEmpty()) {
            throw new ActionParamsException(toJSONString(validationErrors));
        }

        MyFeaturesFeature feature = updateFeature.toDomain(om);
        UUID layerId = updateFeature.getLayerId();

        User user = params.getUser();
        if (!canEdit(user, layerId)) {
            throw new ActionDeniedException("User: " + user.getId() + " tried to modify feature " + feature.getId());
        }

        service.updateFeature(layerId, feature);

        ResponseHelper.writeJsonResponse(params, om, feature);
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        UUID layerId = parseLayerId(params);
        long featureId = params.getHttpParam(PARAM_ID, Long.MIN_VALUE);

        User user = params.getUser();
        if (!canEdit(user, layerId)) {
            throw new ActionDeniedException(
                    "User: " + user.getId() + " tried to delete feature(s) from layer " + layerId);
        }

        if (featureId == Long.MIN_VALUE) {
            service.deleteFeaturesByLayerId(layerId);
        } else {
            service.deleteFeature(layerId, featureId);
        }
    }

    <T> T parsePayload(ActionParameters params, Class<T> c) throws ActionParamsException {
        try {
            return om.readValue(params.getPayLoad(), c);
        } catch (Exception e) {
            throw new ActionParamsException("Failed to parse payload", e);
        }
    }

    private UUID parseLayerId(ActionParameters params) throws ActionParamsException {
        String layerId = params.getRequiredParam(PARAM_LAYER_ID);
        try {
            return UUID.fromString(layerId);
        } catch (Exception e) {
            throw new ActionParamsException("Param " + PARAM_LAYER_ID + " must be a valid UUID");
        }
    }

    private boolean canEdit(User user, UUID layerId) {
        MyFeaturesLayer existing = service.getLayer(layerId);
        return existing != null && existing.getOwnerUuid().equals(user.getUuid());
    }

    String toJSONString(Object obj) throws ActionException {
        try {
            return om.writeValueAsString(obj);
        } catch (Exception e) {
            throw new ActionException("Failed to encode response to JSON", e);
        }
    }

}
