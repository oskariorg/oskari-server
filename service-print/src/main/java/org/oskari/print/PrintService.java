package org.oskari.print;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintRequest;

public class PrintService {

    public static BufferedImage getPNG(PrintRequest request) {
        request.setLayers(filterLayersWithZeroOpacity(request.getLayers()));
        return PNG.getBufferedImage(request);
    }

    public static void getPDF(PrintRequest request, PDDocument doc) 
            throws IllegalArgumentException, IOException {
        request.setLayers(filterLayersWithZeroOpacity(request.getLayers()));
        PDF.getPDF(request, doc);
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
