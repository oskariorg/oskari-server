package fi.nls.oskari.control.statistics.plugins.sotka;

import java.util.Map;

import fi.nls.oskari.control.statistics.plugins.sotka.parser.SotkaIndicator;
import fi.nls.oskari.control.statistics.plugins.sotka.parser.SotkaSpecificIndicatorParser;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.IndicatorMetadata;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.SotkaRequest;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

/**
 * This fetches the indicator selector metadata transparently from Sotka.
 * We don't want to make a separate call to the plugin interface for this, because some
 * APIs / plugins might give all the information in the same response, or divide and key the responses differently.
 */
public class SotkaIndicatorSelectorsFetcher {
    private final static Logger LOG = LogFactory.getLogger(SotkaIndicatorSelectorsFetcher.class);
    private SotkaSpecificIndicatorParser specificIndicatorParser;

    public void init() {
        this.specificIndicatorParser = new SotkaSpecificIndicatorParser();
    }

    /**
     * This returns the indicator selectors for one indicator.
     * @param selectors
     * @param indicator
     * @return
     */
    public SotkaIndicator get(String indicatorId, Map<String, String> layerMappings) {
        try {
            SotkaRequest specificIndicatorRequest = SotkaRequest.getInstance(IndicatorMetadata.NAME);
            specificIndicatorRequest.setIndicator(indicatorId);
            String specificIndicatorJsonResponse = specificIndicatorRequest.getData();
            return specificIndicatorParser.parse(specificIndicatorJsonResponse, layerMappings);
        } catch (Throwable e) {
            // The SotkaNET sometimes responds with HTTP 500, for example. For these cases, we should just
            // remove the indicators in question.
            LOG.error("There was an error fetching SotkaNET indicator metadata for indicator: "
                    + indicatorId + ", removing from Oskari.");
            return null;
        }
    }
}
