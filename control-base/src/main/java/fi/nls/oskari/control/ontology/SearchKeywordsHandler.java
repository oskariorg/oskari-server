package fi.nls.oskari.control.ontology;

import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceFactory;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.ontology.domain.Keyword;
import fi.nls.oskari.ontology.service.KeywordService;
import fi.nls.oskari.ontology.service.KeywordServiceMybatisImpl;
import fi.nls.oskari.util.*;
import fi.nls.oskari.wfs.WFSCapabilitiesParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.service.util.ServiceFactory;

import java.util.*;

/**
 * @author SMAKINEN
 */
@OskariActionRoute("SearchKeywords")
public class SearchKeywordsHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(SearchKeywordsHandler.class);
    private KeywordService service = null;
    private PermissionsService permissionsService = null;

    public void init() {
        if (service == null) {
            setService(new KeywordServiceMybatisImpl());
        }
        if (permissionsService == null) {
            setPermissionsService(new PermissionsServiceIbatisImpl());
        }
    }

    public void setService(KeywordService service) {
        this.service = service;
    }

    public void setPermissionsService(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    private static boolean listsOverlap(List<Long> list1, List<Long> list2) {
        for (Long long1 : list1) {
            if (list2.contains(long1)) {
                return true;
            }
        }
        return false;
    }

    public void handleAction(ActionParameters params) throws ActionException {
        if(params.getUser().isAdmin()) {
            // TODO: remove this once we have a real populating code
            // admins can now call route with populate action
            forDebugging(params);
        }
        final String keyword = params.getHttpParam("keyword", null);
        final String lang = params.getHttpParam("lang", null);
        if(keyword == null) {
            throw new ActionParamsException("Keyword was null!");
        }

        if(lang == null) {
            throw new ActionParamsException("Lang was null!");
        }

        List<Map<String,Object>> permittedLayers =  permissionsService.getListOfMaplayerIdsForViewPermissionByUser(params.getUser(), false);
        List<Long> idList = new ArrayList<Long>();
        for (Map<String,Object> entry : permittedLayers) {
            idList.add(Long.parseLong(String.valueOf(entry.get("id"))));
        }

        log.debug("Permitted layers: ", idList);

        Keyword exactMatch = service.findExactKeyword(keyword, lang);
        log.debug("Found exact match", exactMatch);

        final JSONArray list = new JSONArray();

        final List<Keyword> nearMatches = service.findKeywordsMatching(keyword, lang);
        log.debug("Found near matches", nearMatches.size());

        // see if we've found any layers
        List<Keyword> synonyms = null, parents = null, siblings = null;
        boolean foundLayers = false;
        if (exactMatch == null) {
            // add faux keyword so we can shove it in json
            exactMatch = new Keyword();
            exactMatch.setId(-1l);
            exactMatch.setValue(keyword.toLowerCase().trim());
            exactMatch.setLang(lang);
        } else {
            synonyms = service.findSynonyms(exactMatch.getId(), lang);
            log.debug("Found synonyms", synonyms);
            foundLayers =  listsOverlap(exactMatch.getLayerIds(), idList);
            if (foundLayers) {
                log.debug("Found layers in exact match");
            }
        }

        addKeywordToJSONArray(null, exactMatch, list, idList);

        // FIXME foundLayers checks have to check if the user has view permission on the layer

        if (!foundLayers) {
            // exact match doesn't exist or doesn't have any layers, check synonyms
            if (synonyms != null) {
                for (Keyword synonym : synonyms) {
                    if (listsOverlap(synonym.getLayerIds(), idList)) {
                        foundLayers = true;
                        log.debug("Found layers in synonyms");
                        break;
                    }
                }
            }
            if (!foundLayers) {
                // exact and synonyms don't have layers, check near matches
                for (Keyword nearMatch : nearMatches) {
                    if (listsOverlap(nearMatch.getLayerIds(), idList)) {
                        foundLayers = true;
                        log.debug("Found layers in near matches");
                        break;
                    }
                }
                if (!foundLayers && exactMatch != null) {
                    // still no layers, get parents and siblings
                    parents = service.findParents(exactMatch.getId(), lang);
                    log.debug("Found parents", parents);
                    siblings = service.findSiblings(exactMatch.getId(), lang);
                    log.debug("Found siblingss", siblings);
                }
            }
        }

        if (synonyms != null) {
            for (Keyword synonym : synonyms) {
                addKeywordToJSONArray("syn", synonym, list, idList);
            }
        }

        for (Keyword nearMatch : nearMatches) {
            addKeywordToJSONArray(null, nearMatch, list, idList);
        }

        if (parents != null) {
            for(Keyword parent : parents) {
                addKeywordToJSONArray("yk", parent, list, idList);
            }
        }

        if (siblings != null) {
            for(Keyword sibling: siblings) {
                addKeywordToJSONArray("lk", sibling, list, idList);
            }
        }

        ResponseHelper.writeResponse(params, list);
    }

    private static void addKeywordToJSONArray(String relation, Keyword keyword, JSONArray list, List<Long> permittedLayers) {
        if (keyword == null) {
            log.debug("Null keyword");
            return;
        }
        final JSONObject obj = new JSONObject();
        JSONHelper.putValue(obj, "id", keyword.getId());
        JSONHelper.putValue(obj, "keyword", keyword.getValue());
        JSONHelper.putValue(obj, "lang", keyword.getLang());
        JSONHelper.putValue(obj, "type", relation);
        JSONHelper.putValue(obj, "shortcut", false);
        final JSONArray layers = new JSONArray();
        JSONHelper.putValue(obj, "layers", layers);
        for(Long id : keyword.getLayerIds()) {
            // fugly as hell
            if (permittedLayers.contains(id)) {
                layers.put(id);
            } else {
                log.debug("Permission not granted for layer ", id);
            }
        }
        list.put(obj);
    }

    /**
     * Convenience method for adding keywords and populating the keywords db
     * FIXME: REMOVE THIS METHOD AFTER DEBUGGING
     * TODO: REMOVE THIS METHOD AFTER DEBUGGING
     * @param params
     */
    private void forDebugging(ActionParameters params) {

        // TODO: remove add/populate through action route or check permissions/link to map layer
        final String isPopulate = params.getHttpParam("populate", "false");
        if("true".equals(isPopulate)) {
            populateKeywords();
            return;
        }

        final String keyword = params.getHttpParam("keyword", null);
        // /web/fi/kartta?p_p_id=Portti2Map_WAR_portti2mapportlet&p_p_lifecycle=2&action_route=SearchKeywords&keyword=urgh
        // /web/fi/kartta?p_p_id=Portti2Map_WAR_portti2mapportlet&p_p_lifecycle=2&action_route=SearchKeywords&keyword=&populate=true
        // /web/fi/kartta?p_p_id=Portti2Map_WAR_portti2mapportlet&p_p_lifecycle=2&action_route=SearchKeywords&keyword=urgh&add=true
        // /web/fi/kartta?p_p_id=Portti2Map_WAR_portti2mapportlet&p_p_lifecycle=2&action_route=SearchKeywords&keyword=urgh&add=true&layer=35
        final String isAdd = params.getHttpParam("add", "false");
        final long layerId = ConversionHelper.getLong(params.getHttpParam("layer"), -1);
        if("true".equals(isAdd) || layerId != -1) {
            Keyword w = new Keyword();
            w.setLang(params.getLocale().getLanguage());
            w.setValue(keyword);
            w.setUri("");
            w.setEditable(false);
            log.debug("Adding keyword", w);
            service.addKeyword(w);
            if(layerId != -1) {
                log.debug("Linking word:", w.getId(),"to layer", layerId);
                service.linkKeywordToLayer(w.getId(), layerId);
            }
        }
    }

    /* ********************************************************************************************
     * TODO: The following methods shouldn't be here, but on some timer class populating the keywords
     */
    private static OskariLayerService layerService = ServiceFactory.getMapLayerService();
    private static final WFSCapabilitiesParser wfsCapabilitiesparser = new WFSCapabilitiesParser();
    private GetLayerKeywords getLayerKeywords = new GetLayerKeywords();
    private final String[] EMPTY_RESULT = new String[0];

    private void populateKeywords() {
        List<OskariLayer> layers = layerService.findAll();
        for(OskariLayer layer : layers) {
            populateKeywordsForLayer(layer);
        }
    }

    public void populateKeywordsForLayer(final OskariLayer layer) /*throws ServiceException*/ {

        final String[] keys = getKeywordsForLayer(layer);
        for(String key : keys) {
            Keyword w = new Keyword();
            w.setValue(key);
            w.setEditable(true);
            service.addKeyword(w);
            service.linkKeywordToLayer(w.getId(), (long) layer.getId());
        }
        log.debug("Added", keys.length, "keywords for layer", layer.getId());
    }

    private String[] getKeywordsForLayer(final OskariLayer layer) {
        Set<String> layerKeywords = new HashSet<>();
        try {
            if(OskariLayer.TYPE_WMS.equals(layer.getType())) {
                WebMapService wms = WebMapServiceFactory.buildWebMapService(layer);
                if (wms == null || wms.getKeywords() == null) {
                    log.warn("Error parsing keywords for layer", layer);
                    return EMPTY_RESULT;
                }
                layerKeywords.addAll(Arrays.asList(wms.getKeywords()));
            }
            else if(OskariLayer.TYPE_WFS.equals(layer.getType())) {
                layerKeywords.addAll(Arrays.asList(wfsCapabilitiesparser.getKeywordsForLayer(layer)));
            }
            if (layer.getMetadataId() != null) {
                getLayerKeywords.updateLayerKeywords(layer.getId(), layer.getMetadataId());
            }
            return layerKeywords.toArray(new String[layerKeywords.size()]);
        } catch (Exception e) {
            log.error(e, "Error parsing keywords for layer", layer);
        }
        return EMPTY_RESULT;
    }


}