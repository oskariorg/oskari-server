package org.oskari.print;

import fi.nls.oskari.service.ServiceException;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.oskari.print.loader.PrintLoader;
import org.oskari.print.request.PrintRequest;
import org.oskari.service.wfs.client.OskariFeatureClient;
import org.oskari.service.wfs.client.OskariWFSClient;

public class PrintService {
    private final OskariFeatureClient featureClient;
    private final PrintLoader loader;

    public PrintService() {
        this(getDefaultFeatureClient());
    }

    public PrintService(OskariFeatureClient featureClient) {
        this.featureClient = featureClient;
        loader = new PrintLoader();
    }

    private static OskariFeatureClient getDefaultFeatureClient() {
        // Use non caching OskariWFSClient by default
        return new OskariFeatureClient(new OskariWFSClient());
    }
    public PrintLoader getLoader() {
        return this.loader;
    }
    public OskariFeatureClient getFeatureClient() {
        return this.featureClient;
    }

    public BufferedImage getPNG(PrintRequest request) throws ServiceException {
        return PNG.getBufferedImage(this, request);
    }

    public void getPDF(PrintRequest request, PDDocument doc)
            throws IOException, ServiceException {
        PDF.getPDF(this, request, doc);
    }

}
