package fi.nls.oskari.printout.input.maplink;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;

import fi.nls.oskari.printout.input.layers.LayerDefinition;
import fi.nls.oskari.printout.output.map.MapProducerResource;
import fi.nls.oskari.printout.ws.jaxrs.map.WebServiceMapProducerResource;
import fi.nls.oskari.printout.ws.jaxrs.resource.MapResource;

public class MapLinkJSONProcessorTest {

    final List<LayerDefinition> testLayerDefs = new ArrayList<LayerDefinition>();
    private String gridSubsetName = "EPSG_3067_MML";
    private String layerTemplate = "EPSG_3067_MML_LAYER_TEMPLATE";
    WebServiceMapProducerResource shared;
    Properties props;

    @Before
    public void setUp() throws Exception {

        Properties properties = new Properties();

        properties.setProperty("log4j.logger.org.geowebcache", "WARN");

        properties.setProperty("log4j.logger.fi.paikkatietoikkuna", "DEBUG");
        properties.setProperty("lo44j.logger.org.apache.http.wire", "WARN");

        properties.setProperty("log4j.rootLogger", "WARN, A1");
        properties.setProperty("log4j.appender.A1",
                "org.apache.log4j.ConsoleAppender");
        properties.setProperty("log4j.appender.A1.layout",
                "org.apache.log4j.PatternLayout");
        properties.setProperty("log4j.appender.A1.layout.ConversionPattern",
                "%-4r [%t] %-5p %c %x - %m%n");
        /* "%c %x - %m%n"); */

        PropertyConfigurator.configure(properties);

        /** config */
        String conf = System.getProperty("fi.paikkatietoikkuna.imaging.config");

        props = new Properties();
        Reader r = conf != null ? new FileReader(conf) : new InputStreamReader(
                MapResource.class.getResourceAsStream("default.properties"));
        try {
            props.load(r);
        } finally {
            r.close();
        }

        shared = new WebServiceMapProducerResource(props);
        shared.setLayerJSONurl(MapProducerResource.class
                .getResource("blank-layers.json"));

    }

    @Before
    public void setupLayers() {
        LayerDefinition layerDefinition = new LayerDefinition();
        layerDefinition.setVisibility(true);
        layerDefinition.setWmsname("taustakartta_80k");
        layerDefinition
                .setWmsurl("http://karttatiili.fi/dataset/taustakarttasarja/service/wms");
        layerDefinition.setMinScale(56702d);
        layerDefinition.setMaxScale(40000d);
        layerDefinition.setOpacity(100);
        testLayerDefs.add(layerDefinition);
    }

}
