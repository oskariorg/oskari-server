package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.csw.domain.CSWIsoRecord;
import fi.nls.oskari.csw.service.CSWService;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

/**
 * Created by TMIKKOLAINEN on 1.9.2014.
 */
@OskariActionRoute("GetCSWData")
public class GetCSWDataHandler extends ActionHandler {
    private static final String LANG_PARAM = "lang";
    private static final String UUID_PARAM = "uuid";
    // TODO get baseUrl from properties
    private String baseUrl = PropertyUtil.getOptional("service.metadata.url");

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        if (baseUrl == null) {
            throw new ActionException("Service not configured.");
        }
        final String uuid = params.getRequiredParam(UUID_PARAM);
        // TODO use default lang if not found?
        final String lang = params.getRequiredParam(LANG_PARAM);
        CSWIsoRecord record;
        CSWService service;
        try {
            service = new CSWService(baseUrl);
        } catch (Exception e) {
            throw new ActionException("Failed to initialize CSWService:" + e.getMessage());
        }
        try {
            record = service.getRecordById(uuid, lang);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ActionException("Failed to query service: " + e.getMessage());
        }
        JSONObject result;
        if (record != null) {
            result = record.toJSON();
        } else {
            result = new JSONObject();
        }
        ResponseHelper.writeResponse(params, result);
    }
}
