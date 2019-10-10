package fi.nls.oskari.control.layer;


import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatterWFS;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilitiesHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.wfs.GetGtWFSCapabilities;
import fi.nls.oskari.wms.GetGtWMSCapabilities;
import fi.nls.oskari.wmts.WMTSCapabilitiesParser;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import org.oskari.service.wfs3.WFS3Service;
import org.oskari.service.wfs3.model.WFS3CollectionInfo;
import org.oskari.service.wfs3.model.WFS3Exception;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.KEY_LAYERS;

/**
 * Get capabilites for layer and returns JSON formatted as Oskari layers
 */
@OskariActionRoute("GetWSCapabilities")
public class GetWSCapabilitiesHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetWSCapabilitiesHandler.class);
    private static final String PARM_URL = "url";
    private static final String PARM_TYPE = "type";
    private static final String PARM_VERSION = "version";
    private static final String PARM_USER = "user";
    private static final String PARM_PW = "pw";
    private static final String PARM_CRS = "crs";

    private static final String VERSION_WFS3 = "3.0.0";

    private final CapabilitiesCacheService capabilitiesService;
    private String[] permittedRoles;

    public GetWSCapabilitiesHandler() {
        this(OskariComponentManager.getComponentOfType(CapabilitiesCacheService.class));
    }

    public GetWSCapabilitiesHandler(CapabilitiesCacheService capabilitiesService) {
        this.capabilitiesService = capabilitiesService;
    }

    @Override
    public void init() {
        permittedRoles = PropertyUtil.getCommaSeparatedList("actionhandler.GetWSCapabilitiesHandler.roles");
    }

    public void handleAction(ActionParameters params) throws ActionException {
        if (!params.getUser().hasAnyRoleIn(permittedRoles)) {
            throw new ActionDeniedException("Unauthorized user tried to proxy via capabilities");
        }

        final String url = params.getRequiredParam(PARM_URL).trim();
        final String type = params.getHttpParam(PARM_TYPE, OskariLayer.TYPE_WMS);
        final String version = params.getHttpParam(PARM_VERSION, "");
        final String user = params.getHttpParam(PARM_USER, "");
        final String pw = params.getHttpParam(PARM_PW, "");
        final String currentCrs = params.getHttpParam(PARM_CRS, "EPSG:3067");

        log.debug("Trying to get capabilities for type:", type, "with url:", url);
        JSONObject capabilities = getCapabilities(url, type, version, user, pw, currentCrs);
        ResponseHelper.writeResponse(params, capabilities);
    }

    private JSONObject getCapabilities(String url, String type, String version,
            String user, String pw, String currentCrs) throws ActionException {
        try {
            switch (type) {
            case OskariLayer.TYPE_WMS:
                return GetGtWMSCapabilities.getWMSCapabilities(capabilitiesService, url, user, pw, version, currentCrs);
            case OskariLayer.TYPE_WFS:
                if (VERSION_WFS3.equals(version)) {
                    return getWFS3Capabilities(url, user, pw, currentCrs);
                } else {
                    return GetGtWFSCapabilities.getWFSCapabilities(url, version, user, pw, currentCrs);
                }
            case OskariLayer.TYPE_WMTS:
                OskariLayerCapabilities caps = capabilitiesService.getCapabilities(url, OskariLayer.TYPE_WMTS, version, user, pw);
                String capabilitiesXML = caps.getData();
                WMTSCapabilities wmtsCaps = WMTSCapabilitiesParser.parseCapabilities(capabilitiesXML);
                if (caps.getId() == null) {
                    capabilitiesService.save(caps);
                }
                JSONObject resultJSON = WMTSCapabilitiesParser.asJSON(wmtsCaps, url, currentCrs);
                JSONHelper.putValue(resultJSON, "xml", capabilitiesXML);
                return resultJSON;
            default:
                throw new ActionParamsException("Couldn't determine operation based on parameters");
            }
        } catch (Exception e) {
            throw new ActionException("Capabilities parsing failed: " + e.getMessage(), e);
        }
    }
    public static JSONObject getWFS3Capabilities(String url, String user, String pw, String currentCrs) throws WFS3Exception, IOException {
        WFS3Service service = WFS3Service.fromURL(url, user, pw);
        List<JSONObject> layers = service.getCollections().stream()
                .map(collectionInfo -> toOskariLayer(url, service, collectionInfo, null))
                .map(layer -> wfsLayerToJSON(layer, currentCrs, user, pw))
                .collect(Collectors.toList());
        return JSONHelper.createJSONObject(KEY_LAYERS, new JSONArray(layers));
    }
    private static OskariLayer toOskariLayer(String url, WFS3Service service, WFS3CollectionInfo collection, Set<String> systemCRSs) {
        OskariLayer layer = new OskariLayer();
        layer.setType(OskariLayer.TYPE_WFS);
        layer.setVersion(VERSION_WFS3);
        layer.setUrl(url);
        layer.setName(collection.getId());
        layer.setMaxScale(1d);
        layer.setMinScale(1500000d);
        String title = collection.getTitle() != null ? collection.getTitle() : collection.getId();
        for (String lang : PropertyUtil.getSupportedLanguages()) {
            layer.setName(lang, title);
        }
        OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWFS(service, layer, systemCRSs);

        return layer;
    }
    private static JSONObject wfsLayerToJSON(OskariLayer layer, String crs, String user, String pw) {
        LayerJSONFormatterWFS formatter = new LayerJSONFormatterWFS();
        String lang = PropertyUtil.getDefaultLanguage();
        JSONObject obj = formatter.getJSON(layer, lang, false, crs);
        OskariLayerWorker.modifyCommonFieldsForEditing(obj, layer);
        WFSLayerConfiguration lc = layerToWfs30LayerConfiguration(layer, crs, user, pw);
        JSONObject admin = JSONHelper.getJSONObject(obj, "admin");
        JSONHelper.putValue(admin, "passthrough", JSONHelper.createJSONObject(lc.getAsJSON()));
        // NOTE! Important to remove id since this is at template
        obj.remove("id");
        // Admin layer tools needs for listing layers
        JSONHelper.putValue(obj, "title", layer.getName());
        return obj;
    }
    private static WFSLayerConfiguration layerToWfs30LayerConfiguration (OskariLayer layer, String crs, String user, String pw) {
        final WFSLayerConfiguration lc = new WFSLayerConfiguration();
        // Use defaults for now, modify if needed
        String name = layer.getName();
        lc.setDefaults();
        lc.setURL(layer.getUrl());
        lc.setUsername(user);
        lc.setPassword(pw);
        lc.setLayerName(name); // or WFS3CollectionInfo getTitle()
        lc.setLayerId("layer_" + name);
        lc.setSRSName(crs);
        lc.setGMLGeometryProperty("geometry");
        lc.setWFSVersion(VERSION_WFS3);
        lc.setFeatureElement(name);
        lc.setFeatureNamespace("");
        lc.setFeatureNamespaceURI("");
        lc.setJobType("default");
        return lc;
    }
}
