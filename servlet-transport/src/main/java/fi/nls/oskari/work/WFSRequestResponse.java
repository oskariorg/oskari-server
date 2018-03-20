package fi.nls.oskari.work;

import java.io.IOException;
import java.io.Reader;

/**
 * Created by SMAKINEN on 13.2.2015.
 */
public class WFSRequestResponse  implements RequestResponse {

    private Reader response;

    public Reader getResponse() {
        return response;
    }

    public void setResponse(Reader response) {
        this.response = response;
    }

    public void flush() throws IOException {
        if( response != null ) {
            response.close();
            response = null;
        }
    }
}
