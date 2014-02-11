package fi.nls.oskari.control.view.modifier.param;

import fi.mml.portti.service.search.*;
import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.channel.KTJkiiSearchChannel;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

@OskariViewModifier("nationalCadastralReferenceHighlight")
public class NationalCadastralWFSHighlightParamHandler extends WFSHighlightParamHandler {

    private static final Logger log = LogFactory.getLogger(NationalCadastralWFSHighlightParamHandler.class);
    private static SearchService searchService = new SearchServiceImpl();

    @Override
    public boolean handleParam(ModifierParams params)
            throws ModifierException {
        if (params.getParamValue() == null) {
            return false;
        }
        final JSONArray featureIdList = new JSONArray();

        List<SearchResultItem> list = getKTJfeature(params.getParamValue(),
                params.getLocale().getLanguage());
        for (SearchResultItem item : list) {
            featureIdList.put(item.getResourceId());
        }
        try {
            final JSONObject postprocessorState = getPostProcessorState(params);
            postprocessorState.put("highlightFeatureId", featureIdList);

            if(!postprocessorState.has(STATE_LAYERID_KEY)) {
                // failsafe - set id if not defined
                postprocessorState.put(STATE_LAYERID_KEY, NATIONAL_CADASTRAL_REFERENCE_LAYER_ID);
            }
            postprocessorState.put("featurePoints", calculateBbox(list));
        } catch (Exception ex) {
            log.error(ex, "Couldn't insert features to postprocessor bundle state");
        }

        return featureIdList.length() > 0;
    }

    private List<SearchResultItem> getKTJfeature(final String param, final String language) {

        final SearchCriteria sc = new SearchCriteria();
        sc.addChannel(KTJkiiSearchChannel.ID);
        sc.setSearchString(param);
        sc.setLocale(language);

        final Query query = searchService.doSearch(sc);
        return query.findResult(KTJkiiSearchChannel.ID).getSearchResultItems();
    }

    private JSONArray calculateBbox(List<SearchResultItem> list) {

        final JSONArray bbox = new JSONArray();
        // bbox is returned by GetFeature
        if (!list.isEmpty()) {
            // we need only data from the first item
            SearchResultItem item = list.get(0);
            if (item.getWestBoundLongitude() != null) {
                JSONObject bottomLeft = new JSONObject();
                JSONHelper.putValue(bottomLeft, "lon", item.getWestBoundLongitude());
                JSONHelper.putValue(bottomLeft, "lat", item.getSouthBoundLatitude());

                JSONObject topRight = new JSONObject();
                JSONHelper.putValue(topRight, "lon", item.getEastBoundLongitude());
                JSONHelper.putValue(topRight, "lat", item.getNorthBoundLatitude());

                bbox.put(bottomLeft);
                bbox.put(topRight);

            }
        }
        return bbox;
    }
}
