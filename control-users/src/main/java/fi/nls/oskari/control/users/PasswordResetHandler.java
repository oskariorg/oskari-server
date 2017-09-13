package fi.nls.oskari.control.users;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.control.users.model.EmailToken;
import fi.nls.oskari.control.users.model.PasswordRules;
import fi.nls.oskari.control.users.service.MailSenderService;
import fi.nls.oskari.control.users.service.UserRegistrationService;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.spring.SpringContextHolder;
import fi.nls.oskari.user.MybatisUserService;
import fi.nls.oskari.util.ResponseHelper;
import org.springframework.context.MessageSource;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@OskariActionRoute("UserPasswordReset")
public class PasswordResetHandler extends RestActionHandler {

    private static final Logger LOG = LogFactory.getLogger(PasswordResetHandler.class);

    private static final String PARAM_UUID = "uuid";
    private static final String PARAM_EMAIL = "email";
    private static final String PARAM_PASSWORD = "password";

    private ObjectMapper mapper = new ObjectMapper();
    private MessageSource messages;

    private UserRegistrationService registerTokenService = null;
    private final MailSenderService mailSenderService = new MailSenderService();
    private final MybatisUserService mybatisUserService = new MybatisUserService();
    private UserService userService;

    @Override
    public void init() {
        try {
            userService = UserService.getInstance();
        } catch (ServiceException se) {
            LOG.error(se, "Unable to initialize User service!");
        }
        registerTokenService = OskariComponentManager.getComponentOfType(UserRegistrationService.class);
    }

    private MessageSource getMessages() {
        if(messages == null) {
            // "manual autowire"
            messages = SpringContextHolder.getBean(MessageSource.class);

        }
        return messages;
    }
    private String getMessage(String key, String language) {
        return getMessages().getMessage(key, new String[]{}, new Locale(language));
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        if(!RegistrationUtil.isEnabled()) {
            throw new ActionDeniedException("Registration disabled");
        }
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        try {
            ResponseHelper.writeResponse(params, mapper.writeValueAsString(PasswordRules.asMap()));
        } catch (JsonProcessingException e) {
            ResponseHelper.writeError(params, "Couldn't serialize requirements");
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
            throw new ActionParamsException(getMessage("user.registration.error.invalidEmail", params.getLocale().getLanguage()));
        }
        // add or update token
        EmailToken token = registerTokenService.setupToken(email);
        String username = registerTokenService.findUsernameForEmail(email);
        try {
            if (username == null) {
                // no existing user with this email. Offer registration
                LOG.info("User tried to reset password for unknown account:", email);

                // Send an email "Did you try to reset password? There's no account for this email, but you can create one in here [link]"
                mailSenderService.sendEmailForPasswordResetWithoutAccount(email, token.getUuid(), RegistrationUtil.getServerAddress(params), params.getLocale().getLanguage());
                LOG.info("Offer to register sent to:", email);
            } else {
                mailSenderService.sendEmailForResetPassword(email, token.getUuid(), RegistrationUtil.getServerAddress(params), params.getLocale().getLanguage());
                LOG.info("Reset password email sent to:", email);
            }
        } catch (ServiceException ex) {
            throw new ActionException(getMessage("user.registration.error.sendingFailed", params.getLocale().getLanguage()), ex);
        }
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        // set password
        final EmailToken token = parseContentForEmailUpdate(params);
        String username = registerTokenService.findUsernameForEmail(token.getEmail());

        if(!RegistrationUtil.isPasswordOk(token.getPassword())) {
            throw new ActionParamsException("Password too weak");
        }
        if (username == null) {
            LOG.warn("User tried to set password for unknown account:", token.getEmail());
            throw new ActionParamsException("Username doesn't exist.");
        }
        String loginPassword = mybatisUserService.getPassword(username);
        try {
            LOG.warn("Setting password for user:", token.getEmail());
            if (loginPassword != null && !loginPassword.isEmpty()) {
                userService.updateUserPassword(username, token.getPassword());
            } else {
                // Create entry in oskari_jaas_user table
                userService.setUserPassword(username, token.getPassword());
            }
            // After password updated/created, delete the entry related to token from database
            registerTokenService.removeTokenByUUID(token.getUuid());
        } catch (ServiceException se) {
            throw new ActionException(se.getMessage(), se);
        }
    }

    public EmailToken parseContentForEmailUpdate(ActionParameters params) throws ActionException {

        EmailToken token = new EmailToken();
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
        EmailToken tempEmailToken = registerTokenService.findByToken(token.getUuid());
        if (tempEmailToken == null) {
            throw new ActionParamsException("UUID not found.");
        }
        if (tempEmailToken.hasExpired()) {
            registerTokenService.removeTokenByUUID(token.getUuid());
            throw new ActionDeniedException("UUID expired.");
        }
        token.setEmail(tempEmailToken.getEmail());
        return token;
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
