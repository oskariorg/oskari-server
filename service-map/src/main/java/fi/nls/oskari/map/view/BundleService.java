package fi.nls.oskari.map.view;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.service.db.BaseService;
import org.json.JSONObject;

import java.util.List;

public interface BundleService extends BaseService<Bundle> {

    public Bundle getBundleTemplateByName(final String name);

    public long addBundleTemplate(final Bundle bundle);

}
