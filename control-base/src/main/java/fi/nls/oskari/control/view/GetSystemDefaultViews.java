package fi.nls.oskari.control.view;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("GetSystemDefaultViews")
public class GetSystemDefaultViews extends ActionHandler {

    private static final JsonFactory JSONF = new JsonFactory();

    private ViewService viewService;

    @Override
    public void init() {
        viewService = new ViewServiceIbatisImpl();
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        List<Long> viewIds = getDefaultViewIds();
        List<View> views = getViews(viewIds);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeResponseJSON(views, baos);
        ResponseHelper.writeResponse(params, 200, ResponseHelper.CONTENT_TYPE_JSON_UTF8, baos);
    }

    private List<Long> getDefaultViewIds() throws ActionException {
        try {
            return viewService.getSystemDefaultViewIds();
        } catch (ServiceException e) {
            throw new ActionException(e.getMessage(), e);
        }

    }

    private List<View> getViews(List<Long> viewIds) {
        List<View> views = new ArrayList<>(viewIds.size());
        for (long viewId : viewIds) {
            views.add(viewService.getViewWithConf(viewId));
        }
        return views;
    }

    private void writeResponseJSON(List<View> views, OutputStream out) throws ActionException {
        try (JsonGenerator json = JSONF.createGenerator(out)) {
            json.writeStartArray();
            for (View view : views) {
                writeView(view, json);
            }
            json.writeEndArray();
        } catch (JSONException | IOException e) {
            throw new ActionException(e.getMessage(), e);
        }

    }

    private void writeView(View view, JsonGenerator json) throws JSONException, IOException {
        String uuid = view.getUuid();
        String srsName = view.getBundleByName("mapfull")
                .getConfigJSON()
                .getJSONObject("mapOptions")
                .getString("srsName");

        json.writeStartObject();
        json.writeStringField("uuid", uuid);
        json.writeStringField("srsName", srsName);
        json.writeEndObject();
    }

}
