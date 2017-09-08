package fi.nls.oskari.control.admin;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.*;
import fi.nls.oskari.map.view.util.ViewHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@OskariActionRoute("Views")
public class ViewsHandler extends RestActionHandler {

    private static final Logger LOG = LogFactory.getLogger(ViewsHandler.class);

    private final BundleService bundleService;
    private final ViewService viewService;

    public ViewsHandler() {
        this(new BundleServiceIbatisImpl(), new ViewServiceIbatisImpl());
    }

    public ViewsHandler(BundleService bundleService, ViewService viewService) {
        this.bundleService = bundleService;
        this.viewService = viewService;
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionDeniedException {
        if (!params.getUser().isAdmin()) {
            throw new ActionDeniedException("Admin only");
        }
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        String uuid = params.getRequiredParam("uuid");
        View view = viewService.getViewWithConfByUuId(uuid);
        if (view == null) {
            LOG.info("Could not find view for uuid: ", uuid);
            throw new ActionException("View not found!");
        }

        try {
            JSONObject json = ViewHelper.viewToJson(bundleService, view);
            ResponseHelper.writeResponse(params, HttpServletResponse.SC_OK, json);
        } catch (JSONException e) {
            LOG.warn(e);
            ResponseHelper.writeError(params, "Failed to write JSON!");
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        HttpServletRequest req = params.getRequest();
        String contentType = req.getContentType();
        if (contentType == null || !contentType.startsWith(IOHelper.CONTENT_TYPE_JSON)) {
            throw new ActionException("Expected JSON input!");
        }

        try {
            View view = parseView(req);
            viewService.addView(view);
            JSONObject json = createResponse(view);
            ResponseHelper.writeResponse(params, HttpServletResponse.SC_CREATED, json);
        } catch (ViewException e) {
            LOG.warn(e, "Failed to add view!");
            ResponseHelper.writeError(params, "Failed to add view!");
        } catch (JSONException e) {
            LOG.warn(e, "Failed to form response!");
            ResponseHelper.writeError(params, "Failed to form response!");
        }
    }

    protected View parseView(HttpServletRequest req) throws ActionException {
        try (InputStream in = req.getInputStream()) {
            final byte[] json = IOHelper.readBytes(in);
            final String jsonString = new String(json, StandardCharsets.UTF_8);
            final JSONObject viewJSON = new JSONObject(jsonString);
            return ViewHelper.viewFromJson(bundleService, viewJSON);
        } catch (IOException e) {
            LOG.warn(e);
            throw new ActionException("Failed to read request!");
        } catch (IllegalArgumentException | JSONException e) {
            LOG.warn(e);
            throw new ActionException("Invalid request!");
        }
    }

    private static JSONObject createResponse(View view) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", view.getId());
        json.put("uuid", view.getUuid());
        json.put("url", view.getUrl());
        return json;
    }

}
