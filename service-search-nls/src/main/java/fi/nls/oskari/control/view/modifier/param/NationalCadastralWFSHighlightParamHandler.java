package fi.nls.oskari.control.view.modifier.param;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONArray;
import org.json.JSONObject;

import fi.nls.oskari.search.channel.KTJkiiSearchChannel;
//import fi.mml.portti.service.ogc.executor.FindFeatureBboxById;
import fi.mml.portti.service.search.Query;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.mml.portti.service.search.SearchService;
import fi.mml.portti.service.search.SearchServiceImpl;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.domain.geo.comparator.LatComparator;
import fi.nls.oskari.domain.geo.comparator.LonComparator;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;

@OskariViewModifier("nationalCadastralReferenceHighlight")
public class NationalCadastralWFSHighlightParamHandler extends WFSHighlightParamHandler {

    private static final Logger log = LogFactory.getLogger(NationalCadastralWFSHighlightParamHandler.class);
    private static SearchService searchService = new SearchServiceImpl();
//    private final FindFeatureBboxById mybboxfinder = new FindFeatureBboxById();

    @Override
    public boolean handleParam(ModifierParams params)
            throws ModifierException {
        if(params.getParamValue() == null) {
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
            
            String wfsLayerId = null;
            try {
                wfsLayerId = postprocessorState.getString("highlightFeatureLayerId");
            }
            catch(Exception ex) {
                // fallback to constant layer 
                wfsLayerId = WFSHighlightParamHandler.NATIONAL_CADASTRAL_REFERENCE_LAYER_ID;
                postprocessorState.put("highlightFeatureLayerId", WFSHighlightParamHandler.NATIONAL_CADASTRAL_REFERENCE_LAYER_ID);
            }
            postprocessorState.put("featurePoints", calculateBbox(list, wfsLayerId));
        }
        catch(Exception ex) {
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

    private JSONArray calculateBbox(List<SearchResultItem> list,
            String wfslayerId)  {

        final JSONArray bbox = new JSONArray();

        // bbox is returned by GetFeature
        // Axis order is reverse because of historical reasons
        // lon should be  west-east and lat south-north
        if (list.size() > 0)
        {
            SearchResultItem item = list.get(0);
            if (item.getWestBoundLongitude() != null)
            {
                JSONObject bottomLeft = new JSONObject();
                JSONHelper.putValue(bottomLeft, "lat",item.getWestBoundLongitude() );
                JSONHelper.putValue(bottomLeft, "lon", item.getSouthBoundLatitude());

                JSONObject topRight = new JSONObject();
                JSONHelper.putValue(topRight, "lat", item.getEastBoundLongitude());
                JSONHelper.putValue(topRight, "lon", item.getNorthBoundLatitude());

                bbox.put(bottomLeft);
                bbox.put(topRight);

                return bbox;
            }

        }

        //TODO: do we need below code any more
        // Find bbox of selected wfs features
        // Method processParcelFeatureResponseFromStream of class KTJkiiWFSSearchChannelImpl should include the geometry
//        final double[] mimaxy = mybboxfinder.getFeatureBbox(list, wfslayerId);

        final ArrayList<Point> points = new ArrayList<Point>();
        // Min-max based on polygons
/*        if (mimaxy[0] > 0) {
            Point pointlo = new Point(mimaxy[1], mimaxy[0]);
            points.add(pointlo);
            Point pointhi = new Point(mimaxy[3], mimaxy[2]);
            points.add(pointhi);
        } else {*/
            for (SearchResultItem item : list) {

                Point point = new Point(ConversionHelper.getDouble(item.getLat(),
                        -1), ConversionHelper.getDouble(item.getLon(), -1));
                points.add(point);

            }
//        }

        if (points.size() > 1) {

            Collections.sort(points, new LatComparator());

            Point left = points.get(0);
            Point right = points.get(points.size() - 1);

            Collections.sort(points, new LonComparator());

            Point top = points.get(0);
            Point bottom = points.get(points.size() - 1);

            JSONObject bottomLeft = new JSONObject();
            JSONHelper.putValue(bottomLeft, "lon", left.getLon());
            JSONHelper.putValue(bottomLeft, "lat", bottom.getLat());

            JSONObject topRight = new JSONObject();
            JSONHelper.putValue(topRight, "lon", right.getLon());
            JSONHelper.putValue(topRight, "lat", top.getLat());

            bbox.put(bottomLeft);
            bbox.put(topRight);

        } else {
            JSONObject point = new JSONObject();
            if (!points.isEmpty()) {
                JSONHelper.putValue(point, "lon", points.get(0).getLon());
                JSONHelper.putValue(point, "lat", points.get(0).getLat());
                bbox.put(point);
            }
        }

        return bbox;
    }
}
