package org.oskari.print.loader;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;

import org.oskari.print.request.PrintLayer;
import org.oskari.print.request.PrintTile;

public class CommandLoadImageWFS extends CommandLoadImageBase {

    private final PrintLayer layer;
    private final int width;
    private final int height;
    private final double[] bbox;

    public CommandLoadImageWFS(PrintLayer layer,
            int width,
            int height,
            double[] bbox) {
        super(Integer.toString(layer.getId()));
        this.layer = layer;
        this.width = width;
        this.height = height;
        this.bbox = bbox;
    }

    @Override
    public BufferedImage run() throws Exception {
        PrintTile[] tiles = layer.getTiles();
        if (tiles == null) {
            // Preview doesn't set tiles
            return null;
        }

        // Sort the tiles so that we will combine the tiles
        // from top to bottom and left to right
        // The 'Why?' is the memory layout of BufferedImage
        // Is there really a performance difference?
        // Maybe not, but the sort is cheap anyway
        Arrays.sort(tiles, new Comparator<PrintTile>() {
            @Override
            public int compare(PrintTile t1, PrintTile t2) {
                // y1 here is the "max north" which translates to the row in the "master-image"
                if (t1.getBbox() == null) {
                    return t2.getBbox() == null ? 0 : 1;
                }
                if (t2.getBbox() == null) {
                    return -1;
                }
                double t1x1 = t1.getBbox()[0];
                double t1y1 = t1.getBbox()[3];
                double t2x1 = t2.getBbox()[0];
                double t2y1 = t2.getBbox()[3];
                int c = Double.compare(t2y1, t1y1);
                return c != 0 ? c : Double.compare(t1x1, t2x1); 
            }
        });

        String layerId = Integer.toString(layer.getId());
        List<Future<BufferedImage>> images = new ArrayList<>(tiles.length);
        for (PrintTile tile : tiles) {
            images.add(new CommandLoadImageFromURL(layerId, tile.getURL()).queue()); 
        }

        final double x1 = bbox[0];
        final double y1 = bbox[3];
        final double widthInNature = Math.abs(bbox[2] - x1);
        final double heightInNature = Math.abs(y1 - bbox[1]);

        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvas.createGraphics();

        for (int i = 0; i < tiles.length; i++) {
            PrintTile tile = tiles[i];
            double[] tileBbox = tile.getBbox();
            // Flip y-axis, BufferedImages (0,0) is at top left
            int dx1 = getPt(tileBbox[0], x1, widthInNature, width);
            int dy1 = getPt(y1, tileBbox[3], heightInNature, height);
            int dx2 = getPt(tileBbox[2], x1, widthInNature, width);
            int dy2 = getPt(y1, tileBbox[1], heightInNature, height);

            BufferedImage img = images.get(i).get();
            if (img != null) {
                g2d.drawImage(img, dx1, dy1, dx2, dy2, 0, 0, img.getWidth(), img.getHeight(), null);
            }
        }

        g2d.dispose();
        return canvas;
    }

    private int getPt(double coordinate, double min, double distance, int points) {
        double percent = (coordinate - min) / distance;
        return (int) Math.round(percent * points);
    }

}