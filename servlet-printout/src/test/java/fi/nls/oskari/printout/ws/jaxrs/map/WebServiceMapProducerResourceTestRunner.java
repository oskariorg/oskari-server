package fi.nls.oskari.printout.ws.jaxrs.map;

import fi.nls.oskari.printout.output.map.MapProducer;
import fi.nls.oskari.printout.ws.ClientInfoSetup;
import org.geowebcache.GeoWebCacheException;
import org.geowebcache.filter.request.RequestFilterException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import javax.ws.rs.core.StreamingOutput;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Map;

public class WebServiceMapProducerResourceTestRunner {

    private ClientInfoSetup clientInfo;
    private WebServiceMapProducerResource resource;

    public WebServiceMapProducerResourceTestRunner() {

    }

    public WebServiceMapProducerResource getResource() {
        return resource;
    }

    void run(final String testname,
            WebServiceMapProducerResourceTestFileType input,
            WebServiceMapProducerResourceTestFileType output)
            throws IOException,
            GeoWebCacheException, FactoryException,
            com.vividsolutions.jts.io.ParseException, ParseException,
            XMLStreamException, FactoryConfigurationError,
            RequestFilterException, TransformException, InterruptedException,
            org.json.simple.parser.ParseException, URISyntaxException {

        StreamingOutput result = null;

        Map<String, String> xClientInfo = clientInfo.getXClientInfo(resource.getProps());

        InputStream inp = MapProducer.class.getResourceAsStream(input
                .getFilename(testname));
        try {
            switch (output) {
            case PNG:
                switch (input) {
                case JSON:
                    result = resource.getMapPNG(inp, null);
                    break;
                case GEOJSON:
                    result = resource.getGeoJsonMapPNG(inp, xClientInfo);
                    break;
                default:
                    throw new IOException("Invalid args for PNG test");
                }
                break;
            case PDF:
                switch (input) {
                case JSON:
                    result = resource.getMapPDF(inp, null);
                    break;
                case GEOJSON:
                    result = resource.getGeoJsonMapPDF(inp, xClientInfo);
                    break;
                default:
                    throw new IOException("Invalid args for PDF test");
                }
                break;
            default:
                throw new IOException("Invalid args for test");
            }

            FileOutputStream outs = new FileOutputStream(
                    output.getFilename(testname));
            try {
                result.write(outs);
            } finally {
                outs.close();
            }

        } finally {
            inp.close();
        }
    }

    public void setResource(WebServiceMapProducerResource rc) {
        this.resource = rc;
    }

    public void setClientInfo(ClientInfoSetup clientInfo) {
        this.clientInfo = clientInfo;
    }

}
