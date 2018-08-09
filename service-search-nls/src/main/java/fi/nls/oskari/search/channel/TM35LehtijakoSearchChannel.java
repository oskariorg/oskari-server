package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import fi.nls.oskari.search.util.TM35MapSheetDivision;
import fi.nls.oskari.util.ConversionHelper;

@Oskari("TM35LEHTIJAKO_CHANNEL")
public class TM35LehtijakoSearchChannel extends SearchChannel {

    private static final int DEFAULT_TM35_SCALE = 5000;
    private static final String PARAM_TM35_SCALE = "scale";

    public Capabilities getCapabilities() {
        return Capabilities.BOTH;
    }

    /**
     * Find center of a map sheet
     */
    @Override
    public ChannelSearchResult doSearch(SearchCriteria searchCriteria) {
        String mapSheet = searchCriteria.getSearchString();

        if (!TM35MapSheetDivision.validate(mapSheet)) {
            return new ChannelSearchResult();
        }

        int[] bbox = TM35MapSheetDivision.getBoundingBox(mapSheet);
        double centerNorth = bbox[1] + (bbox[3] - bbox[1]) * 0.5;
        double centerEast = bbox[0] + (bbox[2] - bbox[0]) * 0.5;

        SearchResultItem item = new SearchResultItem();
        item.setType("tm35lehtijako");
        item.setTitle(mapSheet);
        item.setLat(centerNorth);
        item.setLon(centerEast);

        ChannelSearchResult result = new ChannelSearchResult();
        result.addItem(item);

        return result;
    }

    /**
     * Find map sheet by coordinates
     */
    public ChannelSearchResult reverseGeocode(SearchCriteria searchCriteria) {
        double lat = searchCriteria.getLat();
        double lon = searchCriteria.getLon();
        String epsg = searchCriteria.getSRS();
        Point p = ProjectionHelper.transformPoint(lon, lat, epsg, "EPSG:3067");
        double east = p.getLon();
        double north = p.getLat();

        if (!TM35MapSheetDivision.validate(east, north)) {
            return new ChannelSearchResult();
        }

        // Must be one of: 200000,100000,50000,25000,20000,10000,5000
        String paramScale = (String) searchCriteria.getParam(PARAM_TM35_SCALE);
        int scale = ConversionHelper.getInt(paramScale, DEFAULT_TM35_SCALE);
        int charLen = TM35MapSheetDivision.getLen(scale);

        String mapSheet = TM35MapSheetDivision.getMapSheetByCoordinate(east, north, charLen, false);

        SearchResultItem item = new SearchResultItem();
        item.setType("tm35lehtijako");
        item.setTitle(mapSheet);
        item.setDescription(mapSheet);
        item.setLat(lat);
        item.setLon(lon);

        ChannelSearchResult result = new ChannelSearchResult();
        result.addItem(item);

        return result;
    }

}
