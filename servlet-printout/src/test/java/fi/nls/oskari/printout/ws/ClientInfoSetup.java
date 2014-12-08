package fi.nls.oskari.printout.ws;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import fi.nls.oskari.printout.config.ConfigValue;

public class ClientInfoSetup {

    private String cookie;

    public ClientInfoSetup(String cookie) {
        this.cookie = cookie;
    }

    public Map<String, String> getXClientInfo(final Properties props) {

        final Map<String, String> xClientInfo = new HashMap<String, String>();

        final String userAgent = ConfigValue.MAPPRODUCER_USERAGENT
                .getConfigProperty(props,
                        "Mozilla/5.0 (Windows NT 6.1) oskari.org/printout");
        final String referer = ConfigValue.MAPPRODUCER_REFERER
                .getConfigProperty(props);

        xClientInfo.put("User-Agent", userAgent);
        if (referer != null) {
            xClientInfo.put("Referer", referer);
        }
        xClientInfo.put("Cookie", cookie);

        return xClientInfo;
    }
}
