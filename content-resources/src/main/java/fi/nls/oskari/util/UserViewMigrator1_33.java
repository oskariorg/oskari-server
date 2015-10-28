package fi.nls.oskari.util;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;

import java.util.List;

/**
 * Created by SMAKINEN on 12.10.2015.
 */
public class UserViewMigrator1_33 {

    private static final Logger LOG = LogFactory.getLogger(UserViewMigrator1_33.class);
    private static final int BATCH_SIZE = 50;

    private int updatedViewCount = 0;
    private ViewService service = null;
    private View template = null;

    /**
     * The main method to call for generating metadata for all published views/appsetups
     * @param templateViewId This should be a view id for system default view used for authenticated users.
     *                       It will be used as template for other USER views
     * @throws Exception
     */
    public void migrateUserViewsAppsetups(ViewService service, long templateViewId)
            throws Exception {
        this.service = service;
        template = service.getViewWithConf(templateViewId);
        if(template == null) {
            throw new Exception("Couldn't find template view with id: " + templateViewId);
        }
        int page = 1;
        while (updateViews(page)) {
            page++;
        }
        LOG.info("Updated views:", updatedViewCount);
        template = null;
        this.service = null;
    }

    private boolean updateViews(int page)
            throws Exception {
        List<View> list = service.getViews(page, BATCH_SIZE);
        LOG.info("Got", list.size(), "views on page", page);
        for (View view : list) {
            if (!ViewTypes.USER.equals(view.getType())) {
                // only interested in USER views
                continue;
            }
            if(view.getId() == template.getId()) {
                // same view, no need to update it
                continue;
            }
            if(!view.getApplication().equals(template.getApplication())) {
                LOG.warn("Skipping view! User view has different application than template: ",
                        view.getApplication(), "<>", template.getApplication(), "ID:", view.getId());
                continue;
            }
            View updated = template.cloneBasicInfo();
            // override all info with current views. We are interested only in bundle setup.
            updated.setId(view.getId());
            updated.setOldId(view.getOldId());
            updated.setUuid(view.getUuid());
            updated.setName(view.getName());
            updated.setDescription(view.getDescription());
            updated.setType(view.getType());
            updated.setCreator(view.getCreator());
            updated.setIsPublic(view.isPublic());
            updated.setLang(view.getLang());
            updated.setPubDomain(view.getPubDomain());
            updated.setIsDefault(view.isDefault());

            // loop bundles and setup states
            for(Bundle b : view.getBundles()) {
                Bundle updBundle = updated.getBundleByName(b.getName());
                if(updBundle != null) {
                    updBundle.setState(b.getState());
                }
            }

            // reset sequence, shouldn't affect order but removes possible duplicates
            int seqNo = 0;
            for(Bundle b : updated.getBundles()) {
                b.setSeqNo(++seqNo);
            }

            service.updatePublishedView(updated);
            updatedViewCount++;
            LOG.info("Updated view:", view.getId(), "Updates count:", updatedViewCount);
        }
        return list.size() == BATCH_SIZE;
    }

}
