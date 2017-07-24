package fi.nls.oskari.control.view.modifier.bundle;

import fi.mml.portti.service.search.SearchServiceImpl;
import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONException;
import org.json.JSONObject;

@OskariViewModifier("search")
public class SearchHandler extends BundleHandler {

    private static final Logger log = LogFactory.getLogger(SearchHandler.class);
    private static boolean autocomplete = false;

    @Override
    public void init() {
        super.init();
        SearchServiceImpl searchService = new SearchServiceImpl();
        autocomplete = searchService.isAvailableAutocompleteChannels();
    }

    /**
     * Updates the query URL from the myplaces2 configuration
     * @param params
     * @return
     * @throws ModifierException
     */
    public boolean modifyBundle(final ModifierParams params) throws ModifierException {
        final JSONObject searchConfig = getBundleConfig(params.getConfig());
        if (searchConfig == null) {
            return false;
        }
        try {
            searchConfig.put("autocomplete", autocomplete);
            return true;
        } catch (JSONException ignored) {
            log.warn("Unable to set autocomplete");
        }
        return false;
    }
}
