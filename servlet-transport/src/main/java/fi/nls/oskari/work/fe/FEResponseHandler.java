package fi.nls.oskari.work.fe;

import fi.nls.oskari.fe.engine.FeatureEngine;
import fi.nls.oskari.fe.input.InputProcessor;
import fi.nls.oskari.fe.output.OutputProcessor;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wfs.WFSExceptionHelper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.entity.ContentType;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class FEResponseHandler implements ResponseHandler<Boolean> {
    protected static final Logger log = LogFactory
            .getLogger(FEResponseHandler.class);

    final InputProcessor inputProcessor;
    final OutputProcessor outputProcessor;
    final FeatureEngine engine;

    private boolean debugResponse = PropertyUtil.getOptional("transport.response.debug", false);

    public FEResponseHandler(FeatureEngine engine,
            InputProcessor inputProcessor, OutputProcessor outputProcessor) {

        this.engine = engine;
        this.inputProcessor = inputProcessor;
        this.outputProcessor = outputProcessor;

    }

    @Override
    public Boolean handleResponse(HttpResponse response)
            throws ClientProtocolException, IOException {

        Boolean succee = false;

        StatusLine statusLine = response.getStatusLine();

        log.debug("[fe] http status : " + statusLine);

        HttpEntity entity = response.getEntity();
        if (statusLine.getStatusCode() >= 300) {
            log.debug("[fe] throwing http exception for : " + statusLine);
            throw new HttpResponseException(statusLine.getStatusCode(),
                    statusLine.getReasonPhrase());
        }
        if (entity == null) {
            log.debug("[fe] throwing client protocol exception no content");
            throw new ClientProtocolException("Response contains no content");
        }

        ContentType contentType = ContentType.getOrDefault(entity);

        if (contentType.getMimeType().toUpperCase().indexOf("HTML") > -1) {
            throw new ServiceRuntimeException("Response type is  " + contentType.getMimeType() + ",  must be xml",
                    WFSExceptionHelper.ERROR_GETFEATURE_POSTREQUEST_FAILED);
        }

        Charset charset = contentType.getCharset();
        log.debug("[fe] response contentType " + contentType + ", charset: "
                + charset);
        InputStream is = entity.getContent();
        if(debugResponse) {
            is = IOHelper.debugResponse(is);
        }
        BufferedInputStream inp = new BufferedInputStream(is);

        try {

            inputProcessor.setInput(inp);

            engine.setInputProcessor(inputProcessor);
            engine.setOutputProcessor(outputProcessor);

            engine.process();

            succee = true;

        } catch (XMLStreamException e) {
            log.debug("[fe] response XML exception ", e);
            throw new ClientProtocolException("Response XMLStreamException "
                    + e);
        } finally {
            if (inp != null) {
                inp.close();
            }
        }

        return succee;
    }

}