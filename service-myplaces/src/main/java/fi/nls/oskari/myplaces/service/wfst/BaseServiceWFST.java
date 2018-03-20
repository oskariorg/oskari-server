package fi.nls.oskari.myplaces.service.wfst;

import java.io.IOException;
import java.net.HttpURLConnection;

import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

public abstract class BaseServiceWFST {

    protected static final String APPLICATION_XML = "application/xml";

    protected HttpURLConnection getConnection() throws IOException {
        final String url = PropertyUtil.get("myplaces.ows.url");
        final String user = PropertyUtil.getOptional("myplaces.user");
        final String pass = PropertyUtil.getOptional("myplaces.password");
        return IOHelper.getConnection(url, user, pass);
    }

}
