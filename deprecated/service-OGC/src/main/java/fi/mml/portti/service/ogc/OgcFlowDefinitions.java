package fi.mml.portti.service.ogc;

import java.util.HashMap;
import java.util.Map;

/**
 * Definitions for our flows
 *
 */
public class OgcFlowDefinitions {
 
	/* Net Service Center WFS Service wizard steps */
	public static final String NSC_WFS_SERVICE_WIZARD_FIND_SERVICES_FROM_GEONETWORK = "NSC_WFS_SERVICE_WIZARD_FIND_SERVICES_FROM_GEONETWORK";
	public static final String NSC_WFS_SERVICE_WIZARD_TEST_BASIC_AUTHENTICATION_USERNAME_AND_PASSWORD  = "NSC_WFS_SERVICE_WIZARD_TEST_BASIC_AUTHENTICATION_USERNAME_AND_PASSWORD";
	public static final String NSC_WFS_SERVICE_WIZARD_LIST_FEATURE_TYPES = "NSC_WFS_SERVICE_WIZARD_LIST_FEATURE_TYPES";
	public static final String NSC_WFS_SERVICE_WIZARD_LIST_SERVICES = "NSC_WFS_SERVICE_WIZARD_LIST_SERVICES";
	public static final String NSC_WFS_SERVICE_WIZARD_STORE_SERVICE = "NSC_WFS_SERVICE_WIZARD_STORE_SERVICE";
	public static final String NSC_WFS_SERVICE_WIZARD_STORE_AUTHENTICATION_INFORMATION = "NSC_WFS_SERVICE_WIZARD_STORE_AUTHENTICATION_INFORMATION";
	public static final String NSC_WFS_SERVICE_WIZARD_REMOVE_SERVICE = "NSC_WFS_SERVICE_WIZARD_REMOVE_SERVICE";
	public static final String NSC_WFS_SERVICE_WIZARD_SAVE_SERVICE = "NSC_WFS_SERVICE_WIZARD_SAVE_SERVICE";
	public static final String GET_STORED_AUTHENTICATION ="GET_STORED_AUTHENTICATION";
	
	/* JSON */
	public static final String QUERY_FIND_RAW_DATA_TO_TABLE = "QUERY_FIND_RAW_DATA_TO_TABLE";
	
	/* PNG */
	public static final String GET_PNG_MAP ="GET_PNG_MAP";
	public static final String GET_HIGHLIGHT_WFS_FEATURE_IMAGE = "GET_HIGHLIGHT_WFS_FEATURE_IMAGE";
	public static final String GET_HIGHLIGHT_WFS_FEATURE_IMAGE_BY_POINT = "GET_HIGHLIGHT_WFS_FEATURE_IMAGE_BY_POINT";

	/* XML */
	public static final String GET_XML_DATA ="GET_XML_DATA";
	
	
	public static Map<String, String> getFlowDefinitions() {
		Map<String, String> definitions = new HashMap<String, String>();
				
		definitions.put(NSC_WFS_SERVICE_WIZARD_TEST_BASIC_AUTHENTICATION_USERNAME_AND_PASSWORD, 
				"fi.mml.portti.service.ogc.handler.action.wfsservicewizard.BasicAuthenticationTestAction");
		//secureFlow(definitions, NSC_WFS_SERVICE_WIZARD_TEST_BASIC_AUTHENTICATION_USERNAME_AND_PASSWORD);
				
		definitions.put(NSC_WFS_SERVICE_WIZARD_LIST_FEATURE_TYPES, 
		"fi.mml.portti.service.ogc.handler.action.wfsservicewizard.FeatureTypeListAction");
		//secureFlow(definitions, NSC_WFS_SERVICE_WIZARD_LIST_FEATURE_TYPES);
		
		definitions.put(NSC_WFS_SERVICE_WIZARD_LIST_SERVICES, 
		"fi.mml.portti.service.ogc.handler.action.wfsservicewizard.ListSavedServicesAction");
		//secureFlow(definitions, NSC_WFS_SERVICE_WIZARD_LIST_FEATURE_TYPES);
				
		definitions.put(NSC_WFS_SERVICE_WIZARD_STORE_SERVICE, 
		"fi.mml.portti.service.ogc.handler.action.wfsservicewizard.WfsServiceStoreAction");
		//secureFlow(definitions, NSC_WFS_SERVICE_WIZARD_LIST_FEATURE_TYPES);
		
		definitions.put(NSC_WFS_SERVICE_WIZARD_SAVE_SERVICE, 
		"fi.mml.portti.service.ogc.handler.action.wfsservicewizard.SaveWFSServiceAction");
		//secureFlow(definitions, NSC_WFS_SERVICE_WIZARD_LIST_FEATURE_TYPES);
				
		definitions.put(NSC_WFS_SERVICE_WIZARD_STORE_AUTHENTICATION_INFORMATION, 
		"fi.mml.portti.service.ogc.handler.action.wfsservicewizard.AuthenticationInformationStoreAction");
		//secureFlow(definitions, NSC_WFS_SERVICE_WIZARD_LIST_FEATURE_TYPES);
		
		definitions.put(NSC_WFS_SERVICE_WIZARD_REMOVE_SERVICE, 
		"fi.mml.portti.service.ogc.handler.action.wfsservicewizard.RemoveWFSServiceAction");
		//secureFlow(definitions, NSC_WFS_SERVICE_WIZARD_LIST_FEATURE_TYPES);
		
		definitions.put(GET_STORED_AUTHENTICATION, 
		"fi.mml.portti.service.ogc.handler.action.wfsservicewizard.GetStoredAuthenticationAction");
		//secureFlow(definitions, NSC_WFS_SERVICE_WIZARD_LIST_FEATURE_TYPES);
		
		/* Map asker flows */
		
		definitions.put(QUERY_FIND_RAW_DATA_TO_TABLE, 
		"fi.mml.portti.service.ogc.handler.action.asker.json.FindRawDataForTableAction");
		secureFlowWithWfsLayerPermissions(definitions, QUERY_FIND_RAW_DATA_TO_TABLE);
		
		definitions.put(GET_XML_DATA,
		"fi.mml.portti.service.ogc.handler.action.asker.xml.GetXmlData");
		secureFlowWithWfsLayerPermissions(definitions, GET_XML_DATA);
		
		definitions.put(GET_PNG_MAP, 
		"fi.mml.portti.service.ogc.handler.action.asker.png.DrawPngMapImage");
		secureFlowWithWfsLayerPermissions(definitions, GET_PNG_MAP);

		definitions.put(GET_HIGHLIGHT_WFS_FEATURE_IMAGE, 
		"fi.mml.portti.service.ogc.handler.action.asker.png.DrawHighlightWFSFeatureImage");
		secureFlowWithWfsLayerPermissions(definitions, GET_HIGHLIGHT_WFS_FEATURE_IMAGE);
		
		definitions.put(GET_HIGHLIGHT_WFS_FEATURE_IMAGE_BY_POINT, 
		"fi.mml.portti.service.ogc.handler.action.asker.png.DrawHighlightWFSFeatureImageByGeoPoint");
		secureFlowWithWfsLayerPermissions(definitions, GET_HIGHLIGHT_WFS_FEATURE_IMAGE_BY_POINT);
		
		return definitions;
	}
	
	/**
	 * Adds security handler
	 * 
	 * @param definitions
	 * @param key
	 */
	private static void secureFlow(Map<String, String> definitions, String key) {
		String flow = definitions.get(key);
		flow = "fi.mml.portti.service.ogc.handler.action.security.CheckNetServiceCenterPermissionAction," + flow;
		definitions.put(key, flow);
	}
	
	private static void secureFlowWithWfsLayerPermissions(Map<String, String> definitions, String key) {
		String flow = definitions.get(key);
		flow = "fi.mml.portti.service.ogc.handler.action.security.CheckWFSLayerPermissionsAction," + flow;
		definitions.put(key, flow);
	}
	
	public static String findFlow(String actionKey) {
		String flow = getFlowDefinitions().get(actionKey);
		if (flow == null) {
			throw new RuntimeException("There is no flow with name '" + actionKey + "'. Check that you have added action class " +
					"to fi.mml.portti.service.ogc.OgcFlowDefinitions and that FlowKey values match with those that " +
					"are sent from the client.");
		}
		
		return flow;
	}
}
