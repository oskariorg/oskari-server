package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.view.modifier.ParamHandler;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.BundleService;
import fi.nls.oskari.map.view.BundleServiceIbatisImpl;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONObject;

public abstract class WFSHighlightParamHandler extends ParamHandler {

    private static final Logger log = LogFactory.getLogger(WFSHighlightParamHandler.class);
    private static final BundleService bundleService = new BundleServiceIbatisImpl();

    protected static final String PARM__WFS_FEATURE_ID = "wfsFeature";
    protected static final String PARM__NATIONAL_CADASTRAL_REFERENCE_HIGHLIGHT = "nationalCadastralReferenceHighlight";
    protected static final String NATIONAL_CADASTRAL_REFERENCE_LAYER_ID = PropertyUtil.get("parcel.cadastral.reference.layer.id"); //"142";

    protected static final String STATE_LAYERID_KEY = "highlightFeatureLayerId";

    private static Bundle postprocessorBundle = null;

    static {
        postprocessorBundle = bundleService.getBundleTemplateByName(BUNDLE_POSTPROCESSOR);
        if(postprocessorBundle == null) {
            log.warn("Couldn't get Postprocessor bundle template from DB!");
        }
    }
    /**
     * Utility method to ensure postprocessor is added to the startupseq
     * @param params
     * @return
     */
    protected JSONObject getPostProcessorState(final ModifierParams params) {
        final JSONObject postprocessorState = getBundleState(params.getConfig(), BUNDLE_POSTPROCESSOR);
        // if we just constructed the state -> length == 0 -> add bundle to startupseq
        if(postprocessorState.length() == 0) {
            // not yet initialized, add it to startup/create initial config
            return createPostProcessor(params);
        }
        return postprocessorState;
    }
    /**
     * 
     * @param params
     * @return configuration state JSON node
     */
    protected JSONObject createPostProcessor(final ModifierParams params) {

        // add to startup sequence
        if(postprocessorBundle == null) {
            // postprocessor bundle init failed. See static block.
            log.debug("Tried to insert postprocessor bundle but it isn't initialized");
            return null;
        }
        try {
            // add to startup sequence
            params.getStartupSequence().put(JSONHelper.createJSONObject(postprocessorBundle.getStartup()));
            // add initial config/state
            final JSONObject postprocessorConfig = new JSONObject();
            postprocessorConfig.put(KEY_CONF,
                    JSONHelper.createJSONObject(postprocessorBundle.getConfig()));

            final JSONObject state = JSONHelper.createJSONObject(postprocessorBundle.getState());
            postprocessorConfig.put(KEY_STATE, state);
            params.getConfig().put(BUNDLE_POSTPROCESSOR, postprocessorConfig);
            return state;
        } catch (Exception e) {
            log.error(e, "Failed to add postprocessor bundle to startup sequence:",
                    postprocessorBundle);
        }
        return null;
    }
    
}
