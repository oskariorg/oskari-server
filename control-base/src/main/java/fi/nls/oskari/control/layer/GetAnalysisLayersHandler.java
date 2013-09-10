package fi.nls.oskari.control.layer;

import java.util.ArrayList;
import java.util.Iterator;

import java.util.List;

import fi.nls.oskari.annotation.OskariActionRoute;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.mml.map.mapwindow.service.db.MapLayerService;

import fi.mml.map.mapwindow.util.MapLayerWorker;

import fi.nls.oskari.domain.map.Layer;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;

import fi.nls.oskari.map.analysis.service.AnalysisDataService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.ServiceFactory;

import fi.nls.oskari.util.ResponseHelper;

/**
 * Get WMS map layers
 */
@OskariActionRoute("GetAnalysisLayers")
public class GetAnalysisLayersHandler extends ActionHandler {

    final static String LANGUAGE_ATTRIBUTE = "lang";

    private AnalysisDataService analysisDataService = new AnalysisDataService();
    
   

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        try {

           
            final String lang = params.getHttpParam(LANGUAGE_ATTRIBUTE, params
                    .getLocale().getLanguage());


            final JSONObject layers = analysisDataService.getListOfAllAnalysisLayers(
                    params.getUser().getUuid(), lang);

          
                ResponseHelper.writeResponse(params, layers);
          

        } catch (Exception e) {
            throw new ActionException(
                    "Couldn't request Analysis data service - get analysis layers", e);
        }
    }

   
}
