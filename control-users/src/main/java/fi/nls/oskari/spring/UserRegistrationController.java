package fi.nls.oskari.spring;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.users.model.Email;
import fi.nls.oskari.control.users.service.UserRegistrationService;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.spring.extension.OskariParam;
import fi.nls.oskari.util.PropertyUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * Handles user's password reseting
 */
@Controller
@RequestMapping("/user")
public class UserRegistrationController {

    private final static Logger log = LogFactory.getLogger(UserRegistrationController.class);
    
    private static final String ERR_TOKEN_INVALID = "Token is invalid.";
    private static final String ERR_TOKEN_NOT_FOUND = "Token is unavailable.";

    private boolean isRegistrationAllowed() {
        return PropertyUtil.getOptional("allow.registration", false);
    }

    @RequestMapping
    public String index(Model model, @OskariParam ActionParameters params) {
        if(!isRegistrationAllowed()) {
            return "error/404";
        }
        User user = params.getUser();
        boolean isGuest = user.isGuest();
        model.addAttribute("editExisting", !isGuest);
        if(!isGuest) {
            model.addAttribute("firstname", user.getFirstname());
            model.addAttribute("lastname", user.getLastname());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("id", user.getId());
        }
        return "register";
    }


    @RequestMapping("/reset")
    public String forgotPassword(Model model, @OskariParam ActionParameters params) {
        if(!isRegistrationAllowed()) {
            return "error/404";
        }
        User user = params.getUser();
        if(!user.isGuest()) {
            model.addAttribute("email", user.getEmail());
        }
        return "forgotPasswordEmail";
    }
    /**
     * "passwordReset" jsp view should ALWAYS be used by user to reset password
     * @param model
     * @param uuid is email token number for reseting password.
     * @return jsp view , which includes 2 attributes: uuid (token number) and error (error message).
     */
    @RequestMapping("/reset/{uuid}")
    public String resetPassword(Model model, @PathVariable String uuid) {
        if(!isRegistrationAllowed()) {
            return "error/404";
        }
        final String jspView = "passwordReset";
        UserRegistrationService emailService = OskariComponentManager.getComponentOfType(UserRegistrationService.class);
        Email email = emailService.findByToken(uuid);
        if (email == null) {
            log.debug("Email token not found, going to error/404");
            model.addAttribute("uuid", null);
            model.addAttribute("error", ERR_TOKEN_NOT_FOUND);
            return jspView;
        }
        // Check if email token has valid date or not.
        if (new Date().before(email.getExpiryTimestamp())) {
            model.addAttribute("uuid", email.getUuid());
        }
        else{
            model.addAttribute("uuid", null);
            model.addAttribute("error", ERR_TOKEN_INVALID);
        }

        return jspView;
    }
   
}

