package fi.nls.oskari.control.users;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

/**
 * Handles user's password reseting
 */
@Controller
public class UserController {

    private final static Logger log = LogFactory.getLogger(UserController.class);
    
    public UserController() {
    	
    }
    
    /**
     * "passwordReset" jsp view should ALWAYS be used by user to reset password
     * @param model
     * @param uuid
     * @return
     */
    @RequestMapping("/resetPassword/{uuid}")
    public String resetPassword(
    		Model model,
    		@ModelAttribute String uuid) {
                   
         return "passwordReset";
    }
   
}

