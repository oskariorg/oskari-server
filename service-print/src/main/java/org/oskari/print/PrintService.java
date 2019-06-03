package org.oskari.print;

import fi.nls.oskari.service.ServiceException;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.oskari.print.request.PrintRequest;
import org.oskari.print.wmts.WMTSCapabilitiesCache;
import org.oskari.service.wfs.client.OskariFeatureClient;
import org.oskari.service.wfs.client.OskariWFSClient;

public class PrintService {

    private final WMTSCapabilitiesCache wmtsCapsCache;
    private final OskariFeatureClient featureClient;

    public PrintService() {
        this(getDefaultWMTSCapabilitiesCache(), getDefaultFeatureClient());
    }
    
    public PrintService(OskariFeatureClient featureClient) {
        this(getDefaultWMTSCapabilitiesCache(), featureClient);
    }
    
    public PrintService(WMTSCapabilitiesCache wmtsCapsCache) {
        this(wmtsCapsCache, getDefaultFeatureClient());
    }

    public PrintService(WMTSCapabilitiesCache wmtsCapsCache, OskariFeatureClient featureClient) {
        this.wmtsCapsCache = wmtsCapsCache;
        this.featureClient = featureClient;
    }
    
    private static WMTSCapabilitiesCache getDefaultWMTSCapabilitiesCache() {
        return new WMTSCapabilitiesCache();
    }
    
    private static OskariFeatureClient getDefaultFeatureClient() {
        // Use non caching OskariWFSClient by default
        return new OskariFeatureClient(new OskariWFSClient());
    }

    public BufferedImage getPNG(PrintRequest request) throws ServiceException {
        return PNG.getBufferedImage(request, wmtsCapsCache);
    }

    public void getPDF(PrintRequest request, PDDocument doc)
            throws IOException, ServiceException {
        PDF.getPDF(request, wmtsCapsCache, featureClient, doc);
    }

}
