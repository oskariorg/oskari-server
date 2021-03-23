package fi.nls.oskari.control.myplaces.handler;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.myplaces.service.MyPlacesFeaturesService;
import fi.nls.oskari.myplaces.service.wfst.MyPlacesFeaturesServiceWFST;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@OskariActionRoute("ExportMyPlacesLayerFeatures")
public class ExportMyPlacesFeaturesHandler extends RestActionHandler {
    private final static Logger LOG = LogFactory.getLogger(ExportMyPlacesFeaturesHandler.class);
    private static final String PARAM_SRS = "srs";
    private static final String PARAM_LAYER_ID = "categoryId";
    private static final String PARAM_INDENT = "indent";
    private static final String PARAM_PRETTIFY = "prettify";
    private static final String FILE_EXT = "geojson";
    private static final String FILE_TYPE = "application/json";
    private static final int DEFAULT_INDENT = 2;

    private MyPlacesFeaturesService featureService;
    private MyPlacesService service;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    @Override
    public void init() {
        super.init();
        featureService = new MyPlacesFeaturesServiceWFST();
        service = OskariComponentManager.getComponentOfType(MyPlacesService.class);
    }
    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final String srs = params.getHttpParam(PARAM_SRS, PropertyUtil.get("oskari.native.srs", "EPSG:4326"));
        final String layerId = params.getRequiredParam(PARAM_LAYER_ID);
        final int indent = params.getHttpParam(PARAM_INDENT, DEFAULT_INDENT);
        final boolean prettify = params.getHttpParam(PARAM_PRETTIFY, true);
        try {
            long categoryId = Long.parseLong(layerId);
            if (!service.canModifyCategory(user, categoryId)) {
                throw new ActionDeniedException(
                        "Tried to GET features from category: " + categoryId);
            }

            JSONObject featureCollection = featureService.getFeaturesByCategoryId(categoryId, srs);
            String layerName = service.findCategory(categoryId).getName();
            String timestamp = LocalDate.now().format(TIME_FORMAT);
            String fileName = layerName + "_" + timestamp + "." + FILE_EXT;
            HttpServletResponse response = params.getResponse();
            response.setContentType(FILE_TYPE);
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            try (OutputStream out = response.getOutputStream()) {
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out))) {
                    String stringified = prettify ? featureCollection.toString(indent) : featureCollection.toString();
                    bw.write(stringified);
                }
            } catch (Exception e) {
                LOG.warn(e);
                throw new ActionException("Failed to write JSON");
            }
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to export features");
        }
    }

}
