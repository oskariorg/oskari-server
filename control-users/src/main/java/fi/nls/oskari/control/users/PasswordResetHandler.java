package fi.nls.oskari.control.users;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.control.users.model.Email;
import fi.nls.oskari.control.users.service.MailSenderService;
import fi.nls.oskari.control.users.service.UserRegistrationService;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.user.IbatisUserService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@OskariActionRoute("UserPasswordReset")
public class PasswordResetHandler extends RestActionHandler {

    private static final Logger log = LogFactory.getLogger(PasswordResetHandler.class);

    private static final String PARAM_UUID = "uuid";
    private static final String PARAM_EMAIL = "email";
    private static final String PARAM_PASSWORD = "password";

    private ObjectMapper mapper = new ObjectMapper();

    private UserRegistrationService registerTokenService = null;
    private final MailSenderService mailSenderService = new MailSenderService();
    private final IbatisUserService ibatisUserService = new IbatisUserService();
    private UserService userService;

    @Override
    public void init() {
        try {
            userService = UserService.getInstance();
        } catch (ServiceException se) {
            log.error(se, "Unable to initialize User service!");
        }
        registerTokenService = OskariComponentManager.getComponentOfType(UserRegistrationService.class);
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        if(!RegistrationUtil.isEnabled()) {
            throw new ActionDeniedException("Registration disabled");
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        // request link to set password
        String email = params.getUser().getEmail();
        if(params.getUser().isGuest()) {
            // use user provided email
            email = params.getRequiredParam(PARAM_EMAIL);
        }
        // validate email
        if(!RegistrationUtil.isValidEmail(email)) {
            throw new ActionParamsException("user.registration.error.invalidEmail");
        }
        String username = registerTokenService.findUsernameForEmail(email);
        if (username == null) {
            // no existing user with this email. Offer registration
            // TODO: Send an email "Did you try to reset password? There's no account for this email, but you can create one in here [link]"
            throw new ActionDeniedException("Username for login doesn't exist for email address: " + email);
        }
        // add or update token
        Email token = registerTokenService.setupToken(email);
        try {
            mailSenderService.sendEmailForResetPassword(email, token.getUuid(), RegistrationUtil.getServerAddress(params), params.getLocale().getLanguage());
        } catch (ServiceException ex) {
            throw new ActionException("Couldn't send email to user", ex);
        }

    }
    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        // set password
        final Email token = parseContentForEmailUpdate(params);
        String username = registerTokenService.findUsernameForEmail(token.getEmail());

        if (username == null) {
            throw new ActionParamsException("Username doesn't exist.");
        }
        String loginPassword = ibatisUserService.getPassword(username);
        try {
            if (loginPassword != null && !loginPassword.isEmpty()) {
                userService.updateUserPassword(username, token.getPassword());
            } else {
                // Create entry in oskari_jaas_user table
                // TODO: check that we want to allow this
                userService.setUserPassword(username, token.getPassword());
            }
            // After password updated/created, delete the entry related to token from database
            registerTokenService.deleteEmailToken(token.getUuid());
        } catch (ServiceException se) {
            throw new ActionException(se.getMessage(), se);
        }
    }

    public Email parseContentForEmailUpdate(ActionParameters params) throws ActionException {

        Email token = new Email();
        Map<String, String> jsonObjectMap = readJsonFromStream(params.getRequest());

        // JSON object ONLY need to have 2 attributes: 'uuid' and 'password'
        if (jsonObjectMap.size() != 2) {
            throw new ActionParamsException("JSON object MUST contain only 2 attributes: 'uuid' and 'password'");
        }
        token.setPassword(jsonObjectMap.get(PARAM_PASSWORD));
        token.setUuid(jsonObjectMap.get(PARAM_UUID));

        // validate
        if (token.getPassword() == null || token.getUuid() == null) {
            throw new ActionParamsException("JSON object MUST contain only 2 attributes: 'uuid' and 'password'");
        }
        Email tempEmail = registerTokenService.findByToken(token.getUuid());
        if (tempEmail == null) {
            throw new ActionParamsException("UUID not found.");
        }
        if (tempEmail.hasExpired()) {
            registerTokenService.deleteEmailToken(token.getUuid());
            throw new ActionDeniedException("UUID expired.");
        }
        token.setEmail(tempEmail.getEmail());
        return token;
    }

    /**
     * Checks if the username exists for login, for the email Address being sent.
     * 
     * @param emailAddress
     * @return
     * @throws ActionException
     */
    private final boolean isUsernameExistsForLogin(final String emailAddress) throws ActionException {
        // Retrieves username , if exists in oskari_users table.
        String username = registerTokenService.findUsernameForEmail(emailAddress);
        if (username == null) {
            return false;
        }

        // Retrieves username for login, if exists in oskari_jaas_users table.
        String loginUsername = registerTokenService.findUsernameForLogin(username);
        return (loginUsername != null);
    }

    /**
     * Reads JSON data from stream
     * 
     * @param request
     * @return
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @SuppressWarnings("unchecked")
    private final Map<String, String> readJsonFromStream(HttpServletRequest request) throws ActionException {
        try {
            return mapper.readValue(request.getInputStream(), HashMap.class);
        } catch (IOException e) {
            throw new ActionParamsException("Invalid JSON object received");
        }
    }

}
