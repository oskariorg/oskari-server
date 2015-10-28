package fi.nls.oskari.spring.security.saml;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.spring.security.OskariUserHelper;
import fi.nls.oskari.user.DatabaseUserService;
import fi.nls.oskari.util.PropertyUtil;
import org.opensaml.saml2.core.Attribute;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Maps SAMLCredentials to both Oskari and Spring security user
 */
@Service
public class OskariSAMLUserDetailsService implements SAMLUserDetailsService {

    private Logger log = LogFactory.getLogger(OskariSAMLUserDetailsService.class);

    private Map<String, String> attributeMapping = new HashMap<String, String>();

    private OskariUserMapper userMapper = null;

    public final static String PROP_UNIQUE_ID_ATTR_NAME = "oskari.saml.mapper.uniqueAttribute";
    public enum ATTRIBUTE {
        FIRSTNAME("firstname"),
        LASTNAME("lastname"),
        EMAIL("email");

        private String property;

        ATTRIBUTE(String prop) {
            property = prop;
        }
        public String getKey() {
            return property;
        }
    }

    public OskariSAMLUserDetailsService() {

        // mapping for saml attributes -> oskari user
        for(ATTRIBUTE a : ATTRIBUTE.values()) {
            final String mapping = PropertyUtil.getOptional("oskari.saml.credential." + a.getKey());
            if(mapping != null) {
                attributeMapping.put(a.getKey(), mapping);
            }
            else {
                attributeMapping.put(a.getKey(), a.getKey());
            }
        }

        try {
            getUserService();
        } catch (Exception ex) {
            log.error(ex, "Error getting UserService. Is it configured?");
        }
        userMapper = getUserMapper();
    }

    private OskariUserMapper getUserMapper() {
        final String mapperClassName = PropertyUtil.getOptional("oskari.saml.mapper");
        if(mapperClassName == null) {
            // no mapper specified
            return null;
        }
        try {
            final Class clazz = Class.forName(mapperClassName.trim());
            return (OskariUserMapper) clazz.newInstance();
        } catch (Exception e) {
            log.error(e, "Error loading SAML user mapper from classname:", mapperClassName);
        }
        return null;
    }

    @Override
    public Object loadUserBySAML(SAMLCredential credential)
            throws UsernameNotFoundException {
        String userID = credential.getNameID().getValue();

        log.info(userID + " logged in");
        log.info("Auth against: ", credential.getRemoteEntityID());
        log.info("Service provider:\n", credential.getLocalEntityID());
        log.info("Attributes:");
        for(Attribute attr : credential.getAttributes()) {
            log.info(attr.getName(), "=", credential.getAttributeAsString(attr.getName()), "\n");
        }
        try {
            final fi.nls.oskari.domain.User user = handleUser(credential);
            User userDetails = new User(user.getScreenname(), "N/A", true, true, true,
                true, OskariUserHelper.getRoles(user.getRoles()));
            return userDetails;
        } catch (Exception ex) {
            log.info(ex, "Error constructing user details");
            throw new UsernameNotFoundException("Couldn't handle user correctly");
        }
    }

    /**
     * Tries to handle transient name ids gracefully.
     * @param credential
     * @return
     * @throws Exception
     */
    protected fi.nls.oskari.domain.User determineUniqueId(SAMLCredential credential) throws Exception {
        final String userID = credential.getNameID().getValue();
        final fi.nls.oskari.domain.User user = new fi.nls.oskari.domain.User();
        log.debug("NameID format:", credential.getNameID().getFormat(), "value:", userID);
        final boolean isTransientNameID = credential.getNameID().getFormat().indexOf("transient") != -1;
        final String uniqueIDAttributeName = PropertyUtil.getOptional(PROP_UNIQUE_ID_ATTR_NAME);
        if(uniqueIDAttributeName != null) {
            user.setScreenname(credential.getAttributeAsString(uniqueIDAttributeName));
        }
        else {
            final String email = credential.getAttributeAsString(attributeMapping.get(ATTRIBUTE.EMAIL.getKey()));
            if(isTransientNameID) {
                if(email != null && !email.isEmpty()) {
                    user.setScreenname(email);
                }
                else {
                    // tried to get it before, now throw an exception saying it's necessary.
                    PropertyUtil.getNecessary(PROP_UNIQUE_ID_ATTR_NAME,
                            "Couldn't find a unique id for user automatically so this property should be used to configure an attribute to use as one.");
                }
            } else {
                user.setScreenname(userID);
            }
        }
        if(user.getScreenname() == null || user.getScreenname().isEmpty()) {
            throw new UsernameNotFoundException("Couldn't determine unique id for user");
        }
        return user;
    }

    public fi.nls.oskari.domain.User handleUser(SAMLCredential credential) throws Exception {
        // load Oskari user from UserService to determine roles
        final DatabaseUserService service = getUserService();

        // IDP might respond with message including a session id but not attributes.
        // TODO: Handle a message with no attributes...
        final fi.nls.oskari.domain.User user = determineUniqueId(credential);

        // mapping SAML data
        user.setFirstname(credential.getAttributeAsString(attributeMapping.get(ATTRIBUTE.FIRSTNAME.getKey())));
        user.setLastname(credential.getAttributeAsString(attributeMapping.get(ATTRIBUTE.LASTNAME.getKey())));
        user.setEmail(credential.getAttributeAsString(attributeMapping.get(ATTRIBUTE.EMAIL.getKey())));
        log.debug("Email attribute name should be '" + attributeMapping.get(ATTRIBUTE.EMAIL.getKey()) + "'. Value is:", user.getEmail());

        // save all non-processed attribute data to user attributes
        final Set<String> processedAttributes = new HashSet<>();
        processedAttributes.add(attributeMapping.get(ATTRIBUTE.FIRSTNAME.getKey()));
        processedAttributes.add(attributeMapping.get(ATTRIBUTE.LASTNAME.getKey()));
        processedAttributes.add(attributeMapping.get(ATTRIBUTE.EMAIL.getKey()));
        for(Attribute attr: credential.getAttributes()) {
            final String key = attr.getName();
            if(!processedAttributes.contains(key)) {
                user.setAttribute(key, credential.getAttributeAsString(key));
            }
        }

        if(user.getRoles().isEmpty()) {
            user.addRole(Role.getDefaultUserRole());
        }
        // hook for custom parsing
        if(userMapper != null) {
            userMapper.mapUser(credential, user);
        }
        log.debug("Saving user:", user, "with roles:", user.getRoles());
        final fi.nls.oskari.domain.User savedUser = service.saveUser(user);
        log.debug("User saved as:", savedUser, "with roles:", savedUser.getRoles());
        return savedUser;
    }


    private DatabaseUserService getUserService() throws UsernameNotFoundException {
        try {
            return (DatabaseUserService)UserService.getInstance();
        } catch (Exception ex) {
            throw new UsernameNotFoundException("Couldn't get UserService or it's not assignable to " + DatabaseUserService.class.getName());
        }
    }

}
