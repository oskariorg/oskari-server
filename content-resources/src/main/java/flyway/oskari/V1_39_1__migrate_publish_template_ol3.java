package flyway.oskari;

import fi.nls.oskari.db.ViewHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONObject;

import java.sql.Connection;

public class V1_39_1__migrate_publish_template_ol3 implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_39_1__migrate_publish_template_ol3.class);
    private ViewService service = null;

    public void migrate(Connection connection)
            throws Exception {

        if(PropertyUtil.getOptional("flyway.1_39_1.skip", false)) {
            LOG.warn("You are skipping publish template migration.",
                    "All new development is happening for the Openlayers 3 based publish template",
                    "and having Openlayers 2 based template might not work properly anymore.",
                    "You will have to make an app specific migration since you skipped this one.");
            return;
        }
        service = new ViewServiceIbatisImpl();
        long tplId = getTemplateId();
        View template = service.getViewWithConf(tplId);
        if(template == null) {
            LOG.warn("Publish template couldn't be loaded with id", tplId, "- If you are populating an empty DB this is fine.",
                    "If you are migrating an old database you have misconfigured template property (", ViewService.PROPERTY_PUBLISH_TEMPLATE,
                    ") in oskari-ext.properties!");
            return;
        }

        Bundle mapfull = template.getBundleByName("mapfull");
        if (isOl3(mapfull)) {
            // already using ol3 template
            LOG.info("Already using Openlayers 3 based template, no need to migrate.");
            return;
        }
        // save template as new view for backup
        long backupId = service.addView(template);
        LOG.info("Previous publish template was saved as a backup with view id:", backupId,
                ". You can remove it or leave as is.");

        try {
            // load view from json and update startups for bundles
            final String file = PropertyUtil.get("flyway.1_39_1.file", "ol3-publisher-template-view-4326.json");
            JSONObject json = ViewHelper.readViewFile(file);
            View newTpl = ViewHelper.createView(json);
            newTpl.setId(tplId);
            newTpl.setApplication(PropertyUtil.get("flyway.1_39_1.application", newTpl.getApplication()));
            newTpl.setDevelopmentPath(PropertyUtil.get("flyway.1_39_1.path", newTpl.getDevelopmentPath()));
            newTpl.setPage(PropertyUtil.get("flyway.1_39_1.page", newTpl.getPage()));
            // loop bundles from template -> setup state & config to newTpl
            // skip ones that aren't in the new template
            for(Bundle b : template.getBundles()) {
                Bundle newB = newTpl.getBundleByName(b.getName());
                if(newB == null) {
                    continue;
                }
                newB.setConfig(b.getConfig());
                newB.setState(b.getState());
            }
            // updates page/app/path for view and all bundles
            service.updatePublishedView(newTpl);
        } catch (Exception e) {
            LOG.warn(e, "Something went wrong while updating the publish template!",
                    "Previous publish template was saved as a backup with view id:", backupId,
                    "The update failed so to keep going you can configure it as the new publish template to keep going.",
                    "You might need to manually update the publish template to use Openlayers 3 based map.");
            throw e;
        }
    }

    private long getTemplateId() {
        long id = PropertyUtil.getOptional(ViewService.PROPERTY_PUBLISH_TEMPLATE, -1);
        if (id == -1) {
            throw new RuntimeException("Publish template not configured");
        }
        return id;
    }

    private boolean isOl3(Bundle mapfull)
            throws Exception {
        JSONObject startup = JSONHelper.createJSONObject(mapfull.getStartup());
        JSONObject metadata = startup.optJSONObject("metadata");
        JSONObject imports = metadata.optJSONObject("Import-Bundle");
        JSONObject mapmodule = imports.optJSONObject("mapmodule");
        if (mapmodule != null) {
            // propably ol3
            String path = mapmodule.optString("bundlePath");
            if (!path.contains("packages/mapping/ol3")) {
                throw new Exception("Didn't detect either ol3 or ol2 mapmodule - cancel update");
            }
            // definately ol3
            return true;
        }
        // ol2 version is mapmodule-plugin from packages/framework/bundle/
        JSONObject mapmodulePlugin = imports.optJSONObject("mapmodule-plugin");
        if (mapmodulePlugin == null) {
            throw new Exception("Didn't detect either ol3 or ol2 mapmodule - cancel update");
        }
        String path = mapmodulePlugin.optString("bundlePath");
        // propably ol2 version
        if (!path.contains("packages/framework/bundle")) {
            throw new Exception("Didn't detect either ol3 or ol2 mapmodule - canceling update");
        }
        // was ol2
        return false;
    }

}
