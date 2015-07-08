package fi.nls.oskari.spring.security.saml;

import fi.nls.oskari.domain.User;
import org.springframework.security.saml.SAMLCredential;

/**
 * Handler interface for mapping users from SAML credentials. Implement your own SAML to Oskari user mapping
 * by implementing mapUser().
 * Configured to be used in oskari-ext.properties:
 * - oskari.saml.mapper=[fqcn for class implementing OskariUserMapper]
 */
public interface OskariUserMapper {
    public void mapUser(SAMLCredential credential, User prepopulatedUser) throws Exception;
}
