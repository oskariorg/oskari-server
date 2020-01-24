package org.oskari.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceRuntimeException;
import org.json.JSONObject;

public class LayerAdminJSONHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static MapLayer readJSON(String layerJSON) {
        try {
            return OBJECT_MAPPER.readValue(layerJSON, MapLayer.class);
        } catch (Exception ex) {
            throw new ServiceRuntimeException("Coudn't parse layer from: " + layerJSON, ex);
        }
    }
    public static OskariLayer fromJSON(MapLayer model) {
        OskariLayer layer = new OskariLayer();
        layer.setId(model.getId());
        layer.setType(model.getType());
        layer.setUrl(model.getUrl());
        layer.setUsername(model.getUsername());
        layer.setPassword(model.getPassword());
        layer.setVersion(model.getVersion());
        layer.setName(model.getName());
        layer.setLocale(new JSONObject(model.getLocale()));

        layer.setSrs_name(model.getSrs());
        layer.setOpacity(model.getOpacity());
        layer.setStyle(model.getStyle());
        layer.setMinScale(model.getMinscale());
        layer.setMaxScale(model.getMaxscale());

        layer.setLegendImage(model.getLegend_image());
        layer.setMetadataId(model.getMetadataid());
        if (model.getParams() != null) {
            layer.setParams(new JSONObject(model.getParams()));
        }
        if (model.getAttributes() != null) {
            layer.setAttributes(new JSONObject(model.getAttributes()));
        }
        if (model.getOptions() != null) {
            layer.setOptions(new JSONObject(model.getOptions()));
        }

        layer.setGfiType(model.getGfi_type());
        layer.setGfiXslt(model.getGfi_xslt());
        layer.setGfiContent(model.getGfi_content());

        layer.setBaseMap(model.isBase_map());
        layer.setRealtime(model.isRealtime());
        layer.setRefreshRate(model.getRefresh_rate());
        layer.setCapabilitiesUpdateRateSec(model.getCapabilities_update_rate_sec());

        layer.setDataproviderId(getDataProviderId(model));
        layer.setInternal(model.isInternal());

        // TODO: handle sublayers layer.getSublayers()
        // TODO: handle groups -> MapLayerGroupsHelper.findGroupsForNames_dangerzone_() + setGroupsForLayer()
        // TODO: role_permissions -> MapLayerPermissionsHelper.setLayerPermissions()
        return layer;
    }

    private static int getDataProviderId(MapLayer model) {
        DataProvider provider = null;
        if (model.getDataprovider_id() > 0) {
            provider = getDataProviderService().find(model.getDataprovider_id());
        } else if (model.getDataprovider() != null) {
            provider = getDataProviderService().find(model.getDataprovider_id());
        }
        if (provider == null) {
            throw new ServiceRuntimeException("Couln't find data provider for layer");
        }
        return provider.getId();
    }

    private static DataProviderService getDataProviderService() {
        return OskariComponentManager.getComponentOfType(DataProviderService.class);
    }
}
