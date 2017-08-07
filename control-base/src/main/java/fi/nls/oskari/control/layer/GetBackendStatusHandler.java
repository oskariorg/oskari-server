package fi.nls.oskari.control.layer;

import fi.mml.map.mapwindow.service.db.BackendStatusService;
import fi.mml.map.mapwindow.service.db.BackendStatusServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.BackendStatus;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

@OskariActionRoute("GetBackendStatus")
public class GetBackendStatusHandler extends ActionHandler {

    private static final Logger log = LogFactory.getLogger(GetBackendStatusHandler.class);
    private final BackendStatusService backendStatusService = new BackendStatusServiceIbatisImpl();
    
    private final static String ID = "id";
    private final static String TS = "ts";
    private final static String MAPLAYER_ID = "maplayer_id";
    private final static String STATUS = "status";
    private final static String INFOURL = "infourl";
    private final static String STATUSJSON = "statusjson";
    private final static String BACKEND_STATUS = "backendstatus";

    private final static String PARAM_SUBSET = "Subset";
    private final static String DEFAULT_SUBSET = "Alert";
    private final static String SUBSET_ALL_KNOWN = "AllKnown";

    public void handleAction(ActionParameters params) throws ActionException {
       
       List<BackendStatus> bes;
       
       if (SUBSET_ALL_KNOWN.equals(params.getHttpParam(PARAM_SUBSET, DEFAULT_SUBSET))) {
           bes = backendStatusService.findAllKnown();
       } else {
           bes = backendStatusService.findAll();
       }
       
       log.debug("BackendStatus list size = " + bes.size());

       final JSONArray rootList = new JSONArray();
       for (final BackendStatus  be: bes) {
            final JSONObject status = new JSONObject();
            JSONHelper.putValue(status, ID, be.getId());
            JSONHelper.putValue(status, TS, be.getTs());
            JSONHelper.putValue(status, MAPLAYER_ID, be.getMaplayer_id());
            JSONHelper.putValue(status, STATUS, be.getStatus());
            JSONHelper.putValue(status, INFOURL, be.getInfourl());
            JSONHelper.putValue(status, STATUSJSON, be.getStatusjson());
            rootList.put(status);
       }
       
       final JSONObject root = new JSONObject();
       JSONHelper.putValue(root, BACKEND_STATUS, rootList);

       ResponseHelper.writeResponse(params,root);
    }
}
