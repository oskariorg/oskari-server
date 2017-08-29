package org.oskari.print;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.oskari.util.PDFBoxUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PrintService {

    private static final int MAX_PX = 2048;

    private static final Logger LOG = LogFactory.getLogger(PrintService.class);
    private static final Setter CONFIG = HystrixCommand.Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey("test"))
            .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                    .withExecutionTimeoutEnabled(false))
                    .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                            .withCoreSize(25)
                            .withMaximumSize(50)
                            .withMaxQueueSize(1000)
                            .withQueueSizeRejectionThreshold(1000));

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

        removeFullyTransparentLayers(request);

        return null;
    }

    public static byte[] getPNG(PrintRequest request, String formatName) {
        try {
            BufferedImage bi = getPNG(request, CONFIG);
            if (bi != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bi, formatName, baos);
                return baos.toByteArray();
            }
        } catch (IOException e) {
            LOG.error(e);
        }
        return null;
    }

    public static BufferedImage getPNG(PrintRequest request, Setter config) {
        if (config == null) {
            config = CONFIG;
        }
        return PNG.getBufferedImage(request, config);
    }

    public static byte[] getPDF(PrintRequest request) {
        PDDocument doc = new PDDocument();
        try {
            boolean success = getPDF(request, doc, CONFIG);
            if (success) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                doc.save(baos);
                return baos.toByteArray();
            }
        } catch (IOException e) {
            // ByteArrayOutputStream should not throw IOExceptions
            LOG.error(e);
        } finally {
            PDFBoxUtil.closeSilently(doc);
        }
        return null;
    }

    public static boolean getPDF(PrintRequest request, PDDocument doc, Setter config) {
        if (config == null) {
            config = CONFIG;
        }
        return PDF.getPDF(request, doc, config);
    }

    private static void removeFullyTransparentLayers(PrintRequest request) {
        List<PrintLayer> filtered = new ArrayList<>();
        for (PrintLayer layer : request.getLayers()) {
            if (layer.getOpacity() > 0) {
                filtered.add(layer);
            }
        }
        request.setLayers(filtered);
    }

}
