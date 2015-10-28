package fi.nls.oskari.work.fe;

import fi.nls.oskari.utils.GeometryJSONOutputModule;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.impl.DefaultPrettyPrinter;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This uses the Jackson 1.x version since it's used by the current version of CometD.
 * Don't upgrade if not upgrading CometD.
 */
public class JacksonCounterJsonResultProcessor extends CounterJsonResultProcessor {

    protected ObjectMapper json = new ObjectMapper();
    protected ObjectWriter writer;

    public JacksonCounterJsonResultProcessor() {
        SerializationConfig x = json.getSerializationConfig()
                .withSerializationInclusion(Inclusion.NON_NULL);
        json.setSerializationConfig(x);
        writer = json.writer(new DefaultPrettyPrinter());

        GeometryJSONOutputModule simpleModule = new GeometryJSONOutputModule();

        json.registerModule(simpleModule);

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
