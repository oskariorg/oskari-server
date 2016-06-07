package fi.nls.oskari.control.statistics;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.statistics.db.RegionSet;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ResponseHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Returns the layer information. This specifies the name and id attributes in the geoserver layer.
 * Sample response:
 * {
 *   "9": {
 *     "name": "oskari:kunnat2013",
 *     "nameTag": "kuntanimi",
 *     "idTag": "kuntakoodi",
 *     "url": " http://localhost:8080/geoserver"
 *   }
 * }
 * @deprecated - these should be detected in frontend by layers of type statslayer
 */
@OskariActionRoute("GetRegionSets")
public class GetRegionSetsHandler extends ActionHandler {
    private static final Logger LOG = LogFactory.getLogger(GetRegionSetsHandler.class);
    private RegionSetService service;

    public void handleAction(ActionParameters ap) throws ActionException {
        JSONObject response = getLayerInfoJSON();
        ResponseHelper.writeResponse(ap, response);
    }

    JSONObject getLayerInfoJSON() throws ActionException {
        JSONObject response = new JSONObject();
        List<RegionSet> regionsets = service.getRegionSets();
        for (RegionSet set : regionsets) {
            try {
                // TODO: maybe reformat the response here. Use an array with id tag, maybe name for the region set
                // frontend uses id to get layer and show the layers name as region set name
                response.put(String.valueOf(set.getId()), set.asJSON());
            } catch (JSONException e) {
                e.printStackTrace();
                throw new ActionException("Something went wrong serializing the layer infos.", e);
            }
        }
        return response;
    }

    public void setRegionsetService(final RegionSetService service) {
        this.service = service;
    }

    @Override
    public void init() {

        if(service == null) {
            setRegionsetService(OskariComponentManager.getComponentOfType(RegionSetService.class));
        }
    }


}
