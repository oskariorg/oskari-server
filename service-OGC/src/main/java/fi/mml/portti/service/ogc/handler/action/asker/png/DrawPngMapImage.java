package fi.mml.portti.service.ogc.handler.action.asker.png;

import fi.mml.portti.service.ogc.OgcFlowException;
import fi.mml.portti.service.ogc.executor.GetFeaturesWorker;
import fi.mml.portti.service.ogc.executor.WFSResponseCapsule;
import fi.mml.portti.service.ogc.executor.WfsExecutorService;
import fi.mml.portti.service.ogc.handler.FlowModel;
import fi.mml.portti.service.ogc.handler.OGCActionHandler;
import fi.mml.portti.service.ogc.handler.action.asker.BaseAskerAction;
import fi.nls.oskari.domain.map.wfs.FeatureType;
import fi.nls.oskari.domain.map.wfs.SelectedFeatureType;
import fi.nls.oskari.domain.map.wfs.WFSLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.visualization.SLDStore;
import fi.nls.oskari.util.PropertyUtil;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContext;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class DrawPngMapImage extends BaseAskerAction implements OGCActionHandler {
	
    
    private static final String BBOX = "BBOX";
    
	/** Logger */
	private static Logger log = LogFactory.getLogger(DrawPngMapImage.class);

    private static final String locale = PropertyUtil.getDefaultLanguage();

	public void handleAction(final FlowModel flowModel) throws OgcFlowException {

		WFSLayer wfsLayer = findWFSLayer(flowModel);
		log.debug("Drawing image for wfs layer '" + wfsLayer.getName(locale) + "'");
		List<Future<WFSResponseCapsule>> futures = new ArrayList<Future<WFSResponseCapsule>>();
		
		/* Create workers */
		log.debug("We have " + wfsLayer.getSelectedFeatureTypes().size() + " selected feature types");
		for (SelectedFeatureType sft : wfsLayer.getSelectedFeatureTypes()) {
			FeatureType ft = sft.getFeatureType();
			log.debug("Processing featuretype '" + ft.getTitle(locale) + "'...");
			
			/* Create filter */
			String geomName = ft.getBboxParameterName();
			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
	        
			GetFeaturesWorker worker = new GetFeaturesWorker(sft, getExtendedMapViewBbox(ff, geomName, flowModel), false);
			Future<WFSResponseCapsule> future = WfsExecutorService.schedule(worker);
			futures.add(future);
		}

		/* collect results */    
		FeatureCollection<SimpleFeatureType, SimpleFeature> features = WfsExecutorService.collectFeaturesFromFutures(futures);

		Double bboxMaxX;
		Double bboxMinX;
		Double bboxMaxY;
        Double bboxMinY;
        
        if (!flowModel.isEmpty(BBOX)) {
            String[] bboxSplit = flowModel.getAsString(BBOX).split(","); 
            bboxMinX = Double.parseDouble(bboxSplit[0]); 
            bboxMinY = Double.parseDouble(bboxSplit[1]);  
            bboxMaxX = Double.parseDouble(bboxSplit[2]); 
            bboxMaxY = Double.parseDouble(bboxSplit[3]);
        } else {
            bboxMaxX = Double.parseDouble(flowModel.getAsString(FlowModel.FLOW_PM_BBOX_MAX_X)); 
            bboxMinX = Double.parseDouble(flowModel.getAsString(FlowModel.FLOW_PM_BBOX_MIN_X));  
            bboxMaxY = Double.parseDouble(flowModel.getAsString(FlowModel.FLOW_PM_BBOX_MAX_Y)); 
            bboxMinY = Double.parseDouble(flowModel.getAsString(FlowModel.FLOW_PM_BBOX_MIN_Y));
        }
		
        
		MapContext mapContext = buildMapContext();
		ReferencedEnvelope bounds = new ReferencedEnvelope(bboxMaxX,bboxMinX, bboxMaxY, bboxMinY, 
				mapContext.getCoordinateReferenceSystem());

		/* Add all found features */
		if (features != null && features.size() > 0) {
			log.debug("Parsing found " + features.size() + " features.");
			String styleString = wfsLayer.getStyle();
			
			// TODO: sld styles should be stored in the database
			if (styleString == null || "".equals(styleString)) {
				styleString = SLDStore.getSLD(wfsLayer.getWmsName()); //MapLayerServiceNoDbImpl.getSldStyle(wfsLayer.getWmsName());
			}
			
			Style style = SLD.styles(createSLDStyle(styleString))[0];
			mapContext.addLayer(features, style);
		} else {
			log.debug("Parsing found no features.");
		}

		BufferedImage image = renderImageFromFeatures(flowModel, mapContext, bounds);
		/*image.getGraphics().setColor(new Color(1f, 0, 0));
		image.getGraphics().drawRect(1, 1, image.getWidth()-1, image.getHeight()-1);*/
		flowModel.put("image", image);
		
	}
}
