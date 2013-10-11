package fi.nls.oskari.wfs;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;

import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.log.LogFactory;
import org.apache.commons.codec.binary.Base64;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.referencing.CRS;
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

import redis.clients.jedis.Jedis;

import fi.nls.oskari.log.Logger;
import fi.nls.oskari.pojo.Location;
import fi.nls.oskari.pojo.Tile;
import fi.nls.oskari.pojo.WFSLayerStore;

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

    private static final String KEY = "WFSImage_";

    private static final String STYLE_DEFAULT = "default";
    private static final String STYLE_HIGHLIGHT = "highlight";

    private static final String DEFAULT_SLD = "sld_default.xml";
    private static final String HIGHLIGHT_SLD = "sld_highlight.xml";
    
    private WFSLayerStore layer;
    private Location location; // location of the tile (modified if not map)
    private String styleName;
    private FeatureCollection<SimpleFeatureType, SimpleFeature> features;
    private int imageWidth = 0;
    private int imageHeight = 0;
    
    /**
     * Constructor for tile image
     * 
     * @param layer
     * @param tile
     * @param location
     * @param bounds
     * @param styleName
     * @param features
     */
    public WFSImage(WFSLayerStore layer, 
			Tile tile, 
			Location location, 
			List<Double> bounds, 
			String styleName,
			FeatureCollection<SimpleFeatureType, SimpleFeature> features) {

    	this.layer = layer;
    	this.location = new Location(location.getSrs());
    	this.location.setBbox(bounds);
    	this.styleName = styleName;
    	this.features = features;

		this.imageWidth = tile.getWidth();
		this.imageHeight = tile.getHeight();
    }
    
    /**
     * Constructor for map image
     * 
     * @param layer
     * @param tile
     * @param location
     * @param styleName
     * @param features
     */
    public WFSImage(WFSLayerStore layer, 
			Tile tile, 
			Location location,
			String styleName,
			FeatureCollection<SimpleFeatureType, SimpleFeature> features) {
    	this.layer = layer;
    	this.location = location;
    	this.styleName = styleName;
    	this.features = features;
    	
		this.imageWidth = tile.getWidth();
		this.imageHeight = tile.getHeight();
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

    	byte[] byteImage = imageToBytes(bufferedImage);
    	String sBbox = bbox[0] + "-" + bbox[1] + "-" + bbox[2]+ "-" + bbox[3];
    	String sKey = KEY + layerId + "_" + styleName + "_" + srs + "_" + sBbox + "_" + zoom;
    	if(!persistent) {
    		sKey = sKey + "_temp";
    	}
    	byte[] key = sKey.getBytes();

		JedisManager.setex(key, 604800, byteImage);
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
     * Creates a tile image of the WFS layer's data
     * 
     * @return image
     */
	public BufferedImage draw() {
		if (layer == null ||
				imageWidth == 0 ||
				imageHeight == 0 ||
				location == null ||
				styleName == null ||
				features == null) {
            log.warn("Not enough information to draw");
            log.warn(imageWidth);
            log.warn(imageHeight);
            log.warn(location);
            log.warn(styleName);
            log.warn(features);
			return null; // TODO: possibility to send error picture (now empty)
		}
		
		MapContent content = new MapContent();
		MapViewport viewport = new MapViewport();

        // TODO: make this in advance - no need to generate per image
		CoordinateReferenceSystem crs = null;
		try {
			crs = CRS.decode(location.getSrs());
		} catch (Exception e) {
			log.error(e, "CRS decoding failed");
		}
		
		Rectangle screenArea = new Rectangle(0, 0, imageWidth, imageHeight); // image size		
		ReferencedEnvelope bounds = new ReferencedEnvelope(
				location.getLeft(), // x1
				location.getRight(), // x2
				location.getBottom(), // y1
				location.getTop(), // y2
				crs
		); // map coordinates

		viewport.setCoordinateReferenceSystem(crs);
		viewport.setScreenArea(screenArea);
		viewport.setBounds(bounds);
        viewport.setMatchingAspectRatio(true);

        // TODO: style could be done before coming to image loop (1 timer!) - here slows down!
        if(features.size() > 0) {
            Style style;
            if(layer.getStyles().containsKey(styleName)) {
                style = createSLDStyle(layer.getStyles().get(styleName).getSLDStyle());
            }
            else if(styleName.equals(STYLE_HIGHLIGHT)) {
                if(layer.getSelectionSLDStyle() != null) {
                    style = createSLDStyle(layer.getSelectionSLDStyle());
                } else { // default highlight
                    style = createSLDStyle(WFSImage.class.getResourceAsStream(HIGHLIGHT_SLD)); // getClass() (non-static)
                }
            } else {
                if(layer.getStyles().containsKey(STYLE_DEFAULT)) {
                    style = createSLDStyle(layer.getStyles().get(STYLE_DEFAULT).getSLDStyle());
                }
                else { // default
                    style = createSLDStyle(WFSImage.class.getResourceAsStream(DEFAULT_SLD)); // getClass() (non-static)
                }
            }

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
	    BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);

	    GTRenderer renderer = new StreamingRenderer();
	    renderer.setMapContent(content);

		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		renderer.paint(g, new Rectangle(imageWidth, imageHeight), content.getViewport().getBounds());
		content.dispose();
	    return image;
	}
	
	/**
	 * Parses SLD style from a String (XML)
	 * 
	 * @param xml
	 * @return sld
	 */
	private Style createSLDStyle(String xml) {
		return createSLDStyle(new ByteArrayInputStream(xml.getBytes()));
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
			log.error(xml);
			return null;
		}
		return SLD.styles(sld)[0]; 
	}
}
