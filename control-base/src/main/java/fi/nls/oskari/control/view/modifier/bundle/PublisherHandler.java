package fi.nls.oskari.control.view.modifier.bundle;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONObject;

import java.util.Map;

@OskariViewModifier("publisher2")
public class PublisherHandler extends BundleHandler {

    private static final String KEY_TERMS_OF_USE_URL = "termsOfUseUrl";
    public static final String PROPERTY_PUBLISH_TERMS_OF_USE_URL = "oskari.map.publish.terms.url";
    public static final String PROPERTY_TERMS_OF_USE_URL = "oskari.map.terms.url";

    public boolean modifyBundle(final ModifierParams params) throws ModifierException {
        final JSONObject config = getBundleConfig(params.getConfig());

        if (config == null) {
            return false;
        }
        setupTerms(config);
        return false;
    }

    private void setupTerms(final JSONObject config) {
        if(config.has(KEY_TERMS_OF_USE_URL)) {
            return;
        }

        Object termsObj = PropertyUtil.getLocalizableProperty(PROPERTY_PUBLISH_TERMS_OF_USE_URL);
        if (termsObj == null) {
            termsObj = PropertyUtil.getLocalizableProperty(PROPERTY_TERMS_OF_USE_URL);
        }
        if (termsObj instanceof String) {
            JSONHelper.putValue(config, KEY_TERMS_OF_USE_URL, termsObj);
        } else if(termsObj instanceof Map) {
            Map<String, String> values = (Map<String, String>) termsObj;
            JSONHelper.putValue(config, KEY_TERMS_OF_USE_URL, new JSONObject(values));
        }
    }
}
