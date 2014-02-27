package fi.mml.portti.service.ogc.handler.action.security;


import java.util.List;

import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.mml.portti.service.ogc.handler.FlowModel;
import fi.mml.portti.service.ogc.handler.OGCActionHandler;
import fi.nls.oskari.domain.User;


/**
 * Checks that user has permissions to use Net Service Center
 *
 */
public class CheckNetServiceCenterPermissionAction implements OGCActionHandler {
	
	/** Application String that should be found */
	private static final String APPLICATION_NAME = "+" + Permissions.APPLICATION_NET_SERVICE_CENTER;
	
	/** Permissions Service */
	private PermissionsService permissionsService = new PermissionsServiceIbatisImpl();
	
	public void handleAction(FlowModel flowModel) {
		List<String> executePermissions = findExecutePermissions(flowModel.getUser());
		for (String s: executePermissions) {
			if (s.equals(APPLICATION_NAME)) {
				return;
			}
		}
		
		throw new RuntimeException("User '" + flowModel.getUser().getEmail() + "' is trying to access feature " +
				"that requires Net Service Center execute privileges, but he does not have required permissions.");
	}
	
	protected List<String> findExecutePermissions(User user) {
		return permissionsService.getResourcesWithGrantedPermissions(Permissions.RESOURCE_TYPE_APPLICATION, user, Permissions.PERMISSION_TYPE_EXECUTE);		
	}
	
}
