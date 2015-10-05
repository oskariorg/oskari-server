package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONArray;
import org.json.JSONObject;

@OskariViewModifier("wfsFeature")
public class FeatureIdWFSHighlightParamHandler extends WFSHighlightParamHandler {

    private static final Logger log = LogFactory.getLogger(FeatureIdWFSHighlightParamHandler.class);

    @Override
    public boolean handleParam(ModifierParams params)
            throws ModifierException {
        if(params.getParamValue() == null) {
            return false;
        }
        final JSONArray featureIdList = new JSONArray();
        
        final String[] featureList = params.getParamValue().split(",");
        for (String s : featureList) {
            featureIdList.put(s);
        }
        try {
            final JSONObject postprocessorState = getPostProcessorState(params);
            if(featureIdList.length() > 0) {
                postprocessorState.put("highlightFeatureId", featureIdList);
                return true;
            }
        }
        catch(Exception ex) {
            log.error(ex, "Couldn't insert features to postprocessor bundle state");
        }
        return false;
    }
}
