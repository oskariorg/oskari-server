package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.nls.oskari.search.util.SearchUtil;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class MaastoAddressChannelSearchService extends BaseWfsAddressChannelSearchService{

    private Logger log = LogFactory.getLogger(this.getClass());

    private String queryURL = null;

    public static final String ID = "MAASTO_ADDRESS_CHANNEL";

    public void setProperty(String propertyName, String propertyValue) {
        if ("query.url".equals(propertyName)) {
            queryURL = propertyValue;
            log.debug("QueryURL set to " + queryURL);
        } else {
            log.warn("Unknown property for " + ID + " search channel: " + propertyName);
        }
    }

    public String getId() {
        return ID;
    }
	@Override
	protected String getQueryUrl(String filter) {
		return queryURL +  filter + "&maxFeatures=" +  (SearchUtil.maxCount+1);  // added 1 to maxCount because need to know if there are more then maxCount
	}
	
	@Override
	protected ChannelSearchResult filterResultsAfterQuery(
			ChannelSearchResult csr) {
		return csr;
	}

	@Override
	protected String getType() {
		return "Tie";
	}
}
