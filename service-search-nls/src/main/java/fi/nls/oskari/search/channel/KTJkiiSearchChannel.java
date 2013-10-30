package fi.nls.oskari.search.channel;

import fi.nls.oskari.search.ktjkiiwfs.KTJkiiWFSSearchChannel.RegisterUnitId;
import fi.nls.oskari.search.ktjkiiwfs.KTJkiiWFSSearchChannelImpl;
import fi.nls.oskari.search.ktjkiiwfs.RegisterUnitParcelSearchResult;
import fi.mml.portti.service.search.ChannelSearchResult;
import fi.mml.portti.service.search.IllegalSearchCriteriaException;
import fi.mml.portti.service.search.SearchCriteria;
import fi.mml.portti.service.search.SearchResultItem;
import fi.nls.oskari.search.util.SearchUtil;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

import java.net.URL;
import java.util.List;

public class KTJkiiSearchChannel implements SearchableChannel {

    private String serviceURL = null;
    private String serviceKTJHost = null;
    private String serviceKTJAuth = null;
    private Logger log = LogFactory.getLogger(this.getClass());
    private String defaultServiceUrl = "https://ws.nls.fi/ktjkii/wfs/wfs";

    public static final String ID = "KTJ_KII_CHANNEL";
    public static final String SERVICE_URL = "service.url";
    public static final String SERVICE_KTJ_HOST = "service.host";
    public static final String SERVICE_KTJ_AUTH = "service.authentization";

    public void setProperty(String propertyName, String propertyValue) {
        if (SERVICE_URL.equals(propertyName)) {
            serviceURL = propertyValue;
            log.debug("ServiceURL set to " + serviceURL);
        } else if (SERVICE_KTJ_HOST.equals(propertyName)) {
            serviceKTJHost = propertyValue;
            log.debug("serviceKTJHost set to " + serviceKTJHost);
        } else if (SERVICE_KTJ_AUTH.equals(propertyName)) {
            serviceKTJAuth = propertyValue;
            log.debug("serviceKTJAuth set to " + serviceKTJAuth);
        } else {
            log.warn("Unknown property for " + ID + " search channel: " + propertyName);
        }
    }
    
    public String getId() {
        return ID;
    }
    
    public ChannelSearchResult doSearch(SearchCriteria searchCriteria)
            throws IllegalSearchCriteriaException {
        ChannelSearchResult searchResultList = new ChannelSearchResult();

        String registerUnitID = searchCriteria.getSearchString();

        KTJkiiWFSSearchChannelImpl impl = new KTJkiiWFSSearchChannelImpl();

        try {

            defaultServiceUrl = serviceURL;

            URL serviceUrl = new URL(defaultServiceUrl);

            impl.setServiceURL(serviceUrl);
            impl.setHost(serviceKTJHost);
            impl.setAuth(serviceKTJAuth);


            RegisterUnitId registerUnitId = 
                    impl.convertRequestStringToRegisterUnitID(
                            searchCriteria.getSearchString());

            if (registerUnitId == null) {
                log.debug("RegisterUnitId not found for query: ", searchCriteria.getSearchString());
                return searchResultList;
            }
            
            List<RegisterUnitParcelSearchResult> results = impl
                    .searchByRegisterUnitIdWithParcelFeature(registerUnitId);

            if (results == null) {
                log.debug("RegisterUnitParcelSearchResult was null for query: '", 
                        searchCriteria.getSearchString(), "' and RegisterUnitId: ", 
                        registerUnitId.getValue());
                return searchResultList;
            }

            for (RegisterUnitParcelSearchResult rupsr : results) {
                SearchResultItem item = new SearchResultItem();

                item.setRank(SearchUtil.RANK_OTHER);
                item.setTitle(registerUnitID);
                item.setContentURL(rupsr.getE() + "_" + rupsr.getN());

                item.setLon(rupsr.getLon());
                item.setLat(rupsr.getLat());

                item.setDescription("Kiinteistötunnus");
                item.setActionURL("Kiinteistötunnus");
                item.setType(SearchUtil.getLocationType("Kiinteistötunnus_" +
                        SearchUtil.getLocaleCode(searchCriteria.getLocale())));
                item.setMapURL(SearchUtil.getMapURL(searchCriteria.getLocale()));
                item.setVillage("");
                item.setZoomLevel("11");
                // resource id == feature id
                item.setResourceId(rupsr.getGmlID());
                item.setResourceNameSpace(defaultServiceUrl);
                searchResultList.addItem(item);
            }

        } catch (Exception e) {
            // never actually throws IllegalSearchCriteriaException
            // since its thrown only by QueryParser.parse() which is not used here
            // so we can catch all
            log.error(e, "Search resulted in an exception for query: ", 
                    searchCriteria.getSearchString(), 
                    "- ServiceURL used was:", defaultServiceUrl);
        }

        return searchResultList;
    }

}
