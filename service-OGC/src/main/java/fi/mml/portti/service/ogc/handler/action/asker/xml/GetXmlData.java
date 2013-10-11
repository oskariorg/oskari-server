package fi.mml.portti.service.ogc.handler.action.asker.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.opengis.filter.FilterFactory2;

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

public class GetXmlData extends BaseAskerAction implements OGCActionHandler {

	@Override
	public void handleAction(FlowModel flowModel) throws OgcFlowException {
		
		WFSLayer wfsLayer = findWFSLayer(flowModel);

		List<Future<WFSResponseCapsule>> futures = new ArrayList<Future<WFSResponseCapsule>>();
		Map<String, FeatureType> featureTypeMap = new HashMap<String, FeatureType>();
		
		/* Create workers */
		for (SelectedFeatureType sft : wfsLayer.getSelectedFeatureTypes()) {
			FeatureType ft = sft.getFeatureType();

			/* Create filter */
			String geomName = ft.getBboxParameterName();
			
			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());

			GetFeaturesWorker worker = new GetFeaturesWorker(
					GetFeaturesWorker.PARSER_TYPE_XML, 
					sft, 
					getMapViewBbox(ff, geomName, flowModel), 
					true);
			Future<WFSResponseCapsule> future = WfsExecutorService.schedule(worker);
			
			futures.add(future);
			
			/* Attach feature type and future with future objects id */
			featureTypeMap.put(future.toString(), ft);
		}

		/* collect results */    
		for (Future<WFSResponseCapsule> future: futures) {
			try {
				/* Get future result */
				
				WFSResponseCapsule capsule = future.get();
				
				FeatureType ft = featureTypeMap.get(future.toString());
				if (ft == null) {
					throw new RuntimeException("Error in logic, could not find FeatureType for future");
				}
				
				//rootJson.put("xmlData",capsule.getXmlData().toString());
				//flowModel.setXmlData(capsule.getXmlData());
				flowModel.put("xmlData",capsule.getXmlData());
			} catch (Exception e) {
				throw new OgcFlowException("Failed to process Feature", e);
			}
		}
		
	}

}
