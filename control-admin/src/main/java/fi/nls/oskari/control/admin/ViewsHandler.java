package fi.nls.oskari.control.admin;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("Views")
public class ViewsHandler extends RestActionHandler {

    private static final Logger LOG = LogFactory.getLogger(ViewsHandler.class);
    // TODO: Use a singleton ObjectMapper
    private static final ObjectMapper OM = new ObjectMapper();

    private ViewService viewService;

    @Override
    public void init() {
        viewService = new ViewServiceIbatisImpl();
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
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
            byte[] json = OM.writeValueAsBytes(view);
            writeJson(params.getResponse(), json);
        } catch (JsonProcessingException e) {
            LOG.warn(e);
            ResponseHelper.writeError(params, "Failed to process View to JSON!");
        }
    }

    private static void writeJson(HttpServletResponse response, byte[] body) {
        response.setContentType("application/json;charset=UTF-8");
        response.setContentLength(body.length);
        try (OutputStream out = response.getOutputStream()) {
            out.write(body);
        } catch (IOException e) {
            LOG.warn(e);
        }
    }

}
