package fi.nls.oskari.myplaces.service.wfst;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.xml.parsers.ParserConfigurationException;

import org.oskari.wfst.TransactionResponseParser_110;
import org.oskari.wfst.TransactionResponse_110;
import org.xml.sax.SAXException;

import fi.nls.oskari.service.ServiceException;
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

    protected TransactionResponse_110 readTransactionResp(HttpURLConnection conn)
            throws ServiceException {
        try {
            byte[] resp = IOHelper.readBytes(conn);
            return TransactionResponseParser_110.parse(resp);
        } catch (IOException e) {
            throw new ServiceException("IOException occured", e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new ServiceException("Failed to parse TransactionResponse", e);
        }
    }

}
