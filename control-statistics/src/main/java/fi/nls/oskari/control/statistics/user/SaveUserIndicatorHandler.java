package fi.nls.oskari.control.statistics.user;

import org.oskari.statistics.user.UserIndicatorService;
import org.oskari.statistics.user.UserIndicatorServiceImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import org.oskari.statistics.user.UserIndicator;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: EVAARASMAKI
 * Date: 22.11.2013
 * Time: 9:25
 * To change this template use File | Settings | File Templates.
 * 
 */
@OskariActionRoute("SaveUserIndicator")
public class SaveUserIndicatorHandler extends ActionHandler {


    private static UserIndicatorService userIndicatorService = new UserIndicatorServiceImpl();

    protected static String PARAM_INDICATOR_ID = "id";
    private static String PARAM_INDICATOR_TITLE = "title";
    private static String PARAM_INDICATOR_SOURCE = "source";
    private static String PARAM_INDICATOR_MATERIAL = "material"; //WMS- layer
    private static String PARAM_INDICATOR_YEAR = "year";
    private static String PARAM_INDICATOR_DATA = "data";
    private static String PARAM_INDICATOR_PUBLISHED = "published";
    private static String PARAM_INDICATOR_DESCRIPTION = "description";
    private static String PARAM_INDICATOR_CATEGORY = "category";

    private static final fi.nls.oskari.log.Logger log = LogFactory.getLogger(SaveUserIndicatorHandler.class);

    public void handleAction(ActionParameters params) throws ActionException {
        if (params.getUser().isGuest()) {
            throw new ActionDeniedException("Session expired");
        }
        int id = Integer.parseInt(params.getHttpParam(PARAM_INDICATOR_ID, "-1"));

        UserIndicator ui = populateUi(params);

        if (id != -1) {
            //update
           ui.setId(id);
           userIndicatorService.update(ui);
        } else {
            //insert
            id = userIndicatorService.insert(ui);
        }

        JSONObject jobj = new JSONObject();
        JSONHelper.putValue(jobj, "id", id);
        ResponseHelper.writeResponse(params,jobj);
    }

    private UserIndicator populateUi(ActionParameters params) {
        UserIndicator ui = new UserIndicator();

        ui.setUserId(params.getUser().getId());
        ui.setTitle(params.getHttpParam(PARAM_INDICATOR_TITLE));
        ui.setSource(params.getHttpParam(PARAM_INDICATOR_SOURCE));
        String material = params.getHttpParam(PARAM_INDICATOR_MATERIAL);
        if (material != null) {
            // New user indicators do not store material id, because mapping to layers is done through Oskari names.
            ui.setMaterial(Long.parseLong(material));
        }
        ui.setDescription(params.getHttpParam(PARAM_INDICATOR_DESCRIPTION));
        ui.setYear(Integer.parseInt(params.getHttpParam(PARAM_INDICATOR_YEAR)));
        ui.setData(params.getHttpParam(PARAM_INDICATOR_DATA));
        ui.setPublished(Boolean.parseBoolean(params.getHttpParam(PARAM_INDICATOR_PUBLISHED)));
        ui.setCategory(params.getHttpParam(PARAM_INDICATOR_CATEGORY));
        return ui;
    }
}
