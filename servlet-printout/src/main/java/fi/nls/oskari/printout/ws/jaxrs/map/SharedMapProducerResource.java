package fi.nls.oskari.printout.ws.jaxrs.map;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Properties;

import org.geowebcache.GeoWebCacheException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import fi.nls.oskari.printout.config.ConfigValue;

public class SharedMapProducerResource {

    /* synchronized for create on call only */
    static Object getmapResourceLock = new Object();

    static WebServiceMapProducerResource shared;

    public static WebServiceMapProducerResource acquire()
            throws NoSuchAuthorityCodeException, IOException,
            GeoWebCacheException, FactoryException,
            com.vividsolutions.jts.io.ParseException {
        synchronized (getmapResourceLock) {
            if (shared == null) {

                String conf = System
                        .getProperty(ConfigValue.CONFIG_SYSTEM_PROPERTY);

                Properties props = new Properties();
                Reader r = conf != null ? new FileReader(conf)
                        : new InputStreamReader(
                                SharedMapProducerResource.class
                                        .getResourceAsStream(ConfigValue.DEFAULT_PROPERTIES));
                try {
                    props.load(r);
                } finally {
                    r.close();
                }

                shared = new WebServiceMapProducerResource(props);

                URL layerJSONurl = new URL(
                        ConfigValue.LAYERSURL.getConfigProperty(props));
                //
                shared.setLayerJSONurl(layerJSONurl);

                try {
                    shared.loadLayerJson();
                } catch (com.vividsolutions.jts.io.ParseException geomEx) {
                    if (shared.getLayerJson() != null) {
                        /* we'll use the old one */
                    } else {
                        throw geomEx;
                    }
                } catch (IOException ioe) {
                    if (shared.getLayerJson() != null) {
                        /* we'll use the old one */
                    } else {
                        throw ioe;
                    }
                }

            }
        }

        return shared;
    }

}
