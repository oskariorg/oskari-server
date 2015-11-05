package fi.nls.oskari.control.statistics.plugins.sotka;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.statistics.plugins.APIException;
import fi.nls.oskari.control.statistics.plugins.StatisticalDatasourcePlugin;
import fi.nls.oskari.control.statistics.plugins.StatisticalIndicator;
import fi.nls.oskari.control.statistics.plugins.sotka.parser.SotkaIndicator;
import fi.nls.oskari.control.statistics.plugins.sotka.parser.SotkaIndicatorsParser;
import fi.nls.oskari.control.statistics.plugins.sotka.parser.SotkaSpecificIndicatorParser;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.IndicatorMetadata;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.Indicators;
import fi.nls.oskari.control.statistics.plugins.sotka.requests.SotkaRequest;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class SotkaStatisticalDatasourcePlugin implements StatisticalDatasourcePlugin {
    private final static Logger LOG = LogFactory.getLogger(SotkaStatisticalDatasourcePlugin.class);
    
    private SotkaIndicatorsParser indicatorsParser = null;
    private SotkaSpecificIndicatorParser specificIndicatorParser = null;
    // Used in testing to not to fetch all the indicators completely.
    public static boolean testMode = false;

    /**
     * For scheduling the cache refresh for the plugin list.
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SotkaStatisticalDatasourcePlugin() {
        indicatorsParser = new SotkaIndicatorsParser();
        specificIndicatorParser = new SotkaSpecificIndicatorParser();
    }
    @Override
    public List<? extends StatisticalIndicator> getIndicators() {
        try {
            // First getting general information of all the indicator layers.
            // Note that some mandatory information about the layers is not given here,
            // for example the year range, but must be requested separately for each indicator.
            SotkaRequest request = SotkaRequest.getInstance(Indicators.NAME);
            String jsonResponse = request.getData();

            // We will now need to add the year range information to the preliminary information using separate requests.
            List<SotkaIndicator> preliminaryIndicatorInformation = indicatorsParser.parse(jsonResponse);
            List<SotkaIndicator> updatedIndicators = new ArrayList<>();
            for (SotkaIndicator indicator : preliminaryIndicatorInformation) {
                try {
                    SotkaRequest specificIndicatorRequest = SotkaRequest.getInstance(IndicatorMetadata.NAME);
                    specificIndicatorRequest.setIndicator(indicator.getId());
                    if (SotkaStatisticalDatasourcePlugin.testMode && Long.valueOf(indicator.getId()) > 200) {
                        // Skipping to speed up tests.
                        updatedIndicators.add(indicator);
                        continue;
                    }
                    String specificIndicatorJsonResponse = specificIndicatorRequest.getData();
                    SotkaIndicator infoToAdd = specificIndicatorParser.parse(specificIndicatorJsonResponse);
                    if (infoToAdd != null) {
                        indicator.merge(infoToAdd);
                        updatedIndicators.add(indicator);
                    }
                } catch (Throwable e) {
                    // The SotkaNET sometimes responds with HTTP 500, for example. For these cases, we should just
                    // remove the indicators in question.
                    LOG.error("There was an error fetching SotkaNET indicator metadata for indicator: "
                            + indicator.getId() + ": " + String.valueOf(indicator.getLocalizedName())
                            + ", removing from Oskari.");
                }
            }
            return updatedIndicators;
        } catch (APIException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new APIException("Something went wrong calling SotkaNET Indicators interface.", e);
        }
    }

    @Override
    public void init() {
        // Refreshing the cache.
        this.getIndicators();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                SotkaStatisticalDatasourcePlugin.this.getIndicators();
            }
        }, 8, 8, TimeUnit.HOURS);
    }

}
