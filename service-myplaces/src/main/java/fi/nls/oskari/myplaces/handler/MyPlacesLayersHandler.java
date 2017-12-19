package fi.nls.oskari.myplaces.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.myplaces.MyPlaceCategoryHelper;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.myplaces.service.MyPlacesLayersService;
import fi.nls.oskari.myplaces.service.wfst.MyPlacesLayersServiceWFST;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("MyPlacesLayers")
public class MyPlacesLayersHandler extends RestActionHandler {

    private final static Logger LOG = LogFactory.getLogger(MyPlacesLayersHandler.class);

    private static final String PARAM_LAYERS = "layers";
    private static final String JSKEY_DELETED = "deleted";

    private MyPlacesService service;
    private MyPlacesLayersService layerService;

    @Override
    public void init() {
        service = OskariComponentManager.getComponentOfType(MyPlacesService.class);
        layerService = new MyPlacesLayersServiceWFST();
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        final String uuid = params.getUser().getUuid();

        final List<MyPlaceCategory> categories;
        try {
            categories = layerService.getByUserId(uuid);
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to get users layers");
        }

        if (categories.size() == 0) {
            // If user has no categories insert a new default category
            categories.add(insertDefaultCategory(uuid));
        }

        ByteArrayOutputStream baos;
        try {
            baos = MyPlaceCategoryHelper.toGeoJSONFeatureCollection(categories);
        } catch (IOException e) {
            LOG.warn(e);
            throw new ActionException("Failed to create response");
        }

        ResponseHelper.writeResponse(params, 200,
                ResponseHelper.CONTENT_TYPE_JSON_UTF8, baos);
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        final String uuid = params.getUser().getUuid();
        final List<MyPlaceCategory> categories = readCategories(params, false);
        for (MyPlaceCategory category : categories) {
            category.setUuid(uuid);
        }

        try {
            int inserted = layerService.insert(categories);
            LOG.info("Inserted", inserted, "/", categories.size());
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to insert layers");
        }

        ByteArrayOutputStream baos;
        try {
            baos = MyPlaceCategoryHelper.toGeoJSONFeatureCollection(categories);
        } catch (IOException e) {
            LOG.warn(e);
            throw new ActionException("Failed to create response");
        }

        ResponseHelper.writeResponse(params, 200,
                ResponseHelper.CONTENT_TYPE_JSON_UTF8, baos);
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final String uuid = user.getUuid();
        final List<MyPlaceCategory> categories = readCategories(params, true);
        for (MyPlaceCategory category : categories) {
            category.setUuid(uuid);
        }
        checkUserCanModifyCategories(user, categories);
        long[] ids = categories.stream().mapToLong(MyPlaceCategory::getId).toArray();
        try {
            LOG.debug("Updating Categories:", ids);
            int updated = layerService.update(categories);
            LOG.info("Updated", updated, "/", categories.size());
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to update layers");
        }

        ByteArrayOutputStream baos;
        try {
            baos = MyPlaceCategoryHelper.toGeoJSONFeatureCollection(categories);
        } catch (IOException e) {
            LOG.warn(e);
            throw new ActionException("Failed to create response");
        }

        ResponseHelper.writeResponse(params, 200,
                ResponseHelper.CONTENT_TYPE_JSON_UTF8, baos);
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final String layerIds = params.getRequiredParam(PARAM_LAYERS);
        final long[] ids = Arrays.stream(layerIds.split(","))
                .mapToLong(Long::parseLong)
                .toArray();
        for (long id : ids) {
            if (!service.canModifyCategory(user, id)) {
                throw new ActionDeniedException("Tried to delete category: " + id);
            }
        }

        final int deleted;
        try {
            deleted = layerService.delete(ids);
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to delete layers");
        }

        JSONObject response = new JSONObject();
        JSONHelper.putValue(response, JSKEY_DELETED, deleted);
        ResponseHelper.writeResponse(params, response);
    }

    private MyPlaceCategory insertDefaultCategory(String uuid)
            throws ActionException {
        try {
            MyPlaceCategory category = createDefaultCategory();
            category.setUuid(uuid);
            layerService.insert(Arrays.asList(category));
            return category;
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to insert default category");
        }
    }

    private MyPlaceCategory createDefaultCategory() {
        MyPlaceCategory category = new MyPlaceCategory();
        category.setCategory_name("");
        category.setDefault(true);
        //point
        category.setDot_color("#000000");
        category.setDot_size(3);
        category.setDot_shape("1");
        //line
        category.setStroke_width(1);
        category.setStroke_dasharray("");
        category.setStroke_linecap("butt");
        category.setStroke_linejoin("mitre");
        category.setStroke_color("#3233ff");
        //area
        category.setFill_pattern(-1);
        category.setBorder_dasharray("");
        category.setBorder_linejoin("mitre");
        category.setBorder_width(1);
        category.setBorder_color("#000000");
        category.setFill_color("#ffde00");
        category.setFill_pattern(-1);

        return category;
    }

    private List<MyPlaceCategory> readCategories(ActionParameters params,
            boolean checkId) throws ActionException {
        try {
            final String payload;
            try (InputStream in = params.getRequest().getInputStream()) {
                payload = IOHelper.readString(in);
            } catch (IOException e) {
                throw new ActionException("IOException occured");
            }
            return MyPlaceCategoryHelper.parseFromGeoJSON(payload, checkId);
        } catch (JSONException e) {
            throw new ActionParamsException("Invalid input", e);
        } catch (IOException e) {
            throw new ActionParamsException("IOException occured", e);
        }
    }

    private void checkUserCanModifyCategories(User user,
            List<MyPlaceCategory> categories) throws ActionDeniedException {
        for (MyPlaceCategory category : categories) {
            if (!service.canModifyCategory(user, category.getId())) {
                throw new ActionDeniedException(
                        "Tried to modify category: " + category.getId());
            }
        }
    }
}
