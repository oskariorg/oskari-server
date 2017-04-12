package fi.nls.oskari.control.view.modifier.param;

import fi.mml.portti.service.search.*;
import fi.nls.oskari.SearchWorker;
import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.view.modifier.ParamHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.channel.KTJkiiSearchChannel;
import fi.nls.oskari.search.channel.MaastoAddressChannelSearchService;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import fi.nls.oskari.view.modifier.ViewModifier;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Locale;

@OskariViewModifier("nationalCadastralReference")
public class NationalCadastralRefParamHandler extends ParamHandler {

    private static SearchService searchService = new SearchServiceImpl();
    private static final Logger log = LogFactory.getLogger(NationalCadastralRefParamHandler.class);
    
    public boolean handleParam(final ModifierParams params) throws ModifierException {
        if(params.getParamValue() == null) {
            return false;
        }

        final String cadastralRef = params.getParamValue().replace('_', ' ');
        // we can use fi as language since we only get coordinates, could
        // get it from published map if needed
        final ArrayList<String[]> latlon_list = getCoordinatesFromNatCadRef(
                params.getLocale(), cadastralRef, PropertyUtil.getDefaultLanguage());
        // TODO: KTJ search channel now returns "palstat" instead of
        // "kiinteistöt" -> one ref returns multiple results
        // we need to think this through
        // if (latlon_list.size() == 1) {
        log.debug("National cadastral reference coordinates", latlon_list);
        if (latlon_list.size() > 0) {
            final JSONObject state = getBundleState(params.getConfig(), ViewModifier.BUNDLE_MAPFULL);
            try {
                state.put(KEY_EAST, latlon_list.get(0)[0]);
                state.put(KEY_NORTH, latlon_list.get(0)[1]);
                return true;
            } catch (JSONException je) {
                throw new ModifierException("Could not set coordinates from cadastral ref.");
            }
        }

        // TODO: need error handling if address not found or multiple
        // address found [http://haisuli.nls.fi/jira/browse/PORTTISK-1078]
        return false;
    }

    private ArrayList<String[]> getCoordinatesFromNatCadRef(Locale locale,
            String searchString, String publishedMapLanguage) {

        ArrayList<String[]> lat_lon = new ArrayList<String[]>();

        if (!"".equalsIgnoreCase(publishedMapLanguage)
                && publishedMapLanguage != null) {
            locale = new Locale(publishedMapLanguage);
        }

        String isLegal = SearchWorker.checkLegalSearch(searchString);
        if (isLegal.equals(SearchWorker.STR_TRUE)) {
            try {
                searchString = URLDecoder.decode(searchString, "UTF-8");
                SearchCriteria sc = new SearchCriteria();
                sc.addChannel(MaastoAddressChannelSearchService.ID);
                sc.addChannel(KTJkiiSearchChannel.ID);
                sc.setSearchString(searchString);
                sc.setLocale(locale.getLanguage());

                Query query = searchService.doSearch(sc);

                for (SearchResultItem item : query.findResult(
                        MaastoAddressChannelSearchService.ID)
                        .getSearchResultItems()) {
                    lat_lon.add(item.getContentURL().split("_"));
                }

                for (SearchResultItem item : query.findResult(
                        KTJkiiSearchChannel.ID).getSearchResultItems()) {
                    lat_lon.add(item.getContentURL().split("_"));
                }

            } catch (UnsupportedEncodingException e) {
                System.err.println("Problem encoding searchString. ");
            }

        }

        return lat_lon;
    }
}
