package org.oskari.control.myfeatures;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.oskari.control.myfeatures.dto.CreateMyFeaturesLayer;
import org.oskari.control.myfeatures.dto.MyFeaturesLayerInfo;
import org.oskari.control.myfeatures.dto.UpdateMyFeaturesLayer;
import org.oskari.map.myfeatures.service.MyFeaturesService;
import org.oskari.user.User;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("MyFeaturesLayer")
public class MyFeaturesLayerHandler extends RestActionHandler {

    public static final String PARAM_ID = "id";

    private MyFeaturesService service;
    private ObjectMapper om;

    void setService(MyFeaturesService service) {
        this.service = Objects.requireNonNull(service);
    }

    void setObjectMapper(ObjectMapper om) {
        this.om = Objects.requireNonNull(om);
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
        String paramId = params.getHttpParam(PARAM_ID);
        User user = params.getUser();
        List<MyFeaturesLayerInfo> layers = getLayers(paramId, user);
        ResponseHelper.writeJsonResponse(params, om, layers);
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        String payload = params.getPayLoad();

        CreateMyFeaturesLayer createLayer;
        try {
            createLayer = om.readValue(payload, CreateMyFeaturesLayer.class);
        } catch (Exception e) {
            throw new ActionParamsException("Failed to parse payload", e);
        }

        List<String> validationErrors = createLayer.validate();
        if (!validationErrors.isEmpty()) {
            throw new ActionParamsException(toJSONString(validationErrors));
        }

        MyFeaturesLayer layer;
        try {
            layer = createLayer.toDomain(om);
        } catch (Exception e) {
            throw new ActionParamsException("Failed to convert to domain model", e);
        }

        layer.setOwnerUuid(params.getUser().getUuid());

        service.createLayer(layer);

        MyFeaturesLayerInfo response = MyFeaturesLayerInfo.from(layer);

        ResponseHelper.writeJsonResponse(params, om, response);
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        String payload = params.getPayLoad();

        UpdateMyFeaturesLayer updateLayer;
        try {
            updateLayer = om.readValue(payload, UpdateMyFeaturesLayer.class);
        } catch (Exception e) {
            throw new ActionParamsException("Failed to parse payload", e);
        }

        List<String> validationErrors = updateLayer.validate();
        if (!validationErrors.isEmpty()) {
            throw new ActionParamsException(toJSONString(validationErrors));
        }

        MyFeaturesLayer layer;
        try {
            layer = updateLayer.toDomain(om);
        } catch (Exception e) {
            throw new ActionParamsException("Failed to convert to domain model", e);
        }

        if (!canEdit(params.getUser(), layer.getId())) {
            ResponseHelper.writeError(params, "No such layer", 404);
            return;
        }

        service.updateLayer(layer);
        MyFeaturesLayerInfo response = MyFeaturesLayerInfo.from(layer);
        ResponseHelper.writeJsonResponse(params, om, response);
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        String paramId = params.getRequiredParam(PARAM_ID);
        UUID layerId = parseLayerId(paramId);
        User user = params.getUser();

        if (canEdit(user, layerId)) {
            service.deleteLayer(layerId);
        }
    }

    private UUID parseLayerId(String paramId) throws ActionParamsException {
        try {
            return UUID.fromString(paramId);
        } catch (Exception e) {
            throw new ActionParamsException("Param " + PARAM_ID + " must be a valid UUID");
        }
    }

    private List<MyFeaturesLayerInfo> getLayers(String paramId, User user) throws ActionException {
        if (paramId != null && !paramId.isBlank()) {
            UUID layerId = parseLayerId(paramId);
            MyFeaturesLayer layer = service.getLayer(layerId);
            if (!canRead(user, layer)) {
                return Collections.emptyList();
            }
            return Collections.singletonList(MyFeaturesLayerInfo.from(layer));
        }
        return service.getLayersByOwnerUuid(user.getUuid()).stream()
            .map(MyFeaturesLayerInfo::from)
            .collect(Collectors.toList());
    }

    private boolean canRead(User user, MyFeaturesLayer layer) {
        return layer.isPublished() || layer.getOwnerUuid().equals(user.getUuid());
    }

    private boolean canEdit(User user, UUID layerId) {
        MyFeaturesLayer existing = service.getLayer(layerId);
        return existing != null && existing.getOwnerUuid().equals(user.getUuid());
    }

    private String toJSONString(Object obj) throws ActionException {
        try {
            return om.writeValueAsString(obj);
        } catch (Exception e) {
            throw new ActionException("Failed to encode response to JSON", e);
        }
    }

}
