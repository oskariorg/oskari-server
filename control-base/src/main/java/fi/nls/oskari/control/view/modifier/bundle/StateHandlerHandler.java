package fi.nls.oskari.control.view.modifier.bundle;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONObject;

import javax.servlet.http.HttpSession;

@OskariViewModifier("statehandler")
public class StateHandlerHandler extends BundleHandler {
    private static final Logger log = LogFactory.getLogger(StateHandlerHandler.class);
    private static final String SESSION_LENGTH = "sessionLength";

    public boolean modifyBundle(final ModifierParams params) throws ModifierException {
        final JSONObject config = getBundleConfig(params.getConfig());
        final HttpSession session = params.getActionParams().getRequest().getSession();
        final int sessionLengthInMinutes = session.getMaxInactiveInterval() / 60;
        log.debug("SESSION LENGTH IN MINUTES:", sessionLengthInMinutes);

        if (config == null) {
            return false;
        }
        if (!params.getUser().isGuest()) {
            JSONHelper.putValue(config, SESSION_LENGTH, sessionLengthInMinutes);
        }
        return false;
    }
}
