package fi.nls.oskari.map.publish.service;

import fi.nls.oskari.map.publish.domain.TermsOfUse;
import fi.nls.oskari.service.OskariComponent;

public abstract class PublishTermsOfUseService extends OskariComponent {
    public abstract boolean setUserAgreed(final long userId);
    public abstract TermsOfUse findByUserId(final long userId);
}
