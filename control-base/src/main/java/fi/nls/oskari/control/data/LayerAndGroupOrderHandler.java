package fi.nls.oskari.control.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;

/**
 * CRUD for layer and group order handling. Methods require admin user.
 */
@OskariActionRoute("LayerAndGroupOrder")
public class LayerAndGroupOrderHandler extends RestActionHandler {
	private static Logger log = LogFactory.getLogger(LayerAndGroupOrderHandler.class);
	
    private static final String KEY_NODE_ID = "nodeId";
    private static final String KEY_NODE_INDEX = "nodeIndex";
    private static final String KEY_TARGET_GROUP_ID = "targetGroupId";
	
	private OskariMapLayerGroupService oskariMapLayerGroupService;

    public void setOskariMapLayerGroupService(final OskariMapLayerGroupService service) {
        oskariMapLayerGroupService = service;
    }

    @Override
    public void init() {
        // setup service if it hasn't been initialized
        if(oskariMapLayerGroupService == null) {
            setOskariMapLayerGroupService(new OskariMapLayerGroupServiceIbatisImpl());
        }
    }
    
    /**
     * Handles update
     * @param params
     * @throws ActionException
     */
    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        checkForAdminPermission(params);
        log.debug("Updating layer/group order");
        JSONObject orderJSON = getOrderJSON(params.getRequest());
        System.out.println(orderJSON);
//        final int id = params.getRequiredParamInt(PARAM_ID);
//        final MaplayerGroup theme = oskariMapLayerGroupService.find(id);
//        populateFromRequest(params, theme);
//        oskariMapLayerGroupService.update(theme);
//        ResponseHelper.writeResponse(params, theme.getAsJSON());
    }
    
    /**
     * Read JSON from request
     * @param req
     * @return
     * @throws ActionException
     */
    protected JSONObject getOrderJSON(HttpServletRequest req) throws ActionException {
        try (InputStream in = req.getInputStream()) {
            final byte[] json = IOHelper.readBytes(in);
            final String jsonString = new String(json, StandardCharsets.UTF_8);
            final JSONObject orderJSON = new JSONObject(jsonString);
            return orderJSON;
        } catch (IOException e) {
            log.warn(e);
            throw new ActionException("Failed to read request!");
        } catch (IllegalArgumentException | JSONException e) {
            log.warn(e);
            throw new ActionException("Invalid request!");
        }
    }
    
    /**
     * Commonly used with
     * @param params
     * @throws ActionException
     */
    private void checkForAdminPermission(ActionParameters params) throws ActionException {
        if(!params.getUser().isAdmin()) {
            throw new ActionDeniedException("Session expired");
        }
    }
}
