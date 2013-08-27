package fi.mml.portti.service.ogc.handler.action.asker;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.sld.SLDConfiguration;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.SAXException;

import fi.mml.map.mapwindow.service.db.MapLayerService;
import fi.mml.map.mapwindow.service.db.MapLayerServiceIbatisImpl;
import fi.mml.portti.domain.ogc.util.Epsg3067CoordinateReferenceSystem;
import fi.mml.portti.domain.ogc.util.OskariBBOX;
import fi.mml.portti.service.ogc.handler.FlowModel;
import fi.nls.oskari.domain.map.wfs.WFSLayer;

public class BaseAskerAction {

    final static private String WIDTH = "WIDTH";
    final static private String HEIGHT = "HEIGHT";
    
    final static private String FLOW_PM_MAP_HEIGHT = "flow_pm_map_height";
    
    final static private String FLOW_PM_MAP_WIDTH = "flow_pm_map_width";
    
    final static private String BBOX = "BBOX";
    
    final static private String PROJECTION = "EPSG:3067";
    
	
	final static int[] levelModifiers = {
		2048*4,2048*2, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1    	
	};
	
	public static final int DEFAULT_WIDTH = 746;

	public static final int DEFAULT_HEIGHT = 569;
	
	MapLayerService wfsLayerDbService = new MapLayerServiceIbatisImpl();
	
	protected MapLayerService getWfsLayerDbService() {
		return wfsLayerDbService;
	}
	
	
	protected OskariBBOX getOskariBBOX(FlowModel flowModel) {
		
		OskariBBOX oskariBbox = new OskariBBOX();
		
		if (!flowModel.isEmpty(BBOX)) {
			String[] bbox = flowModel.getAsString(BBOX).split(",");
			oskariBbox.setBboxMinX(Double.parseDouble(bbox[0]));
			oskariBbox.setBboxMinY(Double.parseDouble(bbox[1]));
			oskariBbox.setBboxMaxX(Double.parseDouble(bbox[2]));
			oskariBbox.setBboxMaxY(Double.parseDouble(bbox[3]));
		} else { 
			oskariBbox.setBboxMinX(Double.parseDouble(flowModel.getAsString(FlowModel.FLOW_PM_BBOX_MIN_X))); 
			oskariBbox.setBboxMinY(Double.parseDouble(flowModel.getAsString(FlowModel.FLOW_PM_BBOX_MIN_Y))); 
			oskariBbox.setBboxMaxX(Double.parseDouble(flowModel.getAsString(FlowModel.FLOW_PM_BBOX_MAX_X)));
			oskariBbox.setBboxMaxY(Double.parseDouble(flowModel.getAsString(FlowModel.FLOW_PM_BBOX_MAX_Y)));
		}
		
		return oskariBbox;
		
	}
	
	/**
	 * Returns BBox that is current size of the map
	 * 
	 * @param flowModel
	 * @return
	 */
	protected Filter getMapViewBbox(FilterFactory2 ff, String geomName, FlowModel flowModel) {
		
		OskariBBOX oskariBbox = getOskariBBOX(flowModel);
		
		return ff.bbox(
				ff.property(geomName),
				oskariBbox.getBboxMinX(),
				oskariBbox.getBboxMinY(),
				oskariBbox.getBboxMaxX(),
				oskariBbox.getBboxMaxY(),
				PROJECTION);
	}
	
	/**
	 * Returns BBox that is current size of the map
	 * 
	 * @param flowModel
	 * @return
	 */
	protected Filter getExtendedMapViewBbox(FilterFactory2 ff, String geomName, FlowModel flowModel) {
		
		OskariBBOX oskariBbox = getOskariBBOX(flowModel);
		
		double expander = 0;
		
		if (!flowModel.isEmpty("flow_pm_zoom_level")) {
			expander = levelModifiers[Integer.parseInt(flowModel.getAsString("flow_pm_zoom_level"))];
		}
		return ff.bbox(
				ff.property(geomName), 
				oskariBbox.getBboxMinX()-expander, 
				oskariBbox.getBboxMinY()-expander, 
				oskariBbox.getBboxMaxX()+expander,
				oskariBbox.getBboxMaxY()+expander, 
				PROJECTION);
	}
	
	/**
	 * Renders an image from given mapContext
	 * 
	 * @param width
	 * @param height
	 * @param mapContext
	 * @param bounds
	 * @return
	 */
	
	protected BufferedImage renderImageFromFeatures(FlowModel flowModel, MapContext mapContext, ReferencedEnvelope bounds) {
		
		/* Build image */
		Integer imageWidth = DEFAULT_WIDTH;
		
		if (!flowModel.isEmpty(WIDTH)) {
		    imageWidth = Integer.parseInt(flowModel.getAsString(WIDTH));
		} else if (!flowModel.isEmpty(FLOW_PM_MAP_WIDTH)) {
			imageWidth = Integer.parseInt(flowModel.getAsString(FLOW_PM_MAP_WIDTH));
		} 
		
		Integer imageHeight = DEFAULT_HEIGHT;
		
		if (!flowModel.isEmpty(HEIGHT)) {
		    imageHeight = Integer.parseInt(flowModel.getAsString(HEIGHT));
        } else if (!flowModel.isEmpty(FLOW_PM_MAP_HEIGHT)) {
			imageHeight = Integer.parseInt(flowModel.getAsString(FLOW_PM_MAP_HEIGHT));
		} 	
		
		BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
		
		StreamingRenderer sr = new StreamingRenderer();
		sr.setContext(mapContext);

		Graphics2D g = (Graphics2D) image.getGraphics();
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		sr.paint(g, new Rectangle(imageWidth, imageHeight), bounds);        
		return image;
	}

	/**
	 * Builds a map context 
	 * 
	 * @param bbox
	 * @return
	 */
	protected MapContext buildMapContext() {
		try {		
			CoordinateReferenceSystem crs = Epsg3067CoordinateReferenceSystem.crs();
			MapContext mapContext = new DefaultMapContext(crs);
			return mapContext;
		} catch (Exception e) {
			throw new RuntimeException("Failed to build MapContext", e);
		}
	}

	/**
	 * Creates a SLD descriptor
	 * 
	 * @param resource
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	protected StyledLayerDescriptor createSLDStyle(String resource) {
		try {

			Configuration config = new SLDConfiguration();
			Parser parser = new Parser(config);
			InputStream xml = new ByteArrayInputStream(resource.getBytes());
			StyledLayerDescriptor sld = null;
			sld = (StyledLayerDescriptor) parser.parse(xml);
			xml.close();
			return sld; 
		} catch (Exception e) {
			throw new RuntimeException("Failed to create SLD Style", e);
		}
	}
	
	/**
  	 * Returns needed WFS layer
  	 */
  	protected WFSLayer findWFSLayer(FlowModel flowModel) {
  		/* TODO change to real implementation */
  		String id = String.valueOf(flowModel.get(FlowModel.FLOW_PM_WFS_LAYER_ID));  		
  		WFSLayer wfsLayer = getWfsLayerDbService().findWFSLayer(Integer.parseInt(id));
  		
  		if (wfsLayer == null) {
			throw new RuntimeException("Cannot find WFS layer with id '" + id + "'");
		}
  		
  		return wfsLayer;
  	}
}
