package fi.mml.portti.service.ogc.handler.action.asker.png;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContext;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.identity.FeatureId;

import fi.mml.portti.domain.ogc.util.OskariBBOX;
import fi.mml.portti.service.ogc.OgcFlowException;
import fi.mml.portti.service.ogc.executor.GetFeaturesWorker;
import fi.mml.portti.service.ogc.executor.WFSResponseCapsule;
import fi.mml.portti.service.ogc.executor.WfsExecutorService;
import fi.mml.portti.service.ogc.handler.FlowModel;
import fi.mml.portti.service.ogc.handler.OGCActionHandler;
import fi.mml.portti.service.ogc.handler.action.asker.BaseAskerAction;
import fi.nls.oskari.domain.map.wfs.SelectedFeatureType;
import fi.nls.oskari.domain.map.wfs.WFSLayer;

public class DrawHighlightWFSFeatureImage extends BaseAskerAction implements OGCActionHandler {

	@Override
	public void handleAction(FlowModel flowModel) throws OgcFlowException {

		WFSLayer wfsLayer = findWFSLayer(flowModel);
		FilterFactory ff = CommonFactoryFinder.getFilterFactory(GeoTools.getDefaultHints());
				
		List<Future<WFSResponseCapsule>> futures = new ArrayList<Future<WFSResponseCapsule>>();
		String[] featureIdsWithQnames = flowModel.getAsString("wfsFeatureId").split(",");

		/* Create workers */
		for (SelectedFeatureType sft : wfsLayer.getSelectedFeatureTypes()) {
			Set<FeatureId> fids = findFeatureIdsForQname(ff, sft.getFeatureType().getQname().toString(), featureIdsWithQnames);
			if (fids.size() == 0) {
				continue;
			}
			
			Filter filter = ff.id( fids );
			GetFeaturesWorker worker = new GetFeaturesWorker(sft, filter, true);
			Future<WFSResponseCapsule> future = WfsExecutorService.schedule(worker);
			futures.add(future);
		}

		if (futures.size() == 0) {
			return;
		}
		
		/* collect results */    
		FeatureCollection<SimpleFeatureType, SimpleFeature> features = WfsExecutorService.collectFeaturesFromFutures(futures);

		OskariBBOX oBox = getOskariBBOX(flowModel);
		
		MapContext mapContext = buildMapContext();
		
		ReferencedEnvelope bounds = new ReferencedEnvelope(
				oBox.getBboxMaxX(),oBox.getBboxMinX(), oBox.getBboxMaxY(), oBox.getBboxMinY(),
				mapContext.getCoordinateReferenceSystem());
		
		
		/*
		ReferencedEnvelope bounds = new ReferencedEnvelope(
				Double.parseDouble(flowModel.getAsString("flow_pm_bbox_max_x")), 
				Double.parseDouble(flowModel.getAsString("flow_pm_bbox_min_x")), 
				Double.parseDouble(flowModel.getAsString("flow_pm_bbox_max_y")), 
				Double.parseDouble(flowModel.getAsString("flow_pm_bbox_min_y")), 
				mapContext.getCoordinateReferenceSystem());
		*/
		/* Add all found features */
		if (features != null && features.size() > 0) {
		    
		    String highLightStyleTmp = highLightStyle;
            if (wfsLayer.getSelection_style() != null && !"".equals(wfsLayer.getSelection_style())) {
                highLightStyleTmp = wfsLayer.getSelection_style();
            }
		    
            Style style = SLD.styles(createSLDStyle(highLightStyleTmp))[0];
            mapContext.addLayer(features, style);
		}    

		BufferedImage image = renderImageFromFeatures(flowModel, mapContext, bounds); 
		flowModel.put("image", image);	
	}
	
	protected OskariBBOX getOskariBBOX(FlowModel flowModel) {
		
		OskariBBOX oskariBbox = new OskariBBOX();
		
		if (!flowModel.isEmpty("BBOX")) {
			String[] bbox = flowModel.getAsString("BBOX").split(",");
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
	 * Returns featureids for requested qname
	 * 
	 * @param ff
	 * @param requestedQname
	 * @param featureIdsWithQnames
	 * @return
	 */
	private Set<FeatureId> findFeatureIdsForQname(FilterFactory ff, String requestedQname, String[] featureIdsWithQnames) {
		Set<FeatureId> result = new HashSet<FeatureId>();
		for(String featureIdWithQname: featureIdsWithQnames) {
			String[] splitted = featureIdWithQname.split(":::");
			if (splitted.length == 2) {
				String featureId = splitted[0];
				String qname = splitted[1];
				if (qname.equals(requestedQname)) {
					result.add(ff.featureId(featureId));
				} 
			} else {
				result.add(ff.featureId(featureIdWithQname));
			}
		}
		return result;
	}


	private String highLightStyle =
		"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
		"<StyledLayerDescriptor version=\"1.0.0\" xmlns=\"http://www.opengis.net/sld\" xmlns:ogc=\"http://www.opengis.net/ogc\"\n"+
		" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"+
		" xsi:schemaLocation=\"http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd\">\n"+
		"    <NamedLayer>\n"+
		"    <Name>NimisSijaintitiedot</Name>\n"+
		"    <UserStyle>\n"+
		"      <Title>Default Nimiston style</Title>\n"+
		"      <Abstract></Abstract>\n"+
		"      <FeatureTypeStyle>\n"+
		"      <Rule>\n"+
		" 			<Title>Point</Title>\n"+
		"           <PointSymbolizer>\n"+
		"         		<Graphic>\n"+
		"           <Mark>\n"+
		"	             <WellKnownName>circle</WellKnownName>\n"+
		"	             <Fill>\n"+
		"	               <CssParameter name=\"fill\">#f5af3c</CssParameter>\n"+
		"	             </Fill>\n"+
		"				 <Stroke> \n" +
		"					<CssParameter name=\"stroke\">#000000</CssParameter>\n" +
		"					<CssParameter name=\"stroke-width\">1</CssParameter>\n" +
		"				 </Stroke>\n" +
		"	           </Mark>\n"+
		"	           <Size>15</Size>\n"+
		"         	</Graphic>\n"+
		"       	</PointSymbolizer>        \n"+
		"        </Rule>\n"+
		"     <Rule>\n" +
		"       <Title>Line</Title>\n" +
		"		<PolygonSymbolizer>\n" +
		"			<Fill> \n" +
		"				<CssParameter name=\"fill\">#f5af3c</CssParameter>\n" +
		"			</Fill>" +
		"       	<Stroke>\n" +
		"       		<CssParameter name=\"stroke\">#000000</CssParameter>\n" +
		"           	<CssParameter name=\"stroke-width\">1</CssParameter>\n" +
		"      		</Stroke>\n" +
		"      </PolygonSymbolizer> \n" +
		" 	   </Rule>\n" +
		"     <Rule>\n" +
		"       <Title>Line</Title>\n" +
		"		<LineSymbolizer>\n" +
		"       	<Stroke>\n" +
		"       		<CssParameter name=\"stroke\">#000000</CssParameter>\n" +
		"           	<CssParameter name=\"stroke-width\">5</CssParameter>\n" +
		"				<CssParameter name=\"stroke-linecap\">round</CssParameter> \n"+
		"      		</Stroke>\n" +
		"      </LineSymbolizer> \n" +
		"	  </Rule>\n" +
		"      </FeatureTypeStyle>\n"+
		"      <FeatureTypeStyle>\n"+
		"        <Rule>\n"+
		"		   <LineSymbolizer>\n" +
		"       	<Stroke>\n" +
		"       		<CssParameter name=\"stroke\">#f5af3c</CssParameter>\n" +
		"           	<CssParameter name=\"stroke-width\">4</CssParameter>\n" +
		"				<CssParameter name=\"stroke-linecap\">round</CssParameter> \n"+
		"      		</Stroke>\n" +
		"          </LineSymbolizer> \n" +
		"	     </Rule>\n" +
		"      </FeatureTypeStyle>\n"+
		"    </UserStyle>\n"+
		"  </NamedLayer>\n"+
		"</StyledLayerDescriptor> \n";

}
