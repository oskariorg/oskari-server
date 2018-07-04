package org.oskari.print;

import fi.nls.oskari.domain.map.OskariLayer;
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
        return PNG.getBufferedImage(request, wmtsCapsCache);
    }

    public void getPDF(PrintRequest request, PDDocument doc)
            throws IOException, ServiceException {
        PDF.getPDF(request, doc, wmtsCapsCache);
    }

}
