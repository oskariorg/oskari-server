package fi.mml.portti.service.ogc.handler.action.wfsservicewizard;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDContentTypeCategory;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDEnumerationFacet;
import org.eclipse.xsd.XSDFacet;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDPatternFacet;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDSimpleTypeDefinition;
import org.eclipse.xsd.XSDTerm;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.XSDWildcard;
import org.eclipse.xsd.XSDParticle.DFA.Transition;
import org.eclipse.xsd.util.XSDResourceImpl;

import org.geotools.data.DataStoreFinder;

import fi.mml.portti.domain.ogc.util.http.EasyHttpClient;
import fi.mml.portti.domain.ogc.util.http.HttpPostResponse;
import fi.mml.portti.service.ogc.OgcFlowException;
import fi.mml.portti.service.ogc.handler.FlowModel;
import fi.mml.portti.service.ogc.handler.OGCActionHandler;
import fi.nls.oskari.domain.map.wfs.FeatureParameter;
import fi.nls.oskari.domain.map.wfs.FeatureType;
import fi.nls.oskari.domain.map.wfs.WFSService;

import flexjson.JSONSerializer;

public class FeatureTypeListAction implements OGCActionHandler {

	public void handleAction(FlowModel flowModel) throws OgcFlowException {
		
		try {
			WFSService service = (WFSService) flowModel.get(FlowModel.WFS_SERVICE);
			String getCapabilities = service.getUrl() + "?request=GetCapabilities&version=1.1.0&service=WFS";
			
			Map<String,Object> connectionParameters = new HashMap<String,Object>();
	
	        connectionParameters.put(
	                         "WFSDataStoreFactory:GET_CAPABILITIES_URL",
	                         getCapabilities);
	        connectionParameters.put("WFSDataStoreFactory:PROTOCOL",
	                         Boolean.TRUE);
	        connectionParameters.put("WFSDataStoreFactory:TIMEOUT",
	                         new Integer(30000));
	
	         
	        if (!"".equals(service.getPassword()) && !"".equals(service.getUsername())) {
		        connectionParameters.put("WFSDataStoreFactory:USERNAME",service.getUsername());
		        connectionParameters.put("WFSDataStoreFactory:PASSWORD",service.getPassword());
	        }
	        
	        
	         // Step 2 - connection
	        org.geotools.data.wfs.WFSDataStore data = (org.geotools.data.wfs.WFSDataStore) DataStoreFinder
	        .getDataStore(connectionParameters);
	         
	       
	    	String[] typeNames = data.getTypeNames();
	    	String hostUrl = data.getInfo().getSource().parseServerAuthority().toString();
	    	
	    	List<FeatureType> featureTypes = new ArrayList<FeatureType>();
	    	
	    	for (String type: typeNames) {
	    		
	    		FeatureType ft = new FeatureType();
	    		QName qname = new QName(type.split(":")[0],type.split(":")[1]);
	    		ft.setQname(qname);
	    		ft.setFeatureParameters(geoFeatureType(type, hostUrl, service));
	    		featureTypes.add(ft);
	    		
	    	}
	   
	    	
	    	if (flowModel.isEmpty(FlowModel.WFS_SERVICE)) {
	    		WFSService wfsService = new WFSService();
	    		flowModel.put(FlowModel.WFS_SERVICE, wfsService);
	    	}
	    	 
	    	((WFSService)flowModel.get(FlowModel.WFS_SERVICE)).setFeatureTypes(featureTypes);
	    	flowModel.putValueToRootJson("featureTypes", new JSONSerializer()
	    		.include("featureParameters")
	    		.exclude("filterBboxFeatureParameter")
	    		.serialize(featureTypes));
	    	
	    	
		} catch(Exception e) {
			throw new OgcFlowException("Ongelma parsinnassa.", e);
		}
		
	}
	
	public List geoFeatureType(String type, String hostUrl, WFSService service) throws Exception {

		//String hostUrl = "http://ktjkiiwfs.nls.fi/ktjkii/wfs/wfs";
        String queryPart = "SERVICE=WFS&VERSION=1.1.0&REQUEST=DescribeFeatureType&TYPENAME="+type+
        "&NAMESPACE=xmlns(ktjkiiwfs=http://xml.nls.fi/ktjkiiwfs/2010/02)";
        URL url = new URL(hostUrl + queryPart);
        
        XSDSchema schema;
        
        if (!"".equals(service.getPassword()) && !"".equals(service.getUsername())) { 
        	schema = parse(url.toString(),service.getUsername(),service.getPassword());
        } else {
        	schema = org.geotools.xml.Schemas.parse(url.toString());
        }
        
        
        String featNs = schema.getTargetNamespace();
        
        XSDElementDeclaration feature = schema.resolveElementDeclaration(featNs, type.substring(type.indexOf(":")+1));
        
        return handle(feature);
        
	}

	private List handle(XSDElementDeclaration xsdElementDeclaration) throws Exception {
		List<FeatureParameter> featureParameters = new ArrayList<FeatureParameter>();	
		List<String> abstractParametersList = Arrays.asList(FlowModel.ABSTRACT_PARAMETER_TYPES);
		XSDTypeDefinition xsdTypeDefinition = xsdElementDeclaration.getTypeDefinition();
		
		if (xsdTypeDefinition instanceof XSDSimpleTypeDefinition) {
			XSDSimpleTypeDefinition xsdSimpleTypeDefinition = (XSDSimpleTypeDefinition)xsdTypeDefinition;
		   
			for (Iterator i = xsdSimpleTypeDefinition.getFacets().iterator(); i.hasNext(); )  {
				XSDFacet xsdFacet = (XSDFacet)i.next();
				if (xsdFacet instanceof XSDEnumerationFacet) {
					for (Iterator j = ((XSDEnumerationFacet)xsdFacet).getValue().iterator(); j.hasNext(); ) {
						Object enumerator = j.next();
						 System.out.println("enumerator:"+enumerator);
					}
				}
				else if (xsdFacet instanceof XSDPatternFacet) {
				}
		     // ...
			}
		}
		else {
			XSDComplexTypeDefinition xsdComplexTypeDefinition = (XSDComplexTypeDefinition)xsdTypeDefinition;
			
			switch (xsdComplexTypeDefinition.getContentTypeCategory().getValue()) {
				case XSDContentTypeCategory.EMPTY: {
					break;
				}
			    case XSDContentTypeCategory.SIMPLE: {
			    	XSDSimpleTypeDefinition xsdSimpleTypeDefinition = (XSDSimpleTypeDefinition)xsdComplexTypeDefinition.getContentType();
			        break;
			    }
			    case XSDContentTypeCategory.ELEMENT_ONLY:
			    case XSDContentTypeCategory.MIXED: {
			    	XSDParticle xsdParticle = (XSDParticle)xsdComplexTypeDefinition.getContentType();
			    	XSDTerm xsdTerm = xsdParticle.getTerm();
			    	
			    	if (xsdTerm instanceof XSDModelGroup) {
			    		XSDModelGroup xsdModelGroup = (XSDModelGroup)xsdTerm;
			    		List<String> addedNames =  new ArrayList<String>();
			    		for (Iterator i = xsdModelGroup.getParticles().iterator(); i.hasNext(); ) {
			    			XSDParticle childXSDParticle = (XSDParticle)i.next();
			    			
			    			List<XSDParticle.DFA.State> states = childXSDParticle.getDFA().getStates();
			    			
			    			for  (int ii = 0; ii < states.size(); ii++ ) {//(XSDParticle.DFA dfa : states) {
			    			    
			    				Object dfa = states.get(ii);
			    				if (dfa instanceof XSDParticle.DFA.State){
				    			
				    				XSDParticle.DFA.State transition= (XSDParticle.DFA.State)dfa;// childXSDParticle.getDFA().getStates().get(0);
				    				/*
				    				if (transition.getTransitions().size() > 0) {
					    				XSDParticle.DFA.Transition sdt = (Transition) transition.getTransitions().get(0);
					    				
					    				if (sdt.getParticle() != null && sdt.getParticle().getContent() != null && 
					    						sdt.getParticle().getContent().getElement() != null && !"".equals(sdt.getParticle().getContent().getElement().getAttribute("name"))) {
					    					
					    					if (!addedNames.contains(sdt.getParticle().getContent().getElement().getAttribute("name"))) {
						    					FeatureParameter fp = new FeatureParameter();
								    			fp.setDescription(sdt.getParticle().getContent().getElement().getAttribute("name"));
								    			addedNames.add(sdt.getParticle().getContent().getElement().getAttribute("name"));
								    			featureParameters.add(fp);
					    					}
							    			
					    				}
				    				}*/
				    				
				    				List<Transition> transitionList  =  transition.getTransitions();//.get(0);
				    				
				    				for (XSDParticle.DFA.Transition sdt : transitionList) {
				    					if (sdt.getParticle() != null && sdt.getParticle().getContent() != null && 
					    						sdt.getParticle().getContent().getElement() != null && !"".equals(sdt.getParticle().getContent().getElement().getAttribute("name"))) {
					    					if (!addedNames.contains(sdt.getParticle().getContent().getElement().getAttribute("name"))) {
						    					FeatureParameter fp = new FeatureParameter();
								    			fp.setDescription(sdt.getParticle().getContent().getElement().getAttribute("name"));
								    			fp.setXmlType(sdt.getParticle().getContent().getElement().getAttribute("type"));
								    			
								    			addedNames.add(sdt.getParticle().getContent().getElement().getAttribute("name"));
								    			
								    			if (abstractParametersList.contains(sdt.getParticle().getContent().getElement().getAttribute("type"))) {
								    				fp.setAbstractSchemaType(true);
								    			}
								    			
								    			featureParameters.add(fp);
					    					}
							    			
					    				}
				    				}
				    			}
			    			}
			    		}
			       }
			       else if (xsdTerm instanceof XSDElementDeclaration) {
			    	   System.out.println("XSDElementDeclaration");
			       }
			       else if (xsdTerm instanceof XSDWildcard) {
			    	   
			    	   System.out.println("XSDWildcard");
			       }
			       break;
			    }
			}
		}
		return featureParameters;
	}
	
	public XSDSchema parse(String location, String userName, String password) throws IOException {
		
		final ResourceSet resourceSet = new ResourceSetImpl();
	    XSDResourceImpl xsdMainResource = (XSDResourceImpl) resourceSet.createResource(URI.createURI(".xsd"));
	    // TODO proxy settings must be read from wizard
	    HttpPostResponse pr = EasyHttpClient.post(location, userName, password, null, false);
	    try {
	    	xsdMainResource.load(pr.getResponseAsInputStream(),resourceSet.getLoadOptions());
	    	return xsdMainResource.getSchema();
	    } finally {
	    	pr.closeResponseStream();
	    }
	}
	
}
