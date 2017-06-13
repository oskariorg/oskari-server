package fi.nls.oskari.spring;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.users.RegistrationUtil;
import fi.nls.oskari.control.users.model.EmailToken;
import fi.nls.oskari.control.users.model.PasswordRules;
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

/**
 * Handles user's password reseting
 */
@Controller
@RequestMapping("/user")
public class UserRegistrationController {

    private final static Logger LOG = LogFactory.getLogger(UserRegistrationController.class);

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

    @RequestMapping(method = RequestMethod.POST)
    public String initRegistration(Model model, @OskariParam ActionParameters params) {
        if (!RegistrationUtil.isEnabled()) {
            return "error/404";
        }

        if(!params.getUser().isGuest()) {
            LOG.warn("Logged in user (", params.getUser().getEmail(), ") tried posting on registration form:", params.getHttpParam("email"));
            model.addAttribute("error", ERR_EXPECTED_GUEST);
            return "init_registration";
        }
        User user = new User();
        user.setScreenname("");
        user.setEmail(params.getHttpParam("email"));
        if(!RegistrationUtil.isValidEmail(user.getEmail())) {
            model.addAttribute("error", "user.registration.error.invalidEmail");
            return "init_registration";
        }
        String language = params.getLocale().getLanguage();
        UserRegistrationService service = getService();
        if (isEmailRegistered(user.getEmail())) {
            LOG.info("User tried to register with email that already has account:", user.getEmail());
            try {
                mailSenderService.sendEmailAlreadyExists(user.getEmail(), RegistrationUtil.getServerAddress(params), language);
                model.addAttribute("msg", "user.registration.email.sent");
            } catch (ServiceException se) {
                //Do nothing, account already exists and tried to send email about that failed.
                model.addAttribute("error", "user.registration.error.generic");
            }
            // don't bleed out the information, "successful" from user perspective, but send out a mail about already registered user
            return "init_registration";
        }
        EmailToken emailTokenToken = service.setupToken(user.getEmail());

        try {
            mailSenderService.sendEmailForRegistrationActivation(user.getEmail(), emailTokenToken.getUuid(), RegistrationUtil.getServerAddress(params), language);
            model.addAttribute("msg", "user.registration.email.sent");
        } catch (ServiceException se) {
            LOG.error(se, "Error sending email", emailTokenToken);
            model.addAttribute("error", "user.registration.error.sendingFailed");
        }
        return "init_registration";
    }

    @RequestMapping("/{uuid}")
    public String register(Model model, @PathVariable String uuid, @OskariParam ActionParameters params) {
        if (!RegistrationUtil.isEnabled()) {
            return "error/404";
        }
        UserRegistrationService emailService = OskariComponentManager.getComponentOfType(UserRegistrationService.class);
        EmailToken emailToken = emailService.findByToken(uuid);
        if (emailToken == null) {
            // go back to registration start with an error message
            LOG.debug("Email token not found, going to registration init:", uuid);
            model.addAttribute("error", ERR_TOKEN_NOT_FOUND);
            return "init_registration";
        }
        // Check if email token has valid date or not.
        if (emailToken.hasExpired()) {
            LOG.info("User accessed link after token expiration:", emailToken.getEmail());
            emailService.removeTokenByUUID(emailToken.getUuid());
            model.addAttribute("error", ERR_TOKEN_NOT_FOUND);
            return "init_registration";
        }
        User user = params.getUser();
        if (!user.isGuest()) {
            // not guest
            if(user.getEmail().equalsIgnoreCase(emailToken.getEmail())) {
                emailService.removeTokenByUUID(uuid);
            } else {
                LOG.warn("User", user.getEmail(), " is trying to access another accounts token:", emailToken.getEmail(), " - ", emailToken.getUuid());
            }
            return index(model, params);
        }
        model.addAttribute("uuid", uuid);
        model.addAttribute("requirements", PasswordRules.asMap());
        model.addAttribute("email", emailToken.getEmail());
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
        UserRegistrationService emailService = OskariComponentManager.getComponentOfType(UserRegistrationService.class);
        EmailToken emailToken = emailService.findByToken(uuid);
        if (emailToken == null) {
            LOG.debug("Email token not found for reseting password:", uuid);
            model.addAttribute("error", ERR_TOKEN_NOT_FOUND);
            return "forgotPasswordEmail";
        }
        // Check if email token has valid date or not.
        if (emailToken.hasExpired()) {
            LOG.debug("Email token expired for reseting password:", uuid, "-", emailToken.getEmail());
            emailService.removeTokenByUUID(emailToken.getUuid());
            model.addAttribute("error", ERR_TOKEN_NOT_FOUND);
            return "forgotPasswordEmail";
        }
        model.addAttribute("uuid", emailToken.getUuid());
        model.addAttribute("requirements", PasswordRules.asMap());
        return "passwordReset";

    }

}

