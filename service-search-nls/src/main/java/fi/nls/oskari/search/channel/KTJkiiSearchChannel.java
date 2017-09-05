package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.IllegalSearchCriteriaException;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.search.ktjkiiwfs.KTJkiiWFSSearchChannel.RegisterUnitId;
import fi.nls.oskari.search.ktjkiiwfs.KTJkiiWFSSearchChannelImpl;
import fi.nls.oskari.search.ktjkiiwfs.RegisterUnitParcelSearchResult;
import fi.nls.oskari.search.util.SearchUtil;
import fi.nls.oskari.util.PropertyUtil;

import java.io.IOException;
import java.util.List;

@Oskari(KTJkiiSearchChannel.ID)
public class KTJkiiSearchChannel extends SearchChannel {

    private String serviceURL = null;
    private Logger log = LogFactory.getLogger(this.getClass());
    private static final String PROPERTY_SERVICE_URL = "search.channel.KTJ_KII_CHANNEL.service.url";

    public static final String ID = "KTJ_KII_CHANNEL";

    @Override
    public void init() {
        super.init();
        serviceURL = PropertyUtil.getOptional(PROPERTY_SERVICE_URL);
    }

    public ChannelSearchResult doSearch(SearchCriteria searchCriteria)
            throws IllegalSearchCriteriaException {
        if(serviceURL == null) {
            log.warn("ServiceURL not configured. Add property with key", PROPERTY_SERVICE_URL);
            return null;
        }
        ChannelSearchResult searchResultList = new ChannelSearchResult();

        String registerUnitID = searchCriteria.getSearchString();

        KTJkiiWFSSearchChannelImpl impl = new KTJkiiWFSSearchChannelImpl();

        try {
            impl.setConnectionProvider(this);

            RegisterUnitId registerUnitId = 
                    impl.convertRequestStringToRegisterUnitID(
                            searchCriteria.getSearchString());

            if (registerUnitId == null) {
                log.debug("RegisterUnitId not found for query: ", searchCriteria.getSearchString());
                return searchResultList;
            }
            
            List<RegisterUnitParcelSearchResult> results = impl
                    .searchByRegisterUnitIdWithParcelFeature(registerUnitId);

            if (results == null || results.isEmpty()) {
                log.info("No result for: '",
                        searchCriteria.getSearchString(), "' and RegisterUnitId: '",
                        registerUnitId.getValue(), "' ServiceURL:", serviceURL);
                return searchResultList;
            }

            for (RegisterUnitParcelSearchResult rupsr : results) {
                SearchResultItem item = new SearchResultItem();

                item.setRank(SearchUtil.RANK_OTHER);
                item.setTitle(registerUnitID);
                item.setContentURL(rupsr.getE() + "_" + rupsr.getN());

                item.setLon(rupsr.getLon());
                item.setLat(rupsr.getLat());
                if(rupsr.getBBOX() != null )
                {
                    String[] bbox =  rupsr.getBBOX().split(" ");
                    if (bbox.length == 4)
                    {
                        item.setWestBoundLongitude(bbox[0]);
                        item.setSouthBoundLatitude(bbox[1]);
                        item.setEastBoundLongitude(bbox[2]);
                        item.setNorthBoundLatitude(bbox[3]);
                    }
                }
                // FIXME: Kiinteistötunnus...
                item.setDescription("Kiinteistötunnus");
                item.setActionURL("Kiinteistötunnus");
                item.setType(SearchUtil.getLocationType("Kiinteistötunnus_" +
                        SearchUtil.getLocaleCode(searchCriteria.getLocale())));
                item.setMapURL(SearchUtil.getMapURL(searchCriteria.getLocale()));
                item.setRegion("");
                // resource id == feature id
                item.setResourceId(rupsr.getGmlID());
                item.setResourceNameSpace(serviceURL);
                searchResultList.addItem(item);
            }

        }
        catch (IOException e) {
            // If the service is down we don't care about the stack trace
            log.warn("Error connecting to service/reading response:", e.getMessage(), "- URL:", serviceURL);
            searchResultList.setException(e);
            searchResultList.setQueryFailed(true);
            return searchResultList;
        }
        catch (Exception e) {
            // never actually throws IllegalSearchCriteriaException
            // since its thrown only by QueryParser.parse() which is not used here
            // so we can catch all
            log.error(e, "Search resulted in an exception for query: ", 
                    searchCriteria.getSearchString(), 
                    "- ServiceURL used was:", serviceURL);
        }

        return searchResultList;
    }
}
