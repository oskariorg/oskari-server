package fi.nls.oskari.control.view.modifier;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.control.view.modifier.bundle.BundleHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONException;
import org.json.JSONObject;

@OskariViewModifier("myplaces2")
public class Myplaces2Handler extends BundleHandler {
    
    private static final Logger log = LogFactory.getLogger(Myplaces2Handler.class);
    private static final JSONObject CONFIG = new JSONObject();
    private static String[] DEFAULT_NAMES= new String[0];

    private static final String DEFAULT_XMLNS_NAMESPACE = "oskari";
    private static final String DEFAULT_XMLNS = "http://www.oskari.org";

    private MyPlacesService service = null;

    @Override
    public void init() {
        super.init();
        service = OskariComponentManager.getComponentOfType(MyPlacesService.class);
        // crafting as JSONObject so data gets validated on init (that we can actually use these in JSONObject)
        final JSONObject layerDefaults = new JSONObject();
        final String xmlnsPrefix = PropertyUtil.get("myplaces.xmlns.prefix", DEFAULT_XMLNS_NAMESPACE);
        JSONHelper.putValue(layerDefaults, "wmsName", xmlnsPrefix + ":my_places_categories");
        JSONHelper.putValue(CONFIG, "layerDefaults", layerDefaults);

        JSONHelper.putValue(CONFIG, "featureNS", PropertyUtil.get("myplaces.xmlns", DEFAULT_XMLNS));
        JSONHelper.putValue(CONFIG, "wmsUrl", service.getClientWMSUrl());
        DEFAULT_NAMES = JSONObject.getNames(CONFIG);
    }

    /**
     * Updates the query URL from the myplaces2 configuration
     * @param params
     * @return
     * @throws ModifierException
     */
    public boolean modifyBundle(final ModifierParams params) throws ModifierException {
        final JSONObject myplacesConfig = getBundleConfig(params.getConfig());
        if (myplacesConfig == null) {
            return false;
        }
        // copy defaults
        for(String key: DEFAULT_NAMES) {
            if(!myplacesConfig.has(key)) {
                try {
                    myplacesConfig.putOpt(key, CONFIG.opt(key));
                }catch (Exception ignored) {}
            }
        }
        // setup queryUrl
        final String ajaxUrl = params.getBaseAjaxUrl() + params.getAjaxRouteParamName() + "=MyPlaces";
        try {
            myplacesConfig.putOpt("queryUrl", ajaxUrl);
            return true;
        } catch (JSONException ignored) {
            log.warn("Unable to replace", getName(), "conf.queryUrl to ", ajaxUrl);
        }
        return false;
    }
}
/*
{
   "layerDefaults":{
      "wmsName":"oskari:my_places_categories"
   },
   "queryUrl":"/web/fi/kartta?p_p_id=Portti2Map_WAR_portti2mapportlet&p_p_lifecycle=2&action_route=MyPlaces",
   "wmsUrl":"/karttatiili/myplaces?myCat=",
   "featureNS":"http://www.oskari.org"
}
*/