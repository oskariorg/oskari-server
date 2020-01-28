package org.oskari.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;
import org.oskari.admin.model.MapLayer;
import org.oskari.admin.model.MapLayerAdminOutput;

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

    public static String writeJSON(MapLayer layer) {
        try {
            return OBJECT_MAPPER.writeValueAsString(layer);
        } catch (Exception ex) {
            throw new ServiceRuntimeException("Coudn't write layer to JSON", ex);
        }
    }

    public static OskariLayer fromJSON(MapLayer model) {
        OskariLayer layer = new OskariLayer();
        Integer id = model.getId();
        if (id != null) {
            // unboxing null Integer to int causes NPE so not calling setId() with null value
            layer.setId(model.getId());
        }
        layer.setType(model.getType());
        layer.setUrl(model.getUrl());
        layer.setUsername(model.getUsername());
        layer.setPassword(model.getPassword());
        if (model.getVersion() != null) {
            // version has non-null requirement in db (empty string is ok)
            layer.setVersion(model.getVersion());
        }
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

        DataProvider provider = getDataProvider(model);
        layer.addDataprovider(provider);
        layer.setDataproviderId(provider.getId());
        layer.setInternal(model.isInternal());

        // TODO: handle sublayers layer.getSublayers()
        // TODO: handle groups -> MapLayerGroupsHelper.findGroupsForNames_dangerzone_() + setGroupsForLayer()
        // TODO: role_permissions -> MapLayerPermissionsHelper.setLayerPermissions()
        return layer;
    }


    public static MapLayerAdminOutput toJSON(OskariLayer layer) {
        MapLayerAdminOutput out = new MapLayerAdminOutput();
        out.setId(layer.getId());
        out.setType(layer.getType());
        out.setUrl(layer.getUrl());
        out.setUsername(layer.getUsername());
        out.setPassword(layer.getPassword());
        out.setVersion(layer.getVersion());
        out.setName(layer.getName());
        out.setLocale(JSONHelper.getObjectAsMap(layer.getLocale()));

        out.setSrs(layer.getSrs_name());
        out.setOpacity(layer.getOpacity());
        out.setStyle(layer.getStyle());
        out.setMinscale(layer.getMinScale());
        out.setMaxscale(layer.getMaxScale());

        layer.setLegendImage(layer.getLegendImage());
        layer.setMetadataId(layer.getMetadataId());

        out.setParams(JSONHelper.getObjectAsMap(layer.getParams()));
        out.setAttributes(JSONHelper.getObjectAsMap(layer.getAttributes()));
        out.setOptions(JSONHelper.getObjectAsMap(layer.getOptions()));

        out.setGfi_type(layer.getGfiType());
        out.setGfi_xslt(layer.getGfiXslt());
        out.setGfi_content(layer.getGfiContent());

        out.setBase_map(layer.isBaseMap());
        out.setRealtime(layer.getRealtime());
        out.setRefresh_rate(layer.getRefreshRate());
        out.setCapabilities_update_rate_sec(layer.getCapabilitiesUpdateRateSec());

        DataProvider provider = layer.getGroup();
        if (provider != null) {
            out.setDataprovider_id(provider.getId());
        } else {
            out.setDataprovider_id(layer.getDataproviderId());
        }
        out.setInternal(layer.isInternal()); // we might not need to write this
        out.setCapabilities(JSONHelper.getObjectAsMap(layer.getCapabilities()));

        // TODO: handle sublayers layer.getSublayers()
        // TODO: handle groups -> MapLayerGroupsHelper.findGroupsForNames_dangerzone_() + setGroupsForLayer()
        // TODO: role_permissions -> MapLayerPermissionsHelper.setLayerPermissions()
        return out;
    }


    private static DataProvider getDataProvider(MapLayer model) {
        DataProvider provider = null;
        if (model.getDataprovider_id() > 0) {
            provider = getDataProviderService().find(model.getDataprovider_id());
        } else if (model.getDataprovider() != null) {
            provider = getDataProviderService().findByName(model.getDataprovider());
        }
        if (provider == null) {
            throw new ServiceRuntimeException("Couln't find data provider for layer (" + model.getDataprovider_id() + "/" + model.getDataprovider() + ")");
        }
        return provider;
    }

    private static DataProviderService getDataProviderService() {
        return OskariComponentManager.getComponentOfType(DataProviderService.class);
    }
}
