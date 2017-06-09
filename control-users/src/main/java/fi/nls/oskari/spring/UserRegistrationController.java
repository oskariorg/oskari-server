package fi.nls.oskari.spring;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.users.RegistrationUtil;
import fi.nls.oskari.control.users.model.Email;
import fi.nls.oskari.control.users.service.MailSenderService;
import fi.nls.oskari.control.users.service.UserRegistrationService;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.spring.extension.OskariParam;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;
import java.util.UUID;

/**
 * Handles user's password reseting
 */
@Controller
@RequestMapping("/user")
public class UserRegistrationController {

    private final static Logger LOG = LogFactory.getLogger(UserRegistrationController.class);

    private static final String ERR_TOKEN_INVALID = "Token is invalid.";
    private static final String ERR_TOKEN_NOT_FOUND = "user.registration.error.uuid";
    private static final String ERR_EXPECTED_GUEST = "user.registration.error.loggedIn";
    private final MailSenderService mailSenderService = new MailSenderService();

    @RequestMapping(method = RequestMethod.GET)
    public String index(Model model, @OskariParam ActionParameters params) {
        if (!RegistrationUtil.isEnabled()) {
            return "error/404";
        }
        User user = params.getUser();
        boolean isGuest = user.isGuest();
        if (isGuest) {
            return "init_registration";
        }
        model.addAttribute("firstname", user.getFirstname());
        model.addAttribute("lastname", user.getLastname());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("username", user.getScreenname());
        model.addAttribute("id", user.getId());
        return "profile";
    }

    private UserRegistrationService getService() {
        return OskariComponentManager.getComponentOfType(UserRegistrationService.class);
    }

    private final boolean isEmailRegistered(final String emailAddress) {
        return  getService().findUsernameForEmail(emailAddress) != null;
    }

    private boolean isValidEmail(String email) {
        // TODO: validate email syntax
        return email != null && !email.isEmpty();
    }

    @RequestMapping(method = RequestMethod.POST)
    public String initRegistration(Model model, @OskariParam ActionParameters params) {
        if (!RegistrationUtil.isEnabled()) {
            return "error/404";
        }

        if(!params.getUser().isGuest()) {
            model.addAttribute("error", ERR_EXPECTED_GUEST);
            return "init_registration";
        }
        User user = new User();
        user.setScreenname("");
        user.setEmail(params.getHttpParam("email"));
        if(!isValidEmail(user.getEmail())) {
            model.addAttribute("error", "user.registration.error.invalidEmail");
            return "init_registration";
        }
        String language = params.getLocale().getLanguage();
        UserRegistrationService service = getService();
        if (isEmailRegistered(user.getEmail())) {
            try {
                mailSenderService.sendEmailAlreadyExists(user, RegistrationUtil.getServerAddress(params), language);
                model.addAttribute("msg", "user.registration.email.sent");
            } catch (ServiceException se) {
                //Do nothing, email already exists and tried to send email about that failed.
                model.addAttribute("error", "user.registration.error.generic");
            }
            // don't bleed out the information, "successful" from user perspective but send out a mail about already registered user
            return "init_registration";
        }
        Email emailToken = service.findTokenByEmail(user.getEmail());
        if(emailToken != null) {
            // refresh token expiry if one exists
            emailToken.setUuid( UUID.randomUUID().toString());
            emailToken.setExpiryTimestamp(RegistrationUtil.createExpiryTime());
            service.updateToken(emailToken);
        } else {
            // create a new token
            emailToken = new Email();
            emailToken.setEmail(user.getEmail());
            emailToken.setScreenname("");
            emailToken.setUuid( UUID.randomUUID().toString());
            emailToken.setExpiryTimestamp(RegistrationUtil.createExpiryTime());
            service.addToken(emailToken);
        }

        try {
            mailSenderService.sendEmailForRegistrationActivation(user.getEmail(), emailToken.getUuid(), RegistrationUtil.getServerAddress(params), language);
        } catch (ServiceException se) {
            LOG.error(se, "Error sending email", emailToken);
            model.addAttribute("error", "user.registration.error.generic");
        }
        model.addAttribute("msg", "user.registration.email.sent");
        return "init_registration";
    }

    @RequestMapping("/{uuid}")
    public String register(Model model, @PathVariable String uuid, @OskariParam ActionParameters params) {
        if (!RegistrationUtil.isEnabled()) {
            return "error/404";
        }
        UserRegistrationService emailService = OskariComponentManager.getComponentOfType(UserRegistrationService.class);
        Email email = emailService.findByToken(uuid);
        if (email == null) {
            // go back to registration start with an error message
            LOG.debug("Email token not found, going to registration start");
            model.addAttribute("error", ERR_TOKEN_NOT_FOUND);
            return "init_registration";
        }
        User user = params.getUser();
        if (!user.isGuest()) {
            if(user.getEmail().equalsIgnoreCase(email.getEmail())) {
                emailService.deleteEmailToken(uuid);
            }
            return index(model, params);
        }
        model.addAttribute("uuid", uuid);
        model.addAttribute("email", email.getEmail());
        return "new_user";
    }


    @RequestMapping("/reset")
    public String forgotPassword(Model model, @OskariParam ActionParameters params) {
        if (!RegistrationUtil.isEnabled()) {
            return "error/404";
        }
        User user = params.getUser();
        if (!user.isGuest()) {
            model.addAttribute("email", user.getEmail());
        }
        return "forgotPasswordEmail";
    }

    /**
     * "passwordReset" jsp view should ALWAYS be used by user to reset password
     *
     * @param model
     * @param uuid  is email token number for reseting password.
     * @return jsp view , which includes 2 attributes: uuid (token number) and error (error message).
     */
    @RequestMapping("/reset/{uuid}")
    public String resetPassword(Model model, @PathVariable String uuid) {
        if (!RegistrationUtil.isEnabled()) {
            return "error/404";
        }
        final String jspView = "passwordReset";
        UserRegistrationService emailService = OskariComponentManager.getComponentOfType(UserRegistrationService.class);
        Email email = emailService.findByToken(uuid);
        if (email == null) {
            LOG.debug("Email token not found, going to error/404");
            model.addAttribute("uuid", null);
            model.addAttribute("error", ERR_TOKEN_NOT_FOUND);
            return jspView;
        }
        // Check if email token has valid date or not.
        if (new Date().before(email.getExpiryTimestamp())) {
            model.addAttribute("uuid", email.getUuid());
        } else {
            model.addAttribute("uuid", null);
            model.addAttribute("error", ERR_TOKEN_INVALID);
        }

        return jspView;
    }

}

