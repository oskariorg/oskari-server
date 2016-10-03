package fi.nls.oskari.control.users;

import java.util.Date;

import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.util.PropertyUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import fi.nls.oskari.control.users.model.Email;
import fi.nls.oskari.control.users.service.IbatisEmailService;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

/**
 * Handles user's password reseting
 */
@Controller
public class UserController {

    private final static Logger log = LogFactory.getLogger(UserController.class);
    
    private static final String ERR_TOKEN_INVALID = "Token is invalid.";
    private static final String ERR_TOKEN_NOT_FOUND = "Token is unavailable.";

    @RequestMapping("/register")
    public String register() {
        return "register";
    }
    /**
     * "passwordReset" jsp view should ALWAYS be used by user to reset password
     * @param model
     * @param uuid is email token number for reseting password.
     * @return jsp view , which includes 2 attributes: uuid (token number) and error (error message).      
     */
    @RequestMapping("/resetPassword/{uuid}")
    public String resetPassword(Model model, @PathVariable String uuid) {
        if(!PropertyUtil.getOptional("allow.registration", false)) {
            return "error/404";
        }
    	 final String jspView = "passwordReset";
         IbatisEmailService emailService = new IbatisEmailService();
         Email email = emailService.findByToken(uuid);
         if (email == null) {
        	 log.debug("Email token not found, going to error/404");
        	 model.addAttribute("uuid", null);
        	 model.addAttribute("error", ERR_TOKEN_NOT_FOUND);
        	 return jspView;
         }
         // Check if email token has valid date or not.
         if (email.getExpiryTimestamp().after(new Date())) {
             model.addAttribute("uuid", email.getUuid());
         }
         else{
        	 model.addAttribute("uuid", null);
        	 model.addAttribute("error", ERR_TOKEN_INVALID);
         }
        
         return jspView;
    }
   
}

