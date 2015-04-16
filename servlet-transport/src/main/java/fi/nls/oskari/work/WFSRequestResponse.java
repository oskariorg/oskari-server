package fi.nls.oskari.work;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by SMAKINEN on 13.2.2015.
 */
public class WFSRequestResponse  implements RequestResponse {
        BufferedReader response ;

    public BufferedReader getResponse() {
        return response;
    }

    public void setResponse(BufferedReader response) {
        this.response = response;
    }

    public void flush() throws IOException {
        if( response != null ) {
            response.close();
            response = null;
        }
    }
}
