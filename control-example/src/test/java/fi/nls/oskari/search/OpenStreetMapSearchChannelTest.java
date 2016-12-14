package fi.nls.oskari.search;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.map.geometry.ProjectionHelper;
import org.geotools.referencing.CRS;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by SMAKINEN on 15.9.2015.
 */
public class OpenStreetMapSearchChannelTest {


    /**
     * This test can be used to check how the coordinate order works with different combinations of SRS/ forced coordinate order
     * @throws Exception
     */
    @Test
    @Ignore
    public void testCoordinateTransforms() throws Exception {

        OpenStreetMapSearchChannel channel = new OpenStreetMapSearchChannel();
        System.setProperty("http.proxyHost", "wwwp.nls.fi");
        System.setProperty("http.proxyPort", "800");
        channel.init();
        SearchCriteria sc = new SearchCriteria();
        sc.setSearchString("Tampere");
        sc.setSRS("EPSG:3067");
        //sc.setSRS("EPSG:4236");
        sc.setLocale("en");
        ChannelSearchResult result = channel.doSearch(sc);
        System.out.println(ProjectionHelper.isFirstAxisNorth(CRS.decode("EPSG:4326")));
        System.out.println(ProjectionHelper.isFirstAxisNorth(CRS.decode(sc.getSRS())));
        for(SearchResultItem item : result.getSearchResultItems()) {
            System.out.println(item.getLat() + "/" + item.getLon());
        }
    }

}