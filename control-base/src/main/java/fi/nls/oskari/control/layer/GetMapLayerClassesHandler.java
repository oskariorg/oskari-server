package fi.nls.oskari.control.layer;

import fi.mml.map.mapwindow.service.db.LayerClassService;
import fi.mml.map.mapwindow.service.db.LayerClassServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.wms.LayerClass;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * GET map layer classes
 * 
 */
@OskariActionRoute("GetMapLayerClasses")
public class GetMapLayerClassesHandler extends ActionHandler {

    private LayerClassService layerClassService = new LayerClassServiceIbatisImpl();
    private static final Logger log = LogFactory.getLogger(GetMapLayerClassesHandler.class);

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        try {
            final List<LayerClass> layerClasses = layerClassService.findAll();
            final JSONObject classJSON = getLayerClasses(layerClasses);
            ResponseHelper.writeResponse(params, classJSON);

        } catch (Exception e) {
            throw new ActionException("Layer class listing failed", e );
        }
    }

    private JSONObject getLayerClasses(final List<LayerClass> layerClasses) {
        final JSONObject classJSON = new JSONObject();

        try {
            for (LayerClass lc : layerClasses) {
                final JSONObject classProperties = new JSONObject();

                classProperties.put("id", lc.getId());
                classProperties.put("parentid", lc.getParent());
                for (Map.Entry<String, String> localization : lc.getNames().entrySet()) {
                    classProperties.put("name" + Character.toUpperCase(localization.getKey().charAt(0)) + localization.getKey().substring(1), localization.getValue());
                }
                classProperties.put("isgroupmap", lc.isGroupMap());
                classProperties.put("isselectable", lc.isMapLayersSelectable());
                if (lc.getChildren().isEmpty()) {
                    classProperties.put("childrens", lc.getChildren()); // <LayerClass>
                }
                else {
                    classProperties.put("childrens", getLayerClasses(lc
                            .getChildren()));
                }
                // list
                classProperties.put("dataurl", lc.getDataUrl());
                classProperties.put("legendimage", lc.getLegendImage());

                classJSON.accumulate(String.valueOf(lc.getId()),
                        classProperties);
            }
        } catch (JSONException e) {
            log.warn(e, "JSON create failed");
        }
        return classJSON;
    }


}
