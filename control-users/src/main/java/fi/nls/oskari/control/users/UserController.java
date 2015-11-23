package fi.nls.oskari.control.users;

import java.util.Date;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import fi.nls.oskari.control.users.model.Email;
import fi.nls.oskari.control.users.service.IbatisEmailService;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;

/**
 * Handles user's password reseting
 */
@Controller
public class UserController {

    private final static Logger log = LogFactory.getLogger(UserController.class);
    
    private static final String ERR_TOKEN_INVALID = "Token is invalid";
    
    public UserController() {
    	
    }
    
    /**
     * "passwordReset" jsp view should ALWAYS be used by user to reset password
     * @param model
     * @param uuid
     * @return
     * @throws ServiceException
     */
    @RequestMapping("/resetPassword/{uuid}")
    public ModelAndView resetPassword(Model model, @PathVariable String uuid) throws ServiceException {
         IbatisEmailService emailService = new IbatisEmailService();
         Email email = emailService.findByToken(uuid);
         if (email == null)
        	 throw new ServiceException(ERR_TOKEN_INVALID);
         
         ModelAndView mv = new ModelAndView("passwordReset");
         if (email.getExpiryTimestamp().after(new Date())) {
             mv.addObject("uuid", email.getUuid());
         }
         else{
        	 
         }
        
         return mv;
    }
   
}

