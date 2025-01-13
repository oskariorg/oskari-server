package org.oskari.print;

import fi.nls.oskari.service.ServiceException;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.oskari.print.request.PrintRequest;
import org.oskari.service.wfs.client.OskariFeatureClient;
import org.oskari.service.wfs.client.OskariWFSClient;

public class PrintService {

    private final OskariFeatureClient featureClient;

    public PrintService() {
        this(getDefaultFeatureClient());
    }

    public PrintService(OskariFeatureClient featureClient) {
        this.featureClient = featureClient;
    }

    private static OskariFeatureClient getDefaultFeatureClient() {
        // Use non caching OskariWFSClient by default
        return new OskariFeatureClient(new OskariWFSClient());
    }

    public BufferedImage getPNG(PrintRequest request) throws ServiceException {
        return PNG.getBufferedImage(request, featureClient);
    }

    public void getPDF(PrintRequest request, PDDocument doc)
            throws IOException, ServiceException {
        PDF.getPDF(request, featureClient, doc);
    }

}
