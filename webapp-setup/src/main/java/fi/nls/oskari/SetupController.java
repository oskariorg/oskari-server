package fi.nls.oskari;

import fi.nls.oskari.geoserver.GeoserverPopulator;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public String index() {
        return "index";
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
    public
    @ResponseBody
    String init(@RequestParam("srs") String srs) {
        String errorMsg = "Error creating geoserver configuration! Error: ";

        try {
            GeoserverPopulator.setupAll(srs);
            return "success";
        } catch (Exception e) {
            errorMsg = errorMsg + e.getMessage();
            log.error(e, errorMsg);
        }
        return errorMsg;
    }

}
