package org.oskari.print;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintRequest;

public class PrintService {

    private static final int MAX_PX = 2048;

    public static String validate(PrintRequest request) {
        if (PrintFormat.getByContentType(request.getFormat()) == null) {
            return ("Unknown format");
        }
        if (request.getWidth() <= 0) {
            return "'width' must be positive integer";
        }
        if (request.getHeight() <= 0) {
            return "'height' must be positive integer";
        }
        if (request.getWidth() > MAX_PX) {
            return "'width' must be less than " + MAX_PX;
        }
        if (request.getHeight() > MAX_PX) {
            return "'height' must be less than " + MAX_PX;
        }
        if (request.getLayers() == null || request.getLayers().size() == 0) {
            return "'layers' not specified!";
        }
        return null;
    }

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
