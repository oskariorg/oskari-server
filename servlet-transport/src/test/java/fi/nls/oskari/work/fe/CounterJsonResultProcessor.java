package fi.nls.oskari.work.fe;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.work.ResultProcessor;

import java.util.HashMap;

abstract class CounterJsonResultProcessor implements ResultProcessor {
    protected static final Logger log = LogFactory
            .getLogger(CounterJsonResultProcessor.class);

    protected CounterJsonResultProcessor() {

    }

    protected long resultsCounter = 0;
    protected final HashMap<String, Integer> results = new HashMap<String, Integer>();

    public HashMap<String, Integer> getResults() {
        return results;
    }

    protected void logResults(String channel) {
        resultsCounter++;

        // log.debug(clientId, channel, data);
        if (results.get(channel) == null) {
            results.put(channel, 1);
        } else {
            results.put(channel, results.get(channel) + 1);
        }

    }
}
