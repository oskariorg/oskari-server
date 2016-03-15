package fi.nls.oskari.printout.output.layer;

import com.mortennobel.imagescaling.ResampleFilter;
import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;
import com.vividsolutions.jts.geom.Envelope;
import fi.nls.oskari.printout.input.layers.LayerDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DirectLayer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.opengis.feature.simple.SimpleFeature;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;

/* 
 * Class that draws given Images to GeoTools MapContent
 *  
 */
public abstract class DirectTileLayer extends DirectLayer {
    protected static Log log = LogFactory.getLog(DirectTileLayer.class);

    protected AffineTransform transform;
    protected DefaultFeatureCollection fc;

    protected int layerOpacity = 100;

    protected LayerDefinition layerDefinition;

    public DirectTileLayer(LayerDefinition ld, DefaultFeatureCollection fc,
            AffineTransform transform) {
        this.layerDefinition = ld;
        this.fc = fc;
        this.transform = transform;
    }

    private BufferedImage doScaleWithFilters(BufferedImage image, int width,
            int height) throws IOException {

        final ResampleOp resampleOp = new ResampleOp(width, height);
        resampleOp.setNumberOfThreads(2);
        ResampleFilter filter = ResampleFilters.getLanczos3Filter();
        resampleOp.setFilter(filter);

        BufferedImage scaledImage = resampleOp.filter(image, null);

        return scaledImage;

    }

    public abstract void draw(Graphics2D g2d, MapContent mapContent,
            MapViewport mapViewport);

    public void drawImage(Graphics2D g2d, BufferedImage imageBuf, int x, int y,
            int w, int h, boolean doScale) throws IOException {
        if (doScale) {
            g2d.drawImage(doScaleWithFilters(imageBuf, w, h), x, y, null);
        } else {
            g2d.drawImage(imageBuf, x, y, null);
        }

    }

    public void drawImageFeature(Graphics2D g2d, MapContent mapContent,
            SimpleFeature f, BufferedImage imageBuf) {

        Integer width = (Integer) f.getProperty("width").getValue();
        Integer height = (Integer) f.getProperty("height").getValue();
        Envelope e = (Envelope) f.getProperty("env").getValue();

        double[] srcPts = new double[] { e.getMinX(), e.getMaxY(), e.getMaxX(),
                e.getMinY() };
        double[] dstPts = new double[] { 0.0, 0.0, 0.0, 0.0 };

        transform.transform(srcPts, 0, dstPts, 0, 2);
        try {

            Envelope envAdj = new Envelope(dstPts[0], dstPts[2], dstPts[3],
                    dstPts[1]);
            int tw = Double.valueOf(envAdj.getWidth()).intValue();
            int th = Double.valueOf(envAdj.getHeight()).intValue();

            boolean isScaleRequired = (width != Integer.MIN_VALUE && height != Integer.MIN_VALUE)
                    && !(tw == width && th == height);

            if (imageBuf.getColorModel() != null
                    && imageBuf.getColorModel().getNumComponents() != 4
                    || imageBuf.getColorModel().getPixelSize() != 32) {

                int w = imageBuf.getWidth(null);
                int h = imageBuf.getHeight(null);
                BufferedImage bi = new BufferedImage(w, h,
                        BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D g = (Graphics2D) bi.getGraphics();
                g.drawImage(imageBuf, 0, 0, null);

                drawImage(g2d, bi, (int) dstPts[0], (int) dstPts[1], tw, th,
                        isScaleRequired);

                g.dispose();
                bi.flush();

            } else {
                drawImage(g2d, imageBuf, (int) dstPts[0], (int) dstPts[1], tw,
                        th, isScaleRequired);

            }
        } catch (com.sun.media.jai.codecimpl.util.ImagingException iox) {
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect((int) dstPts[0], (int) dstPts[1], 256, 256, 24,
                    24);
        } catch (MalformedURLException e1) {
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect((int) dstPts[0], (int) dstPts[1], 256, 256, 24,
                    24);
        } catch (IOException e1) {
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect((int) dstPts[0], (int) dstPts[1], 256, 256, 24,
                    24);
        }
    }

    public ReferencedEnvelope getBounds() {
        return fc.getBounds();
    }

    public DefaultFeatureCollection getFc() {
        return fc;
    }

    public LayerDefinition getLayerDefinition() {
        return layerDefinition;
    }

    public int getLayerOpacity() {
        return layerOpacity;
    }

    public AffineTransform getTransform() {
        return transform;
    }

    public void setFc(DefaultFeatureCollection fc) {
        this.fc = fc;
    }

    public void setLayerDefinition(LayerDefinition layerDefinition) {
        this.layerDefinition = layerDefinition;
    }

    public void setLayerOpacity(int layerOpacity) {
        this.layerOpacity = layerOpacity;
    }

    public void setTransform(AffineTransform transform) {
        this.transform = transform;
    }

}
