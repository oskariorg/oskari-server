package fi.nls.oskari.control.users;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.crypto.bcrypt.BCrypt;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.users.model.Email;
import fi.nls.oskari.control.users.service.IbatisEmailService;
import fi.nls.oskari.control.users.service.MailSenderService;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.user.IbatisRoleService;
import fi.nls.oskari.user.IbatisUserService;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("UserPasswordReset")
public class PasswordResetHandler extends ActionHandler {
	
	private static final Logger log = LogFactory.getLogger(PasswordResetHandler.class);
	
	private static final String PARAM_UUID = "uuid";
	private static final String PARAM_EMAIL = "email";
	private static final String PARAM_SET_PASSWORD = "setPassword";
	private static final String PARAM_PASSWORD = "password";
		
	private static final String ROLE_USER = "User";
	
	private final IbatisEmailService emailService = new IbatisEmailService();
	private final MailSenderService mailSenderService = new MailSenderService();
	private final IbatisUserService userService = new IbatisUserService();
	
    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        // TODO should only handle POST requests (but best to do that last, it is easier to develop/debug with GET requests)
    	
    	String requestEmail = params.getRequest().getParameter(PARAM_EMAIL);
    	
    	if (requestEmail != null && !requestEmail.isEmpty()) {
    		if (isUsernameExistsForLogin(requestEmail)) {
    			String uuid = UUID.randomUUID().toString();
            	Email emailToken = new Email();
            	emailToken.setEmail(requestEmail);
            	emailToken.setUuid(uuid);
            	emailToken.setExpiryTimestamp(createExpiryTime());
            	emailService.addEmail(emailToken);
            	
            	String username = emailService.findUsernameForEmail(requestEmail);
            	User user = userService.findByUserName(username);
            	mailSenderService.sendEmailForResetPassword(user, uuid, params.getRequest());
            	            	
    		} else {
    			log.info("Username for login doesn't exist for email address: " + requestEmail);
    			return;
    		}
            
    	} else if (params.getRequest().getParameter(PARAM_SET_PASSWORD) != null) {
    		Email token = new Email();
    		Map<String, String> jsonObjectMap;
            try {
                jsonObjectMap = readJsonFromStream(params.getRequest());
            } catch (IOException e) {
                ResponseHelper.writeError(params, "Invalid JSON object received");
                return;
            }
			
			//JSON object ONLY need to have 2 attributes: 'uuid' and 'password'
            if (jsonObjectMap.size() != 2) {
            	 ResponseHelper.writeError(params, "JSON object MUST contain only 2 attributes:"
            	 		+ " 'uuid' and 'password'");
            	 return;
            }
            for (Map.Entry<String, String> entry : jsonObjectMap.entrySet()) {
            	if(entry.getKey().equals(PARAM_PASSWORD) || entry.getKey().equals(PARAM_UUID)) {
            		if(entry.getKey().equals(PARAM_PASSWORD)) {
	        			token.setPassword(entry.getValue());
            		} else {
            			String uuid = entry.getValue();
            			Email tempEmail = emailService.findByToken(uuid);
            			if(tempEmail == null)
            				throw new ActionException("UUID is not found.");
            			
            			if(tempEmail.getExpiryTimestamp().after(new Date())) {
            				token.setUuid(uuid);
            				token.setEmail(tempEmail.getEmail());
            			} else {
            				 ResponseHelper.writeError(params, "Invalid UUID token");
            				 return;
            			}
            		}
            	} else {
            		 ResponseHelper.writeError(params, "JSON object MUST contain attributes: "
            		 		+ "'uuid' and 'password'.");
	            	 return;
            	}
        	}
			
			String username = emailService.findUsernameForEmail(token.getEmail());
			
			if (username == null) {
				throw new ActionException("Username doesn't exist.");
			} else {
				String loginPassword = userService.getPassword(username);				
				final String hashedPass = BCrypt.hashpw(token.getPassword(), BCrypt.gensalt());
				if (loginPassword != null && !loginPassword.isEmpty()) {
					userService.updatePassword(username, hashedPass);
				} else {
					//Create entry in oskari_jaas_user table
					userService.setPassword(username, hashedPass);
					
					//Create link between User and Role (oskari_role_oskari_user); For logged user's default view.
					User user = userService.findByUserName(username);
					int roleId = emailService.findUserRoleId(ROLE_USER);
					IbatisRoleService roleService = new IbatisRoleService();
					roleService.linkRoleToNewUser(roleId, user.getId());
				}
				//After password updated/created, delete the entry related to token from database
				emailService.deleteEmailToken(token.getUuid());
			}			
    	}
    	else {
    	    throw new ActionException("Request must contain either " + PARAM_EMAIL + " or " + PARAM_SET_PASSWORD + ".");
    	}
    }
    
    /**
     * Create timestamp for 2 days as expirytime.
     * @return
     */
    public Timestamp createExpiryTime(){
    	Calendar calender = Calendar.getInstance();
        Timestamp currentTime = new java.sql.Timestamp(calender.getTime().getTime());
        calender.setTime(currentTime);
        calender.add(Calendar.DAY_OF_MONTH, 2);
        Timestamp expiryTime = new java.sql.Timestamp(calender.getTime().getTime());
        return expiryTime;
    }
    
    /**
     * Checks if the username exists for login, for the email Address being sent.
     * @param emailAddress
     * @return
     * @throws ActionException
     */
    private final boolean isUsernameExistsForLogin(final String emailAddress) throws ActionException {
    	// Retrieves username , if exists in oskari_users table.
    	String username = emailService.findUsernameForEmail(emailAddress);
    	if (username == null) {
    		return false;
    	}
    	
    	// Retrieves username for login, if exists in oskari_jaas_users table.
    	String loginUsername = emailService.findUsernameForLogin(username);
    	return (loginUsername != null);
    }
    
    /**
     * Reads JSON data from stream
     * @param request
     * @return
     * @throws IOException 
     * @throws JsonMappingException 
     * @throws JsonParseException 
     */
    @SuppressWarnings("unchecked")
    private final Map<String, String> readJsonFromStream(HttpServletRequest request) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(request.getInputStream(), HashMap.class);
    }
    
}
