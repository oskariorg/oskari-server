package fi.mml.portti.service.ogc.handler.action.asker.png;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

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

public class DrawHighlightWFSFeatureImageByGeoPoint extends BaseAskerAction implements
		OGCActionHandler {

	/*
	final static double[] levelModifiers = {
		2048*4,2048*2, 2048, 1024, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1 ,0.5   	
	};
*/	
	
	public void handleAction(FlowModel flowModel) throws OgcFlowException {

		WFSLayer wfsLayer = findWFSLayer(flowModel);
		
		int zoomLevel = Integer.parseInt(flowModel.getAsString("flow_pm_zoom_level"));
		
		String[] tmpPointsX  = flowModel.getAsString("flow_pm_point_x").split(",");
		String[] tmpPointsY  = flowModel.getAsString("flow_pm_point_y").split(",");
		
		List<Future<WFSResponseCapsule>> futures = new ArrayList<Future<WFSResponseCapsule>>();

		for (int i = 0; i < tmpPointsX.length; i++) {
			for (SelectedFeatureType sft: wfsLayer.getSelectedFeatureTypes()) {
				FeatureType ft = sft.getFeatureType();
				
				/* Create filter */
				String geomName = ft.getBboxParameterName();
				FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
				GeometryFactory gf = JTSFactoryFinder.getGeometryFactory (GeoTools.getDefaultHints() );
				
				Coordinate coord = new Coordinate ( Double.parseDouble(tmpPointsX[i]), Double.parseDouble(tmpPointsY[i]));
				
				Point point = gf.createPoint(coord);
				
				Filter filter = ff.dwithin(ff.property(geomName), ff.literal(point), Math.pow(2, (12-zoomLevel)), "m");
				
		        GetFeaturesWorker worker = new GetFeaturesWorker(sft, filter, true);
				Future<WFSResponseCapsule> future = WfsExecutorService.schedule(worker);
				futures.add(future);
			}
		}
		
		/* collect results */    
		FeatureCollection<SimpleFeatureType, SimpleFeature> features = WfsExecutorService.collectFeaturesFromFutures(futures);
		
		FeatureIterator<SimpleFeature> simpleFeatures = features.features();
		JSONObject featureIdsJSON = new JSONObject();
		boolean singleFeature = false;
		while (simpleFeatures.hasNext()) {
			Feature feature = simpleFeatures.next();
			if (!singleFeature && simpleFeatures.hasNext()) {
				singleFeature = true;
			}
			
			try {
				if (!singleFeature) {
					featureIdsJSON.append("id", feature.getIdentifier().getID());
				} else {
					featureIdsJSON.accumulate("id", feature.getIdentifier().getID());
				}
			} catch (JSONException e) {
				throw new RuntimeException("Failed to do JSON", e);
			}
		}
		flowModel.putValueToRootJson("selectedFeatures", featureIdsJSON.toString());	
	}
}
