package fi.nls.oskari.wfs;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.domain.map.wfs.WFSSLDStyle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.Location;
import fi.nls.oskari.pojo.Tile;
import fi.nls.oskari.pojo.WFSCustomStyleStore;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.wfs.pojo.WFSLayerStore;
import org.apache.commons.codec.binary.Base64;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.sld.SLDConfiguration;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Image drawing for WFS layers 
 */
public class WFSImage {
    private static final Logger log = LogFactory.getLogger(WFSImage.class);

    // Maybe hazardous because static ImageIO setter (changes this setting for all!)
    // NOT using disk for cache [ http://docs.oracle.com/javase/7/docs/api/javax/imageio/ImageIO.html#setUseCache(boolean) ]
    static {
        ImageIO.setUseCache(false);
    }

    public static final String KEY = "WFSImage_";
    public static final String PREFIX_CUSTOM_STYLE = "oskari_custom";

    public static final String STYLE_DEFAULT = "default";
    public static final String STYLE_HIGHLIGHT = "highlight";

    public static final String DEFAULT_SLD = "sld_default.xml";
    public static final String HIGHLIGHT_SLD = "sld_highlight.xml";
    public static final String OSKARI_CUSTOM_SLD = "sld_oskari_custom.xml";

    public static final String GEOM_TYPE_PLACEHOLDER = "wfsGeometryType";

    private Style style;

    private Location location; // location of the tile (modified if not map)
    private FeatureCollection<SimpleFeatureType, SimpleFeature> features;
    private int imageWidth = 0;
    private int imageHeight = 0;
    private double bufferSize = 0.0d;
    private int bufferedImageWidth = 0;
    private int bufferedImageHeight = 0;

    WFSCustomStyleStore customStyle;
    private boolean isHighlight = false;
    private boolean isTile = false;

    protected WFSImage() {

    }
    /**
     * Constructor for image of certain layer and style
     *
     * @param layer
     * @param styleName
     */
    public WFSImage(WFSLayerStore layer, String client, String styleName, String highlightStyleName) {
        if(layer == null || styleName == null) {
            log.error("Failed to construct image (undefined params)");
            return;
        }

        // check if tile buffer is given
        String tileBufferKey;
        if(styleName.startsWith(PREFIX_CUSTOM_STYLE)) {
            tileBufferKey = PREFIX_CUSTOM_STYLE;
        } else {
            tileBufferKey = styleName;
        }
        bufferSize = layer.getTileBuffer(tileBufferKey, bufferSize);
        log.debug(tileBufferKey, "=", bufferSize);

        // TODO: possibility to change the custom style store key to sessionID (it is hard without connection to get client)
        if(styleName.startsWith(PREFIX_CUSTOM_STYLE) && client != null) {
            try {
                this.customStyle = WFSCustomStyleStore.create(client, layer.getLayerId());
                if(this.customStyle == null) {
                    this.style = null;
                    log.error("WFSCustomStyleStore not created", client, layer.getLayerId());
                    return;
                }
                this.customStyle.setGeometry(layer.getGMLGeometryProperty().replaceAll("^[^_]*:", "")); // set the geometry name
                log.debug(this.customStyle.getGeometry());

                if(highlightStyleName == null) {
                    this.style = createCustomSLDStyle();
                } else {
                    isHighlight = true;
                    this.style = createCustomSLDStyle();
                }
            } catch(Exception e) {
                this.style = null;
                log.error(e, "JSON parsing failed for WFSCustomStyleStore");
                return;
            }
        } else if(highlightStyleName == null) {
            this.style = getSLDStyle(layer, styleName);
        } else {
            isHighlight = true;
            this.style = getSLDStyle(layer, highlightStyleName);
        }
    }

    /**
     * Gets bufferedImage from cache (persistant)
     *
     * @param layerId
     * @param srs
     * @param bbox
     * @param zoom
     * @return buffered image from cache
     */
    public static BufferedImage getCache(String layerId, String styleName, String srs, Double[] bbox, long zoom) {
        return getCache(layerId, styleName, srs, bbox, zoom, true);
    }

    /**
     * Gets bufferedImage from cache
     *
     * @param layerId
     * @param srs
     * @param bbox
     * @param zoom
     * @param persistent
     * @return buffered image from cache
     */
    public static BufferedImage getCache(String layerId,
                                         String styleName,
                                         String srs,
                                         Double[] bbox,
                                         long zoom,
                                         boolean persistent) {
        if(layerId == null ||
                styleName == null ||
                srs == null ||
                bbox.length != 4) {
            log.error("Cache key couldn't be created");
            return null;
        }

        // no persistent cache for custom styles
        if(styleName.startsWith(PREFIX_CUSTOM_STYLE) && persistent) {
            return null;
        }

        String sBbox = bbox[0] + "-" + bbox[1] + "-" + bbox[2]+ "-" + bbox[3];
        String sKey = KEY + layerId + "_" + styleName + "_"  + srs + "_" + sBbox + "_" + zoom;
        if(!persistent) {
            sKey = sKey + "_temp";
        }

        byte[] key = sKey.getBytes();
        byte[] bytes = JedisManager.get(key);
        if(bytes != null)
            return bytesToImage(bytes);
        return null;
    }

    /**
     * Sets bufferedImage to cache
     *
     * @param layerId
     * @param srs
     * @param bbox
     * @param zoom
     * @param persistent
     * @return buffered image from cache
     */
    public static void setCache(BufferedImage bufferedImage,
                                String layerId,
                                String styleName,
                                String srs,
                                Double[] bbox,
                                long zoom,
                                boolean persistent) {
        if(layerId == null ||
                styleName == null ||
                srs == null ||
                bbox.length != 4) {
            log.error("Cache key couldn't be created");
            return;
        }

        // no persistent cache for custom styles
        if(styleName.startsWith(PREFIX_CUSTOM_STYLE)) {
            persistent = false;
        }

        byte[] byteImage = imageToBytes(bufferedImage);
        String sBbox = bbox[0] + "-" + bbox[1] + "-" + bbox[2]+ "-" + bbox[3];
        String sKey = KEY + layerId + "_" + styleName + "_" + srs + "_" + sBbox + "_" + zoom;
        if(!persistent) {
            sKey = sKey + "_temp";
        }

        byte[] key = sKey.getBytes();

        JedisManager.setex(key, 86400, byteImage);
    }

    /**
     * Transforms bufferedImage to byte[]
     *
     * @param bufferedImage
     * @return image
     */
    public static byte[] imageToBytes(BufferedImage bufferedImage) {
        if(bufferedImage == null) {
            log.error("No image given");
            return null;
        }

        ByteArrayOutputStream byteaOutput = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", byteaOutput);
            byteaOutput.flush();
            byteaOutput.close();
        } catch (Exception e) {
            log.error(e, "Image could not be written into stream");
        }
        return byteaOutput.toByteArray();
    }

    /**
     * Transforms byte[] to BufferedImage
     *
     * @param byteImage
     * @return image
     */
    public static BufferedImage bytesToImage(byte[] byteImage) {
        BufferedImage bufferedImage = null;
        ByteArrayInputStream byteaInput = null;
        if(byteImage != null) {
            byteaInput = new ByteArrayInputStream(byteImage);
            try {
                bufferedImage = ImageIO.read(byteaInput);
                byteaInput.close();
            } catch (Exception e) {
                log.error(e, "Image could not be read into stream");
            }
        }
        return bufferedImage;
    }

    /**
     * Converts byte[] to Base64 formatted String
     *
     * @param byteImage
     * @return base64
     */
    public static String bytesToBase64(byte[] byteImage) {
        return new String(Base64.encodeBase64(byteImage));
    }


    /**
     * Creates a image of the WFS layer's data
     *
     * @param tile
     * @param location
     * @param features
     *
     * @return image
     */
    public BufferedImage draw(Tile tile,
                              Location location,
                              FeatureCollection<SimpleFeatureType, SimpleFeature> features) {
        return draw(tile, location, null, features);
    }

    /**
     * Creates a image of the WFS layer's data
     *
     * @param tile
     * @param location
     * @param bounds
     * @param features
     *
     * @return image
     */
    public BufferedImage draw(Tile tile,
                              Location location,
                              List<Double> bounds,
                              FeatureCollection<SimpleFeatureType, SimpleFeature> features) {

        this.imageWidth = tile.getWidth();
        this.imageHeight = tile.getHeight();

        if(bounds == null) {
            this.location = location;  //<--  axis order doesn't work correct
           // this.location = new Location(location.getSrs());
           // this.location.setBbox(location.getBbox());
        } else {
            this.location = new Location(location.getSrs());
            this.location.setBbox(bounds);

            // enlarge if tile and buffer is defined
            this.isTile = true;
            if(bufferSize != 0.0d) {
                this.bufferedImageWidth = imageWidth+(int)(imageWidth*bufferSize);
                this.bufferedImageHeight = imageHeight+(int)(imageWidth*bufferSize);
            }
        }

        this.features = features;

        if (imageWidth == 0 ||
                imageHeight == 0 ||
                this.location == null ||
                style == null ||
                features == null) {
            log.warn("Not enough information to draw");
            log.warn(imageWidth);
            log.warn(imageHeight);
            log.warn(location);
            log.warn(style);
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
        MapContent content = new MapContent();
        MapViewport viewport = new MapViewport();

        CoordinateReferenceSystem crs = location.getCrsForMap();
        ReferencedEnvelope bounds = location.getEnvelopeForMap();

        Rectangle screenArea;
        if(isTile && bufferSize != 0.0d) {
            double width = (location.getRight() - location.getLeft())/2 * bufferSize;
            double height = (location.getTop() - location.getBottom())/2 * bufferSize;
            // trick: bounds must be without crs (.crs=null)
            bounds = location.createEnlargedEnvelope(width, height);
            screenArea = new Rectangle(0, 0, bufferedImageWidth, bufferedImageHeight);
            
            
            log.debug(" Enlarged "+bounds+" "+screenArea+" in "+crs);
            
        } else {
            screenArea = new Rectangle(0, 0, imageWidth, imageHeight); // image size
            // trick: bounds must be without crs (.crs=null)
            bounds = location.getEnvelopeForMapNoCrs();
            log.debug(" Normal "+bounds+" "+screenArea+ " in "+crs);
        }

        viewport.setCoordinateReferenceSystem(crs);
        viewport.setScreenArea(screenArea);
        viewport.setBounds(bounds);
        viewport.setMatchingAspectRatio(true);

        if(features.size() > 0) {
            Layer featureLayer = new FeatureLayer(features, style);
            content.addLayer(featureLayer);
        }

        content.setViewport(viewport);

        return saveImage(content);
    }

    /**
     * Draws map content data into image
     *
     * @param content
     * @return image
     */
    private BufferedImage saveImage(MapContent content) {
        BufferedImage image;
        if(isTile && bufferSize != 0.0d) {
            image = new BufferedImage(bufferedImageWidth,
                    bufferedImageHeight,
                    BufferedImage.TYPE_4BYTE_ABGR);
        } else {
            image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
        }

        GTRenderer renderer = new StreamingRenderer();
        renderer.setMapContent(content);

        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if(isTile && bufferSize != 0.0d) {
            renderer.paint(g, new Rectangle(bufferedImageWidth,
                    bufferedImageHeight),
                    content.getViewport().getBounds());
            try {
                image = image.getSubimage((int)(imageWidth*bufferSize)/2,
                        (int)(imageWidth*bufferSize)/2,
                        imageWidth,
                        imageHeight);
            } catch(Exception e) {
                log.error(e, "Image cropping failed");
            }
        } else {
            renderer.paint(g, new Rectangle(imageWidth, imageHeight), content.getViewport().getBounds());
        }

        content.dispose();
        return image;
    }

    /**
     * Creates SLD style
     *
     * @param layer
     * @param styleName
     * @return style
     */
    protected Style getSLDStyle(WFSLayerStore layer, String styleName) {
        log.debug("Trying to get style with name:", styleName);
        // try to find with name
        Style style = createSLDStyle(layer.getSLDStyle(styleName));
        if(style == null) {
            // if not found, use selection style for highlight or try default
            if(STYLE_HIGHLIGHT.equals(styleName)) {
                style = createSLDStyle(layer.getSelectionSLDStyle());
            }
            else {
                style = createSLDStyle(layer.getDefaultSLDStyle());
            }
        }

        // if styles couldn't be parsed, use defaults
        if(style == null) {
            log.info("Layer style not customized or parsing failed. Using defaults.");
            if(STYLE_HIGHLIGHT.equals(styleName)) {
                //style = createDefaultHighlightSLDStyle(layer.getGMLGeometryProperty());
                // TODO: check if we really always want to use without namespace
                style = createDefaultHighlightSLDStyle(layer.getGMLGeometryPropertyNoNamespace());
            }
            else {
                // style = createSLDStyle(WFSImage.class.getResourceAsStream(DEFAULT_SLD)); // getClass() (non-static)
                // TODO: check if we really always want to use without namespace
                style = createDefaultSLDStyle(layer.getGMLGeometryPropertyNoNamespace());
            }
        }
        if(style == null) {
            // something is seriously wrong, even default styles can't be parsed
            log.error("Failed to get SLD style (even default failed)!!");
        }

        return style;
    }

    private Style createSLDStyle(WFSSLDStyle style) {
        if(style == null) {
            return null;
        }
        return createSLDStyle(style.getSLDStyle());
    }
	/**
	 * Parses SLD style from a String (XML)
	 * 
	 * @param xml
	 * @return sld
	 */
	private Style createSLDStyle(String xml) {
        if(xml == null) {
            log.info("Trying to create style from <null> String!");
            return null;
        }
        final Style style = createSLDStyle(new ByteArrayInputStream(xml.getBytes()));
        if(style == null) {
            log.warn("Couldn't create style from XML:", xml);
        }
		return style;
	}	

    /**
     * Parses SLD style from an InputStream (XML)
     *
     * @param xml
     * @return sld
     */
    private Style createSLDStyle(InputStream xml) {
        Configuration config = new SLDConfiguration();
        Parser parser = new Parser(config);
        StyledLayerDescriptor sld = null;
        try {
            sld = (StyledLayerDescriptor) parser.parse(xml);
        } catch (Exception e) {
            log.error(e, "Failed to create SLD Style");
            return null;
        }
        return SLD.styles(sld)[0];
    }

    /**
     * Creates own sld style by replacing
     *
     * @return sld
     */
    public Style createCustomSLDStyle() {
        InputStream resource = WFSImage.class.getResourceAsStream(OSKARI_CUSTOM_SLD);
        try {
            String xml = IOHelper.readString(resource, "ISO-8859-1");
            customStyle.replaceValues(xml, isHighlight);
            xml = customStyle.getSld();
            return createSLDStyle(xml);
        } catch(Exception e) {
            log.error(e, "Failed to get Own SLD Style");
            log.error(resource);
        }
        return null;
    }
    /**
     * Creates default highlight sld style by replacing geomtype
     *
     * @return sld
     */
    public Style createDefaultHighlightSLDStyle(String geom_type) {
        log.debug("Creating default highlight SLD for:", geom_type);
        InputStream resource = WFSImage.class.getResourceAsStream(HIGHLIGHT_SLD);
        try {
            String xml = IOHelper.readString(resource, "ISO-8859-1");
            xml = xml.replaceAll(GEOM_TYPE_PLACEHOLDER, geom_type);
            return createSLDStyle(xml);
        } catch(Exception e) {
            log.error(e, "Failed to get Default highlight SLD Style - geom type ", geom_type);
            log.error(resource);
        }
        return null;
    }

    /**
     * Creates default sld style by replacing geomtype
     *
     * @return sld
     */
    public Style createDefaultSLDStyle(String geom_type) {
        log.debug("Creating default highlight SLD for:", geom_type);
        InputStream resource = WFSImage.class.getResourceAsStream(DEFAULT_SLD);
        try {
            String xml = IOHelper.readString(resource, "ISO-8859-1");
            xml = xml.replaceAll(GEOM_TYPE_PLACEHOLDER, geom_type);
            return createSLDStyle(xml);
        } catch(Exception e) {
            log.error(e, "Failed to get Default SLD Style - geom type ", geom_type);
            log.error(resource);
        }
        return null;
    }

    public Style getStyle() {
        return style;
    }
}
