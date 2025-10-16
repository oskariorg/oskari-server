package org.oskari.control.myfeatures;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.oskari.control.myfeatures.dto.CreateMyFeaturesLayer;
import org.oskari.control.myfeatures.dto.UpdateMyFeaturesLayer;
import org.oskari.map.myfeatures.service.MyFeaturesService;
import org.oskari.user.User;
import org.oskari.util.ObjectMapperProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayerFullInfo;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayerInfo;
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
        if (paramId == null || paramId.isBlank()) {
            String language = params.getLocale().getLanguage();
            List<MyFeaturesLayerInfo> layers = getLayersByUser(user, language);
            ResponseHelper.writeJsonResponse(params, om, layers);
        } else {
            MyFeaturesLayerFullInfo layer = getLayerById(paramId, user);
            if (layer == null) {
                ResponseHelper.writeResponse(params, 404, new JSONObject().put("msg", "No such layer"));
            } else {
                ResponseHelper.writeJsonResponse(params, om, layer);
            }
        }
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

        MyFeaturesLayerFullInfo response = MyFeaturesLayerFullInfo.from(layer);

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
        MyFeaturesLayerFullInfo response = MyFeaturesLayerFullInfo.from(layer);
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

    private MyFeaturesLayerFullInfo getLayerById(String paramId, User user) throws ActionException {
        UUID layerId = parseLayerId(paramId);
        MyFeaturesLayer layer = service.getLayer(layerId);
        if (layer == null || !canRead(user, layer)) {
            return null;
        }
        return MyFeaturesLayerFullInfo.from(layer);
    }

    private List<MyFeaturesLayerInfo> getLayersByUser(User user, String lang) throws ActionException {
        return service.getLayersByOwnerUuid(user.getUuid()).stream()
            .map(layer -> MyFeaturesLayerInfo.from(layer, lang))
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
