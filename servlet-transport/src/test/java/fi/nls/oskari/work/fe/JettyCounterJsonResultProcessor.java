package fi.nls.oskari.work.fe;

import org.eclipse.jetty.util.ajax.JSON;

public class JettyCounterJsonResultProcessor extends CounterJsonResultProcessor {

    protected JSON json = new JSON();

    @Override
    public void addResults(String clientId, String channel, Object data) {
        // display a snapshot
        if (resultsCounter < 10) {

            String result = json.toJSON(data);

            log.debug("JettyJSON", result);
        }

        logResults(channel);
    }
}
