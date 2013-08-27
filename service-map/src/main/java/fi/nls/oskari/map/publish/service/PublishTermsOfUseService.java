package fi.nls.oskari.map.publish.service;

import fi.nls.oskari.map.publish.domain.TermsOfUse;
import fi.nls.oskari.service.db.BaseService;

public interface PublishTermsOfUseService extends BaseService<TermsOfUse> {
    public int setUserAgreed(final long userId);
    public TermsOfUse findByUserId(final long userId);
}
