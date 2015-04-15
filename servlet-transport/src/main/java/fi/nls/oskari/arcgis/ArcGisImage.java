package fi.nls.oskari.arcgis;

import fi.nls.oskari.arcgis.pojo.ArcGisFeature;
import fi.nls.oskari.arcgis.pojo.ArcGisLayerStore;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.Location;
import fi.nls.oskari.pojo.Tile;
import fi.nls.oskari.pojo.WFSCustomStyleStore;
import fi.nls.oskari.wfs.WFSImage;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import fi.nls.oskari.wfs.util.HttpHelper;
import org.geotools.geometry.jts.ReferencedEnvelope;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Image drawing for WFS layers
 */
public class ArcGisImage extends WFSImage {
    private static final Logger log = LogFactory.getLogger(ArcGisImage.class);

    private Location location; // location of the tile (modified if not map)
    private ArrayList<ArcGisFeature> features;
    private int imageWidth = 0;
    private int imageHeight = 0;
    private double bufferSize = 0.0d;
    private int bufferedImageWidth = 0;
    private int bufferedImageHeight = 0;

    WFSCustomStyleStore customStyle;
    private boolean isHighlight = false;
    private boolean isTile = false;
    private WFSLayerStore layer;
    private ArcGisLayerStore arcGisLayer;
    private List<ArcGisLayerStore> arcGisLayers;
    private String token;

    /**
     * Constructor for image of certain layer and style
     *
     * @param layer
     * @param styleName
     */
    public ArcGisImage(WFSLayerStore layer,
                       ArcGisLayerStore arcGisLayer,
                       List<ArcGisLayerStore> arcGisLayers,
                       String client,
                       String styleName,
                       String highlightStyleName,
                       String token) {
        if (layer == null || styleName == null) {
            log.error("Failed to construct image (undefined params)");
            return;
        }

        this.layer = layer;
        this.arcGisLayer = arcGisLayer;
        this.arcGisLayers = arcGisLayers;
        this.token = token;

        // check if tile buffer is given
        String tileBufferKey;
        if (styleName.startsWith(PREFIX_CUSTOM_STYLE)) {
            tileBufferKey = PREFIX_CUSTOM_STYLE;
        } else {
            tileBufferKey = styleName;
        }
        bufferSize = layer.getTileBuffer(tileBufferKey, bufferSize);
        log.debug(tileBufferKey, "=", bufferSize);

        // TODO: possibility to change the custom style store key to sessionID (it is hard without connection to get client)
        if (styleName.startsWith(PREFIX_CUSTOM_STYLE) && client != null) {
            try {
                this.customStyle = WFSCustomStyleStore.create(client, layer.getLayerId());
                if (this.customStyle == null) {
                    log.error("WFSCustomStyleStore not created", client, layer.getLayerId());
                    return;
                }
                this.customStyle.setGeometry(layer.getGMLGeometryProperty().replaceAll("^[^_]*:", "")); // set the geometry name
                log.debug(this.customStyle.getGeometry());

                if (highlightStyleName == null) {
                } else {
                    isHighlight = true;
                }
            } catch (Exception e) {
                log.error(e, "JSON parsing failed for WFSCustomStyleStore");
                return;
            }
        } else if (highlightStyleName == null) {
        } else {
            isHighlight = true;
        }
    }


    public static BufferedImage streamToImage(InputStream stream) {
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(stream);
        } catch (Exception e) {
            log.error(e, "Image could not be read into stream");
        }
        return bufferedImage;
    }


    /**
     * Creates a image of the WFS layer's data
     *
     * @param tile
     * @param location
     * @param features
     * @return image
     */
    public BufferedImage draw(Tile tile,
                              Location location,
                              ArrayList<ArcGisFeature> features) {
        return draw(tile, location, null, features);
    }

    /**
     * Creates a image of the WFS layer's data
     *
     * @param tile
     * @param location
     * @param bounds
     * @param features
     * @return image
     */
    public BufferedImage draw(Tile tile,
                              Location location,
                              List<Double> bounds,
                              ArrayList<ArcGisFeature> features) {

        this.imageWidth = tile.getWidth();
        this.imageHeight = tile.getHeight();

        if (bounds == null) {
            this.location = location;
        } else {
            this.location = new Location(location.getSrs());
            this.location.setBbox(bounds);

            // enlarge if tile and buffer is defined
            this.isTile = true;
            if (bufferSize != 0.0d) {
                this.bufferedImageWidth = imageWidth + (int) (imageWidth * bufferSize);
                this.bufferedImageHeight = imageHeight + (int) (imageWidth * bufferSize);
            }
        }

        this.features = features;

        if (imageWidth == 0 ||
                imageHeight == 0 ||
                this.location == null ||
                features == null) {
            log.warn("Not enough information to draw");
            log.warn(imageWidth);
            log.warn(imageHeight);
            log.warn(location);
            log.warn(features.isEmpty());
            return null;
        }

        return this.draw();
    }

    /**
     * Creates a image of the WFS layer's data
     *
     * @return image
     */
    private BufferedImage draw() {
        //TODO: feature

        ReferencedEnvelope bounds = location.getEnvelope();

        Rectangle screenArea;
        if (isTile && bufferSize != 0.0d) {
            double width = (location.getRight() - location.getLeft()) / 2 * bufferSize;
            double height = (location.getTop() - location.getBottom()) / 2 * bufferSize;
            bounds = location.createEnlargedEnvelope(width, height);
            screenArea = new Rectangle(0, 0, bufferedImageWidth, bufferedImageHeight);
        } else {
            screenArea = new Rectangle(0, 0, imageWidth, imageHeight); // image size
        }

        //SRS
        String mapSrs = location.getSrs();
        //dynamic layers

        String url = layer.getURL() + "/export?";
        String payload = ArcGisCommunicator.createImageRequestPayload(layer, arcGisLayer, arcGisLayers, screenArea, bounds, mapSrs, token);

        if (isHighlight) {
            WFSCustomStyleStore highlightStyle = getDefaultHighlightStyle();
            String stylePayload = ArcGisCommunicator.createHighlightStyleRequestPayload(layer, arcGisLayer, arcGisLayers, features, highlightStyle);
            payload += stylePayload;
        } else if (this.customStyle != null) {
            String stylePayload = ArcGisCommunicator.createStyleRequestPayload(layer, arcGisLayer, arcGisLayers, customStyle);
            payload += stylePayload;
        }

        BufferedInputStream response = HttpHelper.getRequestStream(url + payload, "", layer.getUsername(), layer.getPassword());

        return streamToImage(response);
    }

    private WFSCustomStyleStore getDefaultHighlightStyle() {
        WFSCustomStyleStore result = new WFSCustomStyleStore();

        result.setFillColor("#f5af3c");
        result.setFillPattern(-1);

        result.setBorderDasharray("");
        result.setBorderColor("#000000");
        result.setBorderWidth(2);

        result.setStrokeColor("#f5af3c");
        result.setStrokeDasharray("");
        result.setStrokeWidth(2);

        result.setDotColor("#f5af3c");
        result.setDotSize(2);
        result.setDotShape(5);

        return result;
    }

}
