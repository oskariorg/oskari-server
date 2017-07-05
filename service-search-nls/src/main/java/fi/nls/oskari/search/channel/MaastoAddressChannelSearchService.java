package fi.nls.oskari.search.channel;

import fi.mml.portti.service.search.ChannelSearchResult;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;

@Oskari(MaastoAddressChannelSearchService.ID)
public class MaastoAddressChannelSearchService extends BaseWfsAddressChannelSearchService {

    private Logger log = LogFactory.getLogger(this.getClass());

    public static final String ID = "MAASTO_ADDRESS_CHANNEL";
    private static final String PROPERTY_SERVICE_URL = "search.channel.MAASTO_ADDRESS_CHANNEL.query.url";

    private String queryURL = null;

    @Override
    public void init() {
        super.init();
        queryURL = PropertyUtil.getOptional(PROPERTY_SERVICE_URL);
        log.debug("ServiceURL set to " + queryURL);
    }

	@Override
	protected String getQueryUrl(String filter, int maxResults) {
        if(queryURL == null) {
            return null;
        }
		return queryURL +  filter + "&maxFeatures=" +  (maxResults+1);  // added 1 to maxCount because need to know if there are more then maxCount
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
