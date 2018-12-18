package fi.nls.oskari.work.fe;

import fi.nls.oskari.utils.GeometryJSONOutputModule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JacksonCounterJsonResultProcessor extends CounterJsonResultProcessor {

    protected ObjectMapper json = new ObjectMapper();
    protected ObjectWriter writer;

    public JacksonCounterJsonResultProcessor() {
        writer = json.writer(new DefaultPrettyPrinter());
        json.registerModule(new GeometryJSONOutputModule());
    }

    @Override
    public void addResults(String clientId, String channel, Object data) {
        // display a snapshot
        if (resultsCounter < 10) {

            ByteArrayOutputStream outs = new ByteArrayOutputStream();
            try {
                writer.writeValue(outs, data);
            } catch (JsonGenerationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JsonMappingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            log.debug(new String(outs.toByteArray()));
        }
        logResults(channel);
    }

}
