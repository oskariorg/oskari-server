package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.service.ServiceException;

import fi.nls.oskari.service.styles.SldStyle;
import fi.nls.oskari.service.styles.SldStylesService;
import fi.nls.oskari.service.styles.SldStylesServiceMybatisImpl;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.service.util.ServiceFactory;

import java.net.URLDecoder;
import java.util.List;


/**
 * Returns available sld styles for wfs layers
 */
@OskariActionRoute("SldStyles")
public class SldStylesHandler extends ActionHandler {

    private final SldStylesService service = new SldStylesServiceMybatisImpl();
    private static final String PARAM_SLD_NAME = "name";
    private static final String PARAM_SLD_XML = "xml";

    public void init() {

    }


    /**
     * Action handler
     *
     * @param params Parameters
     * @throws ActionException
     */
    @Override
    public void handleAction(final ActionParameters params)
            throws ActionException {
        final String name = params.getHttpParam(PARAM_SLD_NAME);
        if(name != null){
            // Save new style
            saveStyle(params);
        } else {
            selectAll(params);
        }
    }

    public void selectAll(final ActionParameters params)
            throws ActionException {
        try {

            // find all sld styles for wfs layers
            final List<SldStyle> styles = service.selectAll();
            final JSONArray list = new JSONArray();
            for (SldStyle style : styles) {
                list.put(style.getAsJSON());
            }
            final JSONObject result = new JSONObject();
            JSONHelper.putValue(result, "sldStyles", list);
            ResponseHelper.writeResponse(params, result);
        } catch (Exception ex) {
            throw new ActionException("Error reading sld styles for wfs layers", ex);
        }
    }

    public void saveStyle(final ActionParameters params)
            throws ActionException {
        try {
            final String name = params.getHttpParam(PARAM_SLD_NAME);
            final String xml = params.getHttpParam(PARAM_SLD_XML);
            // Save sld style
            SldStyle style = new SldStyle();
            style.setName(name);
            String value = URLDecoder.decode(xml, "UTF-8");
            style.setSld_style(value);
            final int id = service.saveStyle(style);

            final JSONObject result = new JSONObject();
            JSONHelper.putValue(result, "id", id);
            ResponseHelper.writeResponse(params, result);
        } catch (Exception ex) {
            throw new ActionException("Error inserting new sld style", ex);
        }
    }
}
