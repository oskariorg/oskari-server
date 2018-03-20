package org.oskari.print;

import fi.nls.oskari.service.ServiceException;

import fi.nls.oskari.service.capabilities.CapabilitiesCacheService;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintRequest;
import org.oskari.print.wmts.WMTSCapabilitiesCache;

public class PrintService {

    private WMTSCapabilitiesCache wmtsCapsCache;

    public PrintService() {
        this(new WMTSCapabilitiesCache());
    }

    public PrintService(CapabilitiesCacheService capCacheService) {
        this(new WMTSCapabilitiesCache(capCacheService));
    }

    public PrintService(WMTSCapabilitiesCache wmtsCapsCache) {
        this.wmtsCapsCache = wmtsCapsCache;
    }

    public BufferedImage getPNG(PrintRequest request) throws ServiceException {
        request.setLayers(filterLayersWithZeroOpacity(request.getLayers()));
        return PNG.getBufferedImage(request, wmtsCapsCache);
    }

    public void getPDF(PrintRequest request, PDDocument doc)
            throws IOException, ServiceException {
        request.setLayers(filterLayersWithZeroOpacity(request.getLayers()));
        PDF.getPDF(request, doc, wmtsCapsCache);
    }

    private static List<PrintLayer> filterLayersWithZeroOpacity(List<PrintLayer> layers) {
        List<PrintLayer> filtered = new ArrayList<>();
        for (PrintLayer layer : layers) {
            if (layer.getOpacity() > 0) {
                filtered.add(layer);
            }
        }
        return filtered;
    }

}
