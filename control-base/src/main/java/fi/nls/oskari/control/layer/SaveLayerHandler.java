package fi.nls.oskari.control.layer;

import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilities;
import fi.mml.map.mapwindow.service.wms.LayerNotFoundInCapabilitiesException;
import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceParseException;
import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.data.domain.OskariLayerResource;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.util.ViewHelper;
import fi.nls.oskari.permission.domain.Permission;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import fi.nls.oskari.service.capabilities.OskariLayerCapabilitiesHelper;
import fi.nls.oskari.util.*;
import fi.nls.oskari.wfs.GetGtWFSCapabilities;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.util.WFSParserConfigs;
import fi.nls.oskari.wmts.WMTSCapabilitiesParser;
import fi.nls.oskari.wmts.domain.WMTSCapabilities;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.service.util.ServiceFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static fi.nls.oskari.control.ActionConstants.PARAM_SRS;

/**
 * Admin insert/update of WMS map layer
 */
@OskariActionRoute("SaveLayer")
public class SaveLayerHandler extends ActionHandler {

    private class SaveResult {
        long layerId = -1;
        boolean capabilitiesUpdated = false;
    }

    private OskariLayerService mapLayerService = ServiceFactory.getMapLayerService();
    private ViewService viewService = ServiceFactory.getViewService();
    private WFSLayerConfigurationService wfsLayerService = ServiceFactory.getWfsLayerService();
    private PermissionsService permissionsService = ServiceFactory.getPermissionsService();
    private DataProviderService dataProviderService = ServiceFactory.getDataProviderService();
    private OskariMapLayerGroupService oskariMapLayerGroupService = ServiceFactory.getOskariMapLayerGroupService();
    private CapabilitiesCacheService capabilitiesService = ServiceFactory.getCapabilitiesCacheService();
    private WFSParserConfigs wfsParserConfigs = new WFSParserConfigs();
    private static final Logger LOG = LogFactory.getLogger(SaveLayerHandler.class);

    private static final String PARAM_LAYER_ID = "layer_id";
    private static final String PARAM_LAYER_NAME = "layerName";
    private static final String PARAM_LAYER_URL = "layerUrl";
    private static final String PARAM_SRS_NAME = "srs_name";
    private static final String PARAM_MAPLAYER_GROUPS = "maplayerGroups";
    private static final String PARAM_VIEW_PERMISSIONS = "viewPermissions";
    private static final String PARAM_PUBLISH_PERMISSIONS = "publishPermissions";
    private static final String PARAM_DOWNLOAD_PERMISSIONS = "downloadPermissions";
    private static final String PARAM_EMBEDDED_PERMISSIONS = "embeddedPermissions";
    private static final String PARAM_LAYER_TYPE ="layerType";
    private static final String PARAM_PARENT_ID ="parentId";
    private static final String PARAM_GROUP_ID ="groupId";
    private static final String PARAM_VERSION ="version";
    private static final String PARAM_IS_BASE ="isBase";
    private static final String PARAM_OPACITY ="opacity";
    private static final String PARAM_STYLE ="style";
    private static final String PARAM_MIN_SCALE ="minScale";
    private static final String PARAM_MAX_SCALE ="maxScale";
    private static final String PARAM_LEGEND_IMAGE ="legendImage";
    private static final String PARAM_METADATA_ID ="metadataId";
    private static final String PARAM_GFI_CONTENT ="gfiContent";
    private static final String PARAM_USERNAME ="username";
    private static final String PARAM_PASSWORD ="password";
    private static final String PARAM_CAPABILITIES_UPDATE_RATE_SEC ="capabilitiesUpdateRateSec";
    private static final String PARAM_ATTRIBUTES ="attributes";
    private static final String PARAM_PARAMS ="params";
    private static final String PARAM_REALTIME ="realtime";
    private static final String PARAM_REFRESH_RATE ="refreshRate";
    private static final String PARAM_GML2_SEPARATOR = "GML2Separator";
    private static final String PARAM_GML_GEOMETRY_PROPERTY = "GMLGeometryProperty";
    private static final String PARAM_GML_VERSION = "GMLVersion";
    private static final String PARAM_WFS_VERSION = "WFSVersion";
    private static final String PARAM_FEATURE_ELEMENT = "featureElement";
    private static final String PARAM_FEATURE_NAMESPACE = "featureNamespace";
    private static final String PARAM_FEATURE_NAMESCAPE_URI = "featureNamespaceURI";
    private static final String PARAM_FEATURE_PARAMS_LOCALES = "featureParamsLocales";
    private static final String PARAM_FEATURE_TYPE = "featureType";
    private static final String PARAM_GEOMETRY_NAMESPACE_URI = "geometryNamespaceURI";
    private static final String PARAM_GEOMETRY_TYPE = "geometryType";
    private static final String PARAM_GET_FEATURE_INFO = "getFeatureInfo";
    private static final String PARAM_GET_HIGHLIGHT_IMAGE = "getHighlightImage";
    private static final String PARAM_GET_MAP_TILES = "getMapTiles";
    private static final String PARAM_MAX_FEATURES = "maxFeatures";
    private static final String PARAM_OUTPUT_FORMAT = "outputFormat";
    private static final String PARAM_SELECTED_FEATURE_PARAMS = "selectedFeatureParams";
    private static final String PARAM_TILE_BUFFER = "tileBuffer";
    private static final String PARAM_TILE_REQUEST = "tileRequest";
    private static final String PARAM_JOB_TYPE = "jobType";
    private static final String PARAM_REQUEST_TEMPLATE = "requestTemplate";
    private static final String PARAM_RESPONSE_TEMPLATE = "responseTemplate";
    private static final String PARAM_PARSE_CONFIG = "parseConfig";
    private static final String PARAM_TEMPLATE_NAME = "templateName";
    private static final String PARAM_TEMPLATE_TYPE = "templateType";
    private static final String PARAM_STYLE_SELECTION = "styleSelection";
    private static final String PARAM_XSLT = "xslt";
    private static final String PARAM_GFI_TYPE = "gfiType";
    private static final String PARAM_MANUAL_REFRESH = "manualRefresh";
    private static final String PARAM_RESOLVE_DEPTH = "resolveDepth";

    private static final String LAYER_NAME_PREFIX = "name_";
    private static final String LAYER_TITLE_PREFIX = "title_";

    private static final String ERROR_UPDATE_OR_INSERT_FAILED = "update_or_insert_failed";
    private static final String ERROR_NO_LAYER_WITH_ID = "no_layer_with_id:";
    private static final String ERROR_OPERATION_NOT_PERMITTED = "operation_not_permitted_for_layer_id:";
    private static final String ERROR_MANDATORY_FIELD_MISSING = "mandatory_field_missing:";
    private static final String ERROR_INVALID_FIELD_VALUE = "invalid_field_value:";
    private static final String ERROR_FE_PARSER_CONFIG_MISSING = "FE WFS feature parser config missing";

    private static final String OSKARI_FEATURE_ENGINE = "oskari-feature-engine";
    private static final String WFS1_1_0_VERSION = "1.1.0";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final SaveResult result = saveLayer(params);
        final int layerId = (int)result.layerId;
        final OskariLayer ml = mapLayerService.find(layerId);
        if(ml == null) {
            throw new ActionParamsException("Couldn't get the saved layer from DB - id:" + layerId);
        }

        // construct response as layer json
        final JSONObject layerJSON = OskariLayerWorker.getMapLayerJSON(ml, params.getUser(), params.getLocale().getLanguage(), params.getHttpParam(PARAM_SRS));
        if (layerJSON == null) {
            // handle error getting JSON failed
            throw new ActionException("Error constructing JSON for layer");
        }
        if(!result.capabilitiesUpdated) {
            // Cache update failed, no biggie
            JSONHelper.putValue(layerJSON, "warn", "metadataReadFailure");
            LOG.debug("Metadata read failure");
        }
        ResponseHelper.writeResponse(params, layerJSON);
    }

    private SaveResult saveLayer(final ActionParameters params) throws ActionException {

        // layer_id can be string -> external id!
        final String layer_id = params.getHttpParam(PARAM_LAYER_ID);
        SaveResult result = new SaveResult();

        try {
            // ************** UPDATE ************************
            if (layer_id != null) {

                final OskariLayer ml = mapLayerService.find(layer_id);
                if (ml == null) {
                    // layer wasn't found
                    throw new ActionException(ERROR_NO_LAYER_WITH_ID + layer_id);
                }
                if (!permissionsService.hasEditPermissionForLayerByLayerId(params.getUser(), ml.getId())) {
                    throw new ActionDeniedException(ERROR_OPERATION_NOT_PERMITTED + layer_id);
                }

                result.capabilitiesUpdated = handleRequestToMapLayer(params, ml);

                ml.setUpdated(new Date(System.currentTimeMillis()));
                mapLayerService.update(ml);
                //TODO: WFS spesific property update
                if (OskariLayer.TYPE_WFS.equals(ml.getType())) {
                    final WFSLayerConfiguration wfsl = wfsLayerService.findConfiguration(ml.getId());
                    wfsl.setAttributes(ml.getAttributes());
                    handleRequestToWfsLayer(params, wfsl);

                    // TODO: WFS field management implementation
                    // TODO: WFS2 spesific edits
                    /* if(wfsl.getJobType() != null && wfsl.getJobType().equals(OSKARI_FEATURE_ENGINE)){
                        handleFESpesificToWfsLayer(params, wfsl);
                    }  */
                    //wfsLayerService.update(wfsl);


                    // Styles setup
                    handleWfsLayerStyles(params, wfsl);


                    // Remove old redis data of WFSLayer_xx, new wfs conf data is inserted automatically
                    JedisManager.delAll(WFSLayerConfiguration.KEY + Integer.toString(ml.getId()));
                    JedisManager.delAll(WFSLayerConfiguration.IMAGE_KEY + Integer.toString(ml.getId()));
                }

                LOG.debug(ml);
                result.layerId = ml.getId();
                return result;
            }

            // ************** INSERT ************************
            else {

                if (!permissionsService.hasAddLayerPermission(params.getUser())) {
                    throw new ActionDeniedException(ERROR_OPERATION_NOT_PERMITTED + layer_id);
                }

                final OskariLayer ml = new OskariLayer();
                final Date currentDate = new Date(System.currentTimeMillis());
                ml.setCreated(currentDate);
                ml.setUpdated(currentDate);
                result.capabilitiesUpdated = handleRequestToMapLayer(params, ml);
                validateInsertLayer(params, ml);

                int id = mapLayerService.insert(ml);
                ml.setId(id);

                if(ml.isCollection()) {
                    // update the name with the id for permission mapping
                    ml.setName(ml.getId() + "_group");
                    mapLayerService.update(ml);
                }
                // Wfs
                if(OskariLayer.TYPE_WFS.equals(ml.getType())) {
                    final WFSLayerConfiguration wfsl = new WFSLayerConfiguration();
                    wfsl.setDefaults();
                    wfsl.setLayerId(Integer.toString(id));
                    wfsl.setAttributes(ml.getAttributes());
                    handleRequestToWfsLayer(params, wfsl);
                    if(wfsl.getJobType() != null && wfsl.getJobType().equals(OSKARI_FEATURE_ENGINE)){
                        handleFESpesificToWfsLayer(params, wfsl);
                    }
                    int idwfsl = wfsLayerService.insert(wfsl);
                    wfsl.setId(idwfsl);

                    // Styles setup
                    handleWfsLayerStyles(params, wfsl);
                }

                addPermissionsForRoles(ml,
                        getPermissionSet(params.getHttpParam(PARAM_VIEW_PERMISSIONS)),
                        getPermissionSet(params.getHttpParam(PARAM_PUBLISH_PERMISSIONS)),
                        getPermissionSet(params.getHttpParam(PARAM_DOWNLOAD_PERMISSIONS)),
                        getPermissionSet(params.getHttpParam(PARAM_EMBEDDED_PERMISSIONS)));

                // update keywords
                GetLayerKeywords glk = new GetLayerKeywords();
                glk.updateLayerKeywords(id, ml.getMetadataId());

                result.layerId = ml.getId();
                return result;
            }

        } catch (Exception e) {
            if (e instanceof ActionException) {
                throw (ActionException) e;
            } else {
                throw new ActionException(ERROR_UPDATE_OR_INSERT_FAILED, e);
            }
        }
    }

    /**
     * Treats the param as comma-separated list. Splits to individual values and
     * returns a set of values that could be converted to Long
     * @param param
     * @return
     */
    private Set<Long> getPermissionSet(final String param) {
        if(param == null) {
            return Collections.emptySet();
        }
        final Set<Long> set = new HashSet<Long>();
        final String[] roleIds = param.split(",");
        for (String externalId : roleIds) {
            final long extId = ConversionHelper.getLong(externalId, -1);
            if (extId != -1) {
                set.add(extId);
            }
        }
        return set;
    }

    private boolean handleRequestToMapLayer(final ActionParameters params, OskariLayer ml) throws ActionException {

        HttpServletRequest request = params.getRequest();

        if(ml.getId() == -1) {
            // setup type and parent for new layers only
            ml.setType(params.getHttpParam(PARAM_LAYER_TYPE));
            ml.setParentId(params.getHttpParam(PARAM_PARENT_ID, -1));
        }

        // organization id
        final DataProvider dataProvider = dataProviderService.find(params.getHttpParam(PARAM_GROUP_ID, -1));
        ml.addDataprovider(dataProvider);

        // get names and descriptions
        final Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if (paramName.startsWith(LAYER_NAME_PREFIX)) {
                String lang = paramName.substring(LAYER_NAME_PREFIX.length()).toLowerCase();
                String name = params.getHttpParam(paramName);
                ml.setName(lang, name);
            } else if (paramName.startsWith(LAYER_TITLE_PREFIX)) {
                String lang = paramName.substring(LAYER_TITLE_PREFIX.length()).toLowerCase();
                String title = params.getHttpParam(paramName);
                ml.setTitle(lang, title);
            }
        }

        String groupId = params.getHttpParam(PARAM_MAPLAYER_GROUPS);
        if(groupId != null) {
            ml.emptyMaplayerGroups();
            for (String id: groupId.split(",")) {
                MaplayerGroup maplayerGroup = oskariMapLayerGroupService.find(ConversionHelper.getInt(id, -1));
                ml.addGroup(maplayerGroup);
            }
        }

        ml.setVersion(params.getHttpParam(PARAM_VERSION, ""));
        ml.setBaseMap(ConversionHelper.getBoolean(params.getHttpParam(PARAM_IS_BASE), false));

        if(ml.isCollection()) {
            // ulr is needed for permission mapping, name is updated after we get the layer id
            ml.setUrl(ml.getType());
            // the rest is not relevant for collection layers
            return true;
        }

        ml.setName(params.getRequiredParam(PARAM_LAYER_NAME, ERROR_MANDATORY_FIELD_MISSING + PARAM_LAYER_NAME));
        final String url = params.getRequiredParam(PARAM_LAYER_URL, ERROR_MANDATORY_FIELD_MISSING + PARAM_LAYER_URL);
        ml.setUrl(url);
        validateUrl(ml.getSimplifiedUrl(true));

        ml.setOpacity(params.getHttpParam(PARAM_OPACITY, ml.getOpacity()));
        ml.setStyle(params.getHttpParam(PARAM_STYLE, ml.getStyle()));
        ml.setMinScale(ConversionHelper.getDouble(params.getHttpParam(PARAM_MIN_SCALE), ml.getMinScale()));
        ml.setMaxScale(ConversionHelper.getDouble(params.getHttpParam(PARAM_MAX_SCALE), ml.getMaxScale()));

        ml.setLegendImage(params.getHttpParam(PARAM_LEGEND_IMAGE, ml.getLegendImage()));
        ml.setMetadataId(params.getHttpParam(PARAM_METADATA_ID, ml.getMetadataId()));

        final String gfiContent = request.getParameter(PARAM_GFI_CONTENT);
        if (gfiContent != null) {
            // Clean GFI content
            final String[] tags = PropertyUtil.getCommaSeparatedList("gficontent.whitelist");
            HashMap<String,String[]> attributes = new HashMap<String, String[]>();
            HashMap<String[],String[]> protocols = new HashMap<String[], String[]>();
            String[] allAttributes = PropertyUtil.getCommaSeparatedList("gficontent.whitelist.attr");
            if (allAttributes.length > 0) {
                attributes.put(":all",allAttributes);
            }
            List<String> attrProps = PropertyUtil.getPropertyNamesStartingWith("gficontent.whitelist.attr.");
            for (String attrProp : attrProps) {
                String[] parts = attrProp.split("\\.");
                if (parts[parts.length-2].equals("protocol")) {
                    protocols.put(new String[]{parts[parts.length-3],parts[parts.length-1]},PropertyUtil.getCommaSeparatedList(attrProp));
                } else {
                    attributes.put(parts[parts.length-1],PropertyUtil.getCommaSeparatedList(attrProp));
                }
            }
            ml.setGfiContent(RequestHelper.cleanHTMLString(gfiContent, tags, attributes, protocols));
        }

        ml.setUsername(params.getHttpParam(PARAM_USERNAME, ml.getUsername()));
        ml.setPassword(params.getHttpParam(PARAM_PASSWORD, ml.getPassword()));

        ml.setCapabilitiesUpdateRateSec(params.getHttpParam(PARAM_CAPABILITIES_UPDATE_RATE_SEC, 0));

        String attributes = params.getHttpParam(PARAM_ATTRIBUTES);
        if (attributes != null && !attributes.equals("")) {
            ml.setAttributes(JSONHelper.createJSONObject(attributes));
        }

        String parameters = params.getHttpParam(PARAM_PARAMS);
        if (parameters != null && !parameters.equals("")) {
            ml.setParams(JSONHelper.createJSONObject(parameters));
        }

        ml.setSrs_name(params.getHttpParam(PARAM_SRS_NAME, ml.getSrs_name()));
        ml.setVersion(params.getHttpParam(PARAM_VERSION,ml.getVersion()));

        ml.setRealtime(ConversionHelper.getBoolean(params.getHttpParam(PARAM_REALTIME), ml.getRealtime()));
        ml.setRefreshRate(ConversionHelper.getInt(params.getHttpParam(PARAM_REFRESH_RATE), ml.getRefreshRate()));

        final Set<String> systemCRSs;
        try {
            systemCRSs = ViewHelper.getSystemCRSs(viewService);
        } catch (ServiceException e) {
            throw new ActionException("Failed to retrieve system CRSs", e);
        }

        switch (ml.getType()) {
        case OskariLayer.TYPE_WMS:
            return handleWMSSpecific(params, ml, systemCRSs);
        case OskariLayer.TYPE_WMTS:
            return handleWMTSSpecific(params, ml, systemCRSs);
        case OskariLayer.TYPE_WFS:
            handleWFSSpecific(params, ml, systemCRSs); // fallthrough
        default:
            // no capabilities to update, return true
            return true;
        }
    }

    private void handleRequestToWfsLayer(final ActionParameters params, WFSLayerConfiguration wfsl) throws ActionException {
        wfsl.setGML2Separator(ConversionHelper.getBoolean(params.getHttpParam(PARAM_GML2_SEPARATOR), wfsl.isGML2Separator()));
        wfsl.setGMLGeometryProperty(params.getHttpParam(PARAM_GML_GEOMETRY_PROPERTY));
        wfsl.setGMLVersion(params.getHttpParam(PARAM_GML_VERSION));
        wfsl.setSRSName(params.getHttpParam(PARAM_SRS_NAME));
        wfsl.setWFSVersion(params.getHttpParam(PARAM_WFS_VERSION, params.getHttpParam(PARAM_VERSION)));
        wfsl.setFeatureElement(params.getHttpParam(PARAM_FEATURE_ELEMENT));
        wfsl.setFeatureNamespace(params.getHttpParam(PARAM_FEATURE_NAMESPACE));
        wfsl.setFeatureNamespaceURI(params.getHttpParam(PARAM_FEATURE_NAMESCAPE_URI));
        wfsl.setFeatureParamsLocales(params.getHttpParam(PARAM_FEATURE_PARAMS_LOCALES));
        wfsl.setFeatureType(params.getHttpParam(PARAM_FEATURE_TYPE));
        wfsl.setGeometryNamespaceURI(params.getHttpParam(PARAM_GEOMETRY_NAMESPACE_URI));
        wfsl.setGeometryType(params.getHttpParam(PARAM_GEOMETRY_TYPE));
        wfsl.setGetFeatureInfo(ConversionHelper.getBoolean(params.getHttpParam(PARAM_GET_FEATURE_INFO), wfsl.isGetFeatureInfo()));
        wfsl.setGetHighlightImage(ConversionHelper.getBoolean(params.getHttpParam(PARAM_GET_HIGHLIGHT_IMAGE), wfsl.isGetHighlightImage()));
        wfsl.setGetMapTiles(ConversionHelper.getBoolean(params.getHttpParam(PARAM_GET_MAP_TILES), wfsl.isGetMapTiles()));
        wfsl.setLayerName(params.getHttpParam(PARAM_LAYER_NAME));
        wfsl.setMaxFeatures(ConversionHelper.getInt(params.getHttpParam(PARAM_MAX_FEATURES), wfsl.getMaxFeatures()));
        wfsl.setOutputFormat(params.getHttpParam(PARAM_OUTPUT_FORMAT));
        wfsl.setSelectedFeatureParams(params.getHttpParam(PARAM_SELECTED_FEATURE_PARAMS));
        wfsl.setTileBuffer(params.getHttpParam(PARAM_TILE_BUFFER));
        wfsl.setTileRequest(ConversionHelper.getBoolean(params.getHttpParam(PARAM_TILE_REQUEST), wfsl.isTileRequest()));
        wfsl.setJobType(params.getHttpParam(PARAM_JOB_TYPE));
    }

    private void handleFESpesificToWfsLayer(  final ActionParameters params, WFSLayerConfiguration wfsl) throws ActionException, ServiceException {
        if (wfsl == null) {
            return;
        }

        if(!wfsl.getWFSVersion().equals(WFS1_1_0_VERSION)) {
            wfsl.setRequestTemplate(params.getHttpParam(PARAM_REQUEST_TEMPLATE));
            wfsl.setResponseTemplate(params.getHttpParam(PARAM_RESPONSE_TEMPLATE));
            wfsl.setParseConfig(params.getHttpParam(PARAM_PARSE_CONFIG));
            wfsl.setTemplateName(params.getHttpParam(PARAM_TEMPLATE_NAME));
            wfsl.setTemplateType(params.getHttpParam(PARAM_TEMPLATE_TYPE));
            wfsl.setTemplateDescription("FE parser model - wfs version : " + wfsl.getWFSVersion());
            Map<String, String> model = new HashMap<String, String>();

            model.put("name", wfsl.getFeatureNamespace() + ":" + wfsl.getFeatureElement());
            model.put("description", wfsl.getTemplateDescription());
            model.put("type", wfsl.getTemplateType());
            model.put("request_template", wfsl.getRequestTemplate());
            model.put("response_template", wfsl.getResponseTemplate());
            model.put("parse_config", wfsl.getParseConfig().toString());

            int model_id = wfsLayerService.insertTemplateModel(model);

            wfsl.setTemplateModelId(model_id);
        }
        else {
            //TODO: fe save config support for wfs 1.1.0
            wfsl.setJobType(OSKARI_FEATURE_ENGINE);
            wfsl.setTileBuffer("{ \"default\" : 1, \"oskari_custom\" : 1}");;
        }
    }

    /**
     *  Inserts/updates sld style links to single wfs layer
     * @param params
     * @param wfsl
     * @throws ActionException
     * @throws ServiceException
     */
    private void handleWfsLayerStyles(final ActionParameters params, WFSLayerConfiguration wfsl) throws ActionException, ServiceException {


        if (wfsl != null && params.getHttpParam(PARAM_STYLE_SELECTION) != null) {
            JSONObject selectedStyles = JSONHelper.createJSONObject(params.getHttpParam(PARAM_STYLE_SELECTION));
            JSONArray styles = JSONHelper.getJSONArray(selectedStyles, "selectedStyles");
            List<Integer> sldIds = new ArrayList<Integer>();
            for (int i = 0; i < styles.length(); i++) {
                JSONObject stylelnk = JSONHelper.getJSONObject(styles, i);
                int lnk = ConversionHelper.getInt(JSONHelper.getStringFromJSON(stylelnk, "id", "0"), 0);
                if (lnk != 0) {
                    sldIds.add(lnk);
                }

            }
            //TODO: define a case to remove all styles  and update single style
            if (sldIds.size() > 0) {
                // Removes old links and insert new ones
                List<Integer> ids = wfsLayerService.insertSLDStyles(wfsl.getId(), sldIds);
            }
        }
    }

    /**
     * Check before maplayer insert that all data is valid for specific layer types
     *
     * @param params
     * @param ml
     * @throws ActionException
     */
    private void validateInsertLayer(final ActionParameters params, OskariLayer ml) throws ActionException {

        if (!OskariLayer.TYPE_WFS.equals(ml.getType())) {
            return;
        }
        if(params.getHttpParam(PARAM_JOB_TYPE) != null && params.getHttpParam(PARAM_JOB_TYPE).equals(OSKARI_FEATURE_ENGINE) ) {
            try {

                JSONArray feaconf = wfsParserConfigs.getFeatureTypeConfig(params.getHttpParam(PARAM_FEATURE_NAMESPACE) + ":" + params.getHttpParam(PARAM_FEATURE_ELEMENT));
                if (feaconf == null) {
                    feaconf = wfsParserConfigs.getFeatureTypeConfig("default");
                }

                if (feaconf == null) {
                    throw new ActionException(ERROR_FE_PARSER_CONFIG_MISSING);
                }
            } catch (Exception e) {
                if (e instanceof ActionException) {
                    throw (ActionException) e;
                } else {
                    throw new ActionException(ERROR_FE_PARSER_CONFIG_MISSING, e);
                }

            }
        }

    }

    private boolean handleWMSSpecific(final ActionParameters params, OskariLayer ml, Set<String> systemCRSs) {
        // Do NOT modify the 'xslt' parameter
        HttpServletRequest request = params.getRequest();
        final String xslt = request.getParameter(PARAM_XSLT);
        if(xslt != null) {
            // TODO: some validation of XSLT data
            ml.setGfiXslt(xslt);
        }
        ml.setGfiType(params.getHttpParam(PARAM_GFI_TYPE, ml.getGfiType()));

        try {
            OskariLayerCapabilities raw = capabilitiesService.getCapabilities(ml, true);
            WebMapService wms = OskariLayerCapabilitiesHelper.parseWMSCapabilities(raw.getData(), ml);
            OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWMS(wms, ml, systemCRSs);
            return true;
        } catch (ServiceException | WebMapServiceParseException | LayerNotFoundInCapabilitiesException ex) {
            LOG.error(ex, "Failed to set capabilities for layer", ml);
            return false;
        }
    }

    private boolean handleWMTSSpecific(final ActionParameters params, OskariLayer ml, Set<String> systemCRSs) {
        try {
            String currentCrs = params.getHttpParam(PARAM_SRS_NAME, ml.getSrs_name());
            OskariLayerCapabilities raw = capabilitiesService.getCapabilities(ml, true);
            WMTSCapabilities caps = WMTSCapabilitiesParser.parseCapabilities(raw.getData());
            OskariLayerCapabilitiesHelper.setPropertiesFromCapabilitiesWMTS(caps, ml, currentCrs, systemCRSs);
            return true;
        } catch (Exception ex) {
            LOG.error(ex, "Failed to set capabilities for layer", ml);
            return false;
        }
    }

    private void handleWFSSpecific(final ActionParameters params, OskariLayer ml, Set<String> systemCRSs) throws ActionException {
        // These are only in insert
        ml.setSrs_name(params.getHttpParam(PARAM_SRS_NAME, ml.getSrs_name()));
        ml.setVersion(params.getHttpParam(PARAM_WFS_VERSION,ml.getVersion()));

        // Put manual Refresh mode to attributes if true
        JSONObject attributes = ml.getAttributes();
        attributes.remove(PARAM_MANUAL_REFRESH);
        if(ConversionHelper.getOnOffBoolean(params.getHttpParam(PARAM_MANUAL_REFRESH, "off"), false)){
            JSONHelper.putValue(attributes, PARAM_MANUAL_REFRESH, true);
            ml.setAttributes(attributes);
        }
        // Put resolveDepth mode to attributes if true (solves xlink:href links in GetFeature)
        attributes = ml.getAttributes();
        attributes.remove(PARAM_RESOLVE_DEPTH);
        if(ConversionHelper.getOnOffBoolean(params.getHttpParam(PARAM_RESOLVE_DEPTH, "off"), false)){
            JSONHelper.putValue(attributes, PARAM_RESOLVE_DEPTH, true);
            ml.setAttributes(attributes);
        }
        // Get supported projections
        Map<String, Object> capa = GetGtWFSCapabilities.getGtDataStoreCapabilities(
                ml.getUrl(), ml.getVersion(), ml.getUsername(), ml.getPassword(), ml.getSrs_name());
        Set<String> crss = GetGtWFSCapabilities.parseProjections(capa, ml.getName());
        JSONObject capabilities = new JSONObject();
        JSONHelper.put(capabilities, "srs", new JSONArray(crss));
        ml.setCapabilities(capabilities);
        ml.setCapabilitiesLastUpdated(new Date());
    }


    private String validateUrl(final String url) throws ActionParamsException {
        try {
            // check that it's a valid url by creating an URL object...
            new URL(url);
        } catch (MalformedURLException e) {
            throw new ActionParamsException(ERROR_INVALID_FIELD_VALUE + PARAM_LAYER_URL);
        }
        return url;
    }

    private void addPermissionsForRoles(final OskariLayer ml, final User user, final String[] externalIds) {

        OskariLayerResource res = new OskariLayerResource(ml);
        // insert permissions
        for (String externalId : externalIds) {
            final long extId = ConversionHelper.getLong(externalId, -1);
            if (extId != -1 && user.hasRoleWithId(extId)) {
                Permission permission = new Permission();
                permission.setExternalType(Permissions.EXTERNAL_TYPE_ROLE);
                permission.setExternalId(externalId);
                permission.setType(Permissions.PERMISSION_TYPE_VIEW_LAYER);
                res.addPermission(permission);

                permission = new Permission();
                permission.setExternalType(Permissions.EXTERNAL_TYPE_ROLE);
                permission.setExternalId(externalId);
                permission.setType(Permissions.PERMISSION_TYPE_EDIT_LAYER);
                res.addPermission(permission);
            }
        }
        permissionsService.saveResourcePermissions(res);
    }

    private void addPermissionsForRoles(final OskariLayer ml,
                                        final Set<Long> externalIds,
                                        final Set<Long> publishRoleIds,
                                        final Set<Long> downloadRoleIds,
                                        final Set<Long> viewEmbeddedRoleIds) {

        OskariLayerResource res = new OskariLayerResource(ml);
        // insert permissions
        LOG.debug("Adding permission", Permissions.PERMISSION_TYPE_VIEW_LAYER, "for roles:", externalIds);
        for (long externalId : externalIds) {
            Permission permission = new Permission();
            permission.setExternalType(Permissions.EXTERNAL_TYPE_ROLE);
            permission.setExternalId(Long.toString(externalId));
            permission.setType(Permissions.PERMISSION_TYPE_VIEW_LAYER);
            res.addPermission(permission);

            permission = new Permission();
            permission.setExternalType(Permissions.EXTERNAL_TYPE_ROLE);
            permission.setExternalId(Long.toString(externalId));
            permission.setType(Permissions.PERMISSION_TYPE_EDIT_LAYER);
            res.addPermission(permission);
        }

        LOG.debug("Adding permission", Permissions.PERMISSION_TYPE_PUBLISH, "for roles:", publishRoleIds);
        for (long externalId : publishRoleIds) {
            Permission permission = new Permission();
            permission.setExternalType(Permissions.EXTERNAL_TYPE_ROLE);
            permission.setExternalId(Long.toString(externalId));
            permission.setType(Permissions.PERMISSION_TYPE_PUBLISH);
            res.addPermission(permission);
        }

        LOG.debug("Adding permission", Permissions.PERMISSION_TYPE_DOWNLOAD, "for roles:", downloadRoleIds);
        for (long externalId : downloadRoleIds) {
            Permission permission = new Permission();
            permission.setExternalType(Permissions.EXTERNAL_TYPE_ROLE);
            permission.setExternalId(Long.toString(externalId));
            permission.setType(Permissions.PERMISSION_TYPE_DOWNLOAD);
            res.addPermission(permission);
        }

        LOG.debug("Adding permission", Permissions.PERMISSION_TYPE_VIEW_PUBLISHED, "for roles:", viewEmbeddedRoleIds);
        for (long externalId : viewEmbeddedRoleIds) {
            Permission permission = new Permission();
            permission.setExternalType(Permissions.EXTERNAL_TYPE_ROLE);
            permission.setExternalId(Long.toString(externalId));
            permission.setType(Permissions.PERMISSION_TYPE_VIEW_PUBLISHED);
            res.addPermission(permission);
        }

        permissionsService.saveResourcePermissions(res);
    }
}
