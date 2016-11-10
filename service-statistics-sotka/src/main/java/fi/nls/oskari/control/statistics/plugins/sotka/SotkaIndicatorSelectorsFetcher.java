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
    private SotkaConfig config;

    public void init(SotkaConfig config) {
        this.config = config;
        this.specificIndicatorParser = new SotkaSpecificIndicatorParser(config);
    }

    /**
     * This returns the indicator selectors for one indicator.
     * @param indicatorId
     * @param layerMappings
     * @return
     */
    public SotkaIndicator get(String indicatorId, Map<String, Long> layerMappings) {
        try {
            SotkaRequest specificIndicatorRequest = SotkaRequest.getInstance(IndicatorMetadata.NAME);
            specificIndicatorRequest.setBaseURL(config.getUrl());
            specificIndicatorRequest.setIndicator(indicatorId);
            String specificIndicatorJsonResponse = specificIndicatorRequest.getData();
            return specificIndicatorParser.parse(specificIndicatorJsonResponse, layerMappings);
        } catch (Exception e) {
            // The SotkaNET sometimes responds with HTTP 500, for example. For these cases, we should just
            // remove the indicators in question.
            LOG.error("There was an error fetching SotkaNET indicator metadata for indicator: "
                    + indicatorId + ", removing from Oskari:", e.getMessage());
            return null;
        }
    }
}
