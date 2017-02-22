package fi.nls.oskari.control.view.modifier.param;

import fi.mml.portti.service.search.*;
import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.view.modifier.ParamHandler;
import fi.nls.oskari.control.view.modifier.bundle.MapfullHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.channel.MaastoAddressChannelSearchService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import fi.nls.oskari.view.modifier.ViewModifier;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

@OskariViewModifier("address")
public class AddressParamHandler extends ParamHandler {

    private static final Logger log = LogFactory.getLogger(AddressParamHandler.class);
    //private static final String PARAM_ADDRESS = "address";
    private static SearchService searchService = new SearchServiceImpl();

    @Override
    public int getPriority() {
        return 10;
    }

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        if(params.getParamValue() == null) {
            return false;
        }
        final ArrayList<String[]> coordinates = getCoordinatesFromAddress(params.getParamValue(), params.getLocale());
        if(coordinates.size() != 1) {
            log.debug("Got multiple coordinates for address", coordinates);
            // not found or multiple found -> open search plugin with param value
            final JSONObject config = getBundleConfig(params.getConfig(), ViewModifier.BUNDLE_MAPFULL);
            final JSONObject searchplugin = MapfullHandler.getPlugin(MapfullHandler.PLUGIN_SEARCH, config);
            if(searchplugin != null) {
                log.debug("Modifying search plugin config", searchplugin);
                // get existing config or initialize new node
                final JSONObject searchConfig = initPluginConfig(searchplugin, MapfullHandler.KEY_CONFIG);
                if(searchConfig != null) {
                    // setup initial search for plugin
                    JSONHelper.putValue(searchConfig, "searchKey", params.getParamValue());
                }
            }

            return false;
        }
        // found one -> set mapfull location
        final String[] coords = coordinates.get(0);
        final JSONObject state = getBundleState(params.getConfig(), ViewModifier.BUNDLE_MAPFULL);
        try {
            state.put(KEY_EAST, coords[0]);
            state.put(KEY_NORTH, coords[1]);
        } catch (Exception ex) {
            throw new ModifierException("Got address coordinates but could not modify location.");
        }
        return true;
    }
    protected ArrayList<String[]> getCoordinatesFromAddress(
                              String searchString, Locale locale) {

        final ArrayList<String[]> lat_lon = new ArrayList<String[]>();

        final SearchCriteria sc = new SearchCriteria();
        sc.addChannel(MaastoAddressChannelSearchService.ID);
        //searchString = URLDecoder.decode(searchString, "UTF-8");
        sc.setSearchString(searchString);
        sc.setLocale(locale.getLanguage());

        try {
            final Query query = searchService.doSearch(sc);
            final ChannelSearchResult result = query.findResult(MaastoAddressChannelSearchService.ID);
            for(SearchResultItem item : result.getSearchResultItems()) {
                lat_lon.add(item.getContentURL().split("_"));
            }
        } catch (Exception e) {
            log.error(e, "Problems with address search:", sc);
        }

        return lat_lon;
    }
    // returns existing node or initializes new one if not existing
    private JSONObject initPluginConfig(final JSONObject content, final String key) {
        if(content == null || key == null) {
            return null;
        }
        try {
            if(content.has(key)) {
                return content.getJSONObject(key);
            }
        } catch (Exception e) {
            log.warn(e, "Unable to get json object with key", key, "config from", content);
        }
        final JSONObject conf = new JSONObject();
        JSONHelper.putValue(content, key, conf);
        return conf;
    }
    
}
