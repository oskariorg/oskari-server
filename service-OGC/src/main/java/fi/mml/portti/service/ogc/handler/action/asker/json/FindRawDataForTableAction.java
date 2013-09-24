package fi.mml.portti.service.ogc.handler.action.asker.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.json.JSONException;
import org.json.JSONObject;
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


/**
 * Checks that user has permissions to use Net Service Center
 *
 */
public class FindRawDataForTableAction extends BaseAskerAction implements OGCActionHandler {

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
					GetFeaturesWorker.PARSER_TYPE_JSON_OBJECT, 
					sft, 
					getMapViewBbox(ff, geomName, flowModel), 
					true);
			Future<WFSResponseCapsule> future = WfsExecutorService.schedule(worker);
			
			futures.add(future);
			
			/* Attach feature type and future with future objects id */
			featureTypeMap.put(future.toString(), ft);
		}

		/* Build Json objects */
		JSONObject rootJson = flowModel.getRootJson();
		Map<String,Integer> headersWidth = new HashMap<String,Integer>();
		headersWidth.put("pnr:PaikanNimi", new Integer(300));

		/* collect results */    
		for (Future<WFSResponseCapsule> future: futures) {
			try {
				/* Get future result */
				
				WFSResponseCapsule capsule = future.get();
				
				FeatureType ft = featureTypeMap.get(future.toString());
				if (ft == null) {
					throw new RuntimeException("Error in logic, could not find FeatureType for future");
				}
				
				
				if(wfsLayer.getSelectedFeatureTypes().size()>1){
					rootJson.accumulate("featureDatas", capsule.getJsonObject());
				} else{
					rootJson.append("featureDatas", capsule.getJsonObject());
				}
			} catch (Exception e) {
				throw new OgcFlowException("Failed to process Feature", e);
			}
		}
		
		/* Do headers */
		try {
            List<String> allHeaders = new ArrayList<String>();
            final JSONObject rootHeaderJson = new JSONObject();
            rootHeaderJson.put("header", "Feature");
            rootHeaderJson.put("width", new Integer(100));
            rootHeaderJson.put("dataIndex", "feature");
			rootJson.accumulate("headers", rootHeaderJson);
            for(String headerName : allHeaders) {
                final JSONObject headerJson = new JSONObject();
				headerJson.put("header", headerName);
				if (headersWidth.containsKey(headerName)) {
					headerJson.put("width", headersWidth.get(headerName));
				} else {
					headerJson.put("width", new Integer(100));
				}
				headerJson.put("dataIndex", headerName.toLowerCase().replaceAll("\\s", "_").replaceAll(":", "_"));
				rootJson.accumulate("headers", headerJson);
			}
		} catch (JSONException e) {
			throw new OgcFlowException("Failed to create JSON for table", e);
		}
		
		/** Add wfs query id  */		
		try {
			String wfsQueryId = (String)flowModel.get(FlowModel.FLOW_PM_MAP_WFS_QUERY_ID);
			rootJson.put("wfsQueryId", wfsQueryId);
		} catch (JSONException e) {
			throw new OgcFlowException("Failed to add WFS query id", e);
		}
	}
}
