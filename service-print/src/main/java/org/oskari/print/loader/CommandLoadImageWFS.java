package org.oskari.print.loader;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintTile;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

public class CommandLoadImageWFS extends HystrixCommand<BufferedImage> {

    private final PrintLayer layer;
    private final int width;
    private final int height;
    private final double[] bbox;

    public CommandLoadImageWFS(PrintLayer layer, 
            int width, 
            int height, 
            double[] bbox) {
        super(HystrixCommandGroupKey.Factory.asKey(AsyncImageLoader.GROUP_KEY));
        this.layer = layer;
        this.width = width;
        this.height = height;
        this.bbox = bbox;
    }

    @Override
    public BufferedImage run() throws Exception {
        final PrintTile[] tiles = layer.getTiles();
        final List<Future<BufferedImage>> images = new ArrayList<>(tiles.length);

        for (int i = 0; i < tiles.length; i++) {
            String url = tiles[i].getURL();
            images.add(new CommandLoadImageFromURL(commandGroup, url).queue());
        }

        final double x1 = bbox[0];
        final double y1 = bbox[1];

        final double distanceWidth = bbox[2] - x1;
        final double distanceHeight = bbox[3] - y1;

        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvas.createGraphics();

        for (int i = 0; i < tiles.length; i++) {
            PrintTile tile = tiles[i];

            double[] tileBbox = tile.getBbox();
            // Flip y-axis, BufferedImages (0,0) is at top left
            int dx1 = getPt(tileBbox[0], x1, distanceWidth, width);
            int dy1 = getPt(tileBbox[3], y1, distanceHeight, height);
            int dx2 = getPt(tileBbox[2], x1, distanceWidth, width);
            int dy2 = getPt(tileBbox[1], y1, distanceHeight, height);

            BufferedImage img = images.get(i).get(5L, TimeUnit.SECONDS);
            g2d.drawImage(img, dx1, dy1, dx2, dy2, 0, 0, img.getWidth(), img.getHeight(), null);
        }

        g2d.dispose();
        return canvas;
    }

    private int getPt(double coordinate, double min, double distance, int points) {
        double percent = (coordinate - min) / distance;
        return (int) Math.round(percent * points);
    }

}
