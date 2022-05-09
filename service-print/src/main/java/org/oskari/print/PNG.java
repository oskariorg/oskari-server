package org.oskari.print;

import fi.nls.oskari.service.ServiceException;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.oskari.print.loader.AsyncFeatureLoader;
import org.oskari.print.loader.AsyncImageLoader;
import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintRequest;
import org.oskari.service.wfs.client.OskariFeatureClient;

public class PNG {

    private static final Logger LOG = LogFactory.getLogger(PNG.class);

    /**
     * This method should be called via PrintService
     */
    protected static BufferedImage getBufferedImage(PrintRequest request, OskariFeatureClient featureClient)
            throws ServiceException {
        final int width = request.getWidth();
        final int height = request.getHeight();
        final double [] bbox = request.getBoundingBox();

        final List<PrintLayer> layers = request.getLayers();

        Map<Integer, Future<BufferedImage>> images = AsyncImageLoader.initLayers(request);
        Map<Integer, Future<SimpleFeatureCollection>> featureCollections = AsyncFeatureLoader.initLayers(request, featureClient);
        BufferedImage canvas = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = canvas.createGraphics();
        try {
            for (int i = 0; i < layers.size(); i++) {
                PrintLayer layer = layers.get(i);
                int zIndex = layer.getZIndex();
                Future<BufferedImage> image = images.get(zIndex);
                BufferedImage bi = null;
                float alpha = 1f;
                if (image == null) {
                    // try vectorlayer, opacity handled in vector styles
                    Future<SimpleFeatureCollection> futureFc = featureCollections.get(zIndex);
                    bi = PDF.getVectorLayerImage(layer, futureFc, bbox, width, height);
                } else {
                    bi = image.get();
                    alpha = getAlpha(layer.getOpacity());
                }

                if (bi == null) {
                    continue;
                }
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                g2d.drawImage(bi, 0, 0, null);
            }
            BufferedImage bi = PDF.getMarkersImage(request.getMarkers(), bbox, width, height);
            if (bi != null) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                g2d.drawImage(bi, 0, 0, null);
            }
        } catch (Exception e) {
            LOG.warn(e);
            return null;
        } finally {
            g2d.dispose();
        }

        BufferedImage scaled = scale(canvas,
                request.getTargetWidth(),
                request.getTargetHeight(),
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        return scaled;
    }

    private static float getAlpha(int opacity) {
        return opacity == 100 ? 1.0f : 0.01f * opacity;
    }

    public static BufferedImage scale(BufferedImage bi, int targetWidth, int targetHeight, Object interpolation) {
        if (targetWidth <= 0
                || targetWidth == bi.getWidth()
                || targetHeight <= 0
                || targetHeight == bi.getHeight()) {
            // Return the original image
            return bi;
        }

        if (interpolation == null) {
            interpolation = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
        }

        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, bi.getType());
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);
        g2d.drawImage(bi, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return scaled;
    }

}
