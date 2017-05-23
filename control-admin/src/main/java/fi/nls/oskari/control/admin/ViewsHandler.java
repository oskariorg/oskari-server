package fi.nls.oskari.control.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceIbatisImpl;
import fi.nls.oskari.map.view.ViewException;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("Views")
public class ViewsHandler extends RestActionHandler {

    private static final Logger LOG = LogFactory.getLogger(ViewsHandler.class);

    private BundleService bundleService;
    private ViewService viewService;

    public ViewsHandler() {
        bundleService = new BundleServiceIbatisImpl();
        viewService = new ViewServiceIbatisImpl();
    }

    public ViewsHandler(BundleService bundleService, ViewService viewService) {
        this.bundleService = bundleService;
        this.viewService = viewService;
    }

    public void setBundleService(BundleService bundleService) {
        this.bundleService = bundleService;
    }

    public void setViewService(ViewService viewService) {
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
            byte[] json = viewToJson(view);
            writeJson(params.getResponse(), 200, json);
        } catch (JSONException e) {
            LOG.warn(e);
            ResponseHelper.writeError(params, "Failed to write JSON!");
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        HttpServletRequest req = params.getRequest();
        String contentType = req.getContentType();
        if (contentType == null || !contentType.startsWith("application/json")) {
            throw new ActionException("Expected JSON input!");
        }

        try {
            View view = parseView(req);
            long id = viewService.addView(view);
            byte[] b = createJSON(id, view.getUuid());
            writeJson(params.getResponse(), HttpServletResponse.SC_CREATED, b);
        } catch (ViewException e) {
            LOG.warn(e, "Failed to import view!");
            ResponseHelper.writeError(params, "Failed to add view!");
        } catch (JSONException e) {
            LOG.warn(e, "Failed to form response!");
            ResponseHelper.writeError(params, "Failed to form response!");
        }
    }

    protected byte[] viewToJson(View view) throws JSONException {
        final JSONObject viewJSON = new JSONObject();
        viewJSON.put("creator", view.getCreator());
        viewJSON.put("public", view.isPublic());
        viewJSON.put("onlyUuid", view.isOnlyForUuId());
        viewJSON.put("name", view.getName());
        viewJSON.put("type", view.getType());
        viewJSON.put("default", view.isDefault());
        viewJSON.put("application", view.getApplication());
        viewJSON.put("page", view.getPage());
        viewJSON.put("developmentPath", view.getDevelopmentPath());

        final JSONArray bundles = createBundles(view.getBundles());
        viewJSON.put("bundles", bundles);

        return viewJSON.toString().getBytes(StandardCharsets.UTF_8);
    }

    private JSONArray createBundles(List<Bundle> bundles) throws JSONException {
        JSONArray bundlesJSON = new JSONArray();

        if (bundles != null) {
            for (Bundle bundle : bundles) {
                JSONObject bundleJSON = new JSONObject();
                bundleJSON.put("id", bundle.getName());
                if (bundle.getBundleinstance() != null) {
                    bundleJSON.put("instance", bundle.getBundleinstance());
                }
                if (bundle.getStartup() != null) {
                    bundleJSON.put("startup", new JSONObject(bundle.getStartup()));
                }
                if (bundle.getConfigJSON() != null) {
                    bundleJSON.put("config", bundle.getConfigJSON());
                }
                if (bundle.getStateJSON() != null) {
                    bundleJSON.put("state", bundle.getStateJSON());
                }
                bundlesJSON.put(bundleJSON);
            }
        }

        return bundlesJSON;
    }

    protected View parseView(HttpServletRequest req) throws ActionException {
        try (InputStream in = req.getInputStream()) {
            byte[] json = IOHelper.readBytes(in);
            return viewFromJson(json);
        } catch (IOException e) {
            LOG.warn(e);
            throw new ActionException("Failed to read request!");
        } catch (IllegalArgumentException | JSONException e) {
            LOG.warn(e);
            throw new ActionException("Invalid request!");
        }
    }

    protected View viewFromJson(byte[] json)
            throws JSONException, IllegalArgumentException {
        final String jsonString = new String(json, StandardCharsets.UTF_8);
        final JSONObject viewJSON = new JSONObject(jsonString);

        final View view = new View();
        view.setCreator(viewJSON.optLong("creator", -1L));
        view.setIsPublic(viewJSON.optBoolean("public", false));
        view.setOnlyForUuId(viewJSON.optBoolean("onlyUuid", true));
        view.setName(viewJSON.getString("name"));
        view.setType(viewJSON.getString("type"));
        view.setIsDefault(viewJSON.optBoolean("default"));

        if (viewJSON.has("oskari")) {
            // Support "old" format
            final JSONObject oskari = viewJSON.getJSONObject("oskari");
            view.setApplication(oskari.getString("application"));
            view.setPage(oskari.getString("page"));
            view.setDevelopmentPath(oskari.getString("development_prefix"));
        } else {
            view.setApplication(viewJSON.getString("application"));
            view.setPage(viewJSON.getString("page"));
            view.setDevelopmentPath(viewJSON.getString("developmentPath"));
        }

        addBundles(view, viewJSON.getJSONArray("bundles"));

        return view;
    }

    private void addBundles(final View view, final JSONArray bundles)
            throws JSONException, IllegalArgumentException {
        if (bundles == null) {
            return;
        }

        for (int i = 0; i < bundles.length(); ++i) {
            final JSONObject bJSON = bundles.getJSONObject(i);
            final String name = bJSON.getString("id");
            final Bundle bundle = bundleService.getBundleTemplateByName(name);
            if (bundle == null) {
                throw new IllegalArgumentException("Bundle not registered - id: " + name);
            }
            if (bJSON.has("instance")) {
                bundle.setBundleinstance(bJSON.getString("instance"));
            }
            if (bJSON.has("startup")) {
                bundle.setStartup(bJSON.getJSONObject("startup").toString());
            }
            if (bJSON.has("config")) {
                bundle.setConfig(bJSON.getJSONObject("config").toString());
            }
            if (bJSON.has("state")) {
                bundle.setState(bJSON.getJSONObject("state").toString());
            }
            // set up seq number
            view.addBundle(bundle);
        }
    }

    private byte[] createJSON(long id, String uuid) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("uuid", uuid);
        return json.toString().getBytes(StandardCharsets.UTF_8);
    }

    protected static void writeJson(HttpServletResponse resp, int sc, byte[] b) {
        resp.setStatus(sc);
        resp.setContentType("application/json;charset=UTF-8");
        resp.setContentLength(b.length);
        try (OutputStream out = resp.getOutputStream()) {
            out.write(b);
        } catch (IOException e) {
            LOG.warn(e);
        }
    }

}
