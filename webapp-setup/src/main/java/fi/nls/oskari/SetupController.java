package fi.nls.oskari;

import fi.nls.oskari.geoserver.AnalysisHelper;
import fi.nls.oskari.geoserver.GeoserverPopulator;
import fi.nls.oskari.geoserver.MyplacesHelper;
import fi.nls.oskari.geoserver.UserlayerHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles the map UI based on requested application and user role default applications
 */
@Controller
public class SetupController {

    private final static Logger log = LogFactory.getLogger(SetupController.class);
    private final static String PROPERTY_VERSION = "oskari.client.version";

    private String version = null;

    public SetupController() {
        // Get version from properties
        version = PropertyUtil.get(PROPERTY_VERSION);
    }

    @RequestMapping("/")
    public ModelAndView index(ModelAndView model) {
        model.setViewName("index");
        Map<String, String> map = new LinkedHashMap<>(9);
        model.addObject("properties", map);
        map.put("geoserver.myplaces.url", GeoserverPopulator.getGeoserverProp(MyplacesHelper.MODULE_NAME, GeoserverPopulator.KEY_URL));
        map.put("geoserver.myplaces.user", GeoserverPopulator.getGeoserverProp(MyplacesHelper.MODULE_NAME, GeoserverPopulator.KEY_USER));
        map.put("geoserver.myplaces.password", GeoserverPopulator.getGeoserverProp(MyplacesHelper.MODULE_NAME, GeoserverPopulator.KEY_PASSWD));

        map.put("geoserver.analysis.url", GeoserverPopulator.getGeoserverProp(AnalysisHelper.MODULE_NAME, GeoserverPopulator.KEY_URL));
        map.put("geoserver.analysis.user", GeoserverPopulator.getGeoserverProp(AnalysisHelper.MODULE_NAME, GeoserverPopulator.KEY_USER));
        map.put("geoserver.analysis.password", GeoserverPopulator.getGeoserverProp(AnalysisHelper.MODULE_NAME, GeoserverPopulator.KEY_PASSWD));

        map.put("geoserver.userlayer.url", GeoserverPopulator.getGeoserverProp(UserlayerHelper.MODULE_NAME, GeoserverPopulator.KEY_URL));
        map.put("geoserver.userlayer.user", GeoserverPopulator.getGeoserverProp(UserlayerHelper.MODULE_NAME, GeoserverPopulator.KEY_USER));
        map.put("geoserver.userlayer.password", GeoserverPopulator.getGeoserverProp(UserlayerHelper.MODULE_NAME, GeoserverPopulator.KEY_PASSWD));
        return model;
    }

    @RequestMapping("/version")
    public
    @ResponseBody
    String version() {
        return version;
    }

    /**
     * Configures the geoserver for myplaces, analysis and userlayers
     *
     * @return
     */
    @RequestMapping("/setup")
    public ModelAndView init(@RequestParam("srs") String srs, ModelAndView model) {
        String errorMsg = "Error creating geoserver configuration! Error: ";
        model.setViewName("geoserver_result");
        try {
            GeoserverPopulator.setupAll(srs);
            model.addObject("message", "success");
            Map<String, Integer> ids = new HashMap<>(3);
            model.addObject("properties",ids);
            int myplacesId = GeoserverPopulator.setupMyplacesLayer(srs);
            ids.put("myplaces.baselayer.id", myplacesId);
            int analysisId = GeoserverPopulator.setupAnalysisLayer(srs);
            ids.put("analysis.baselayer.id", analysisId);
            int userlayerId = GeoserverPopulator.setupUserLayer(srs);
            ids.put("userlayer.baselayer.id", userlayerId);

            return model;
        } catch (Exception e) {
            errorMsg = errorMsg + e.getMessage();
            log.error(e, errorMsg);
            model.addObject("message", errorMsg);
        }
        return model;
    }

}
