package fi.nls.oskari.control.users;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.bcrypt.BCrypt;

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
	
	private String requestEmail = "";
	
	private static final String PARAM_UUID = "uuid";
	private static final String PARAM_EMAIL = "email";
	private static final String PARAM_PASSWORD = "password";
		
	private static final String ROLE_USER = "User";
	
	private final IbatisEmailService emailService = new IbatisEmailService();
	private final MailSenderService mailSenderService = new MailSenderService();
	
    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        // TODO should only handle POST requests (but best to do that last, it is easier to develop/debug with GET requests)
        // TODO parse email address from params
        // TODO create password reset token for the email, store it in the database and send a reset link to the address (only if such address is in database)
        // Return SUCCESS status. Do this even if nothing was sent because client should not be allowed to know whether the
        // provided email address exists in the database.
    	
    	if (getRequestParameterCount(params.getRequest().getQueryString()) != 1)
			throw new ActionException("Request URL must contain ONLY ONE parameter.");
    	
    	if(!isParameterValid(params))
			throw new ActionException("Request URL must contain valid parameter.");
    	
    	requestEmail = params.getRequest().getParameter(PARAM_EMAIL);
    	
    	if (requestEmail != null && !requestEmail.isEmpty()) {
    		if (isUsernameExistsForLogin(requestEmail)) {
    			String uuid = UUID.randomUUID().toString();
            	Email emailToken = new Email();
            	emailToken.setEmail(requestEmail);
            	emailToken.setUuid(uuid);
            	emailToken.setExpiryTimestamp(createExpiryTime());
            	emailService.addEmail(emailToken);
            	
            	mailSenderService.sendEmailForResetPassword(requestEmail, uuid, params.getRequest());
            	            	
    		} else {
    			log.info("Username for login doesn't exist for email address: " + requestEmail);
    			return;
    		}
            
    	} else if (params.getRequest().getQueryString().contains(PARAM_PASSWORD)) {
    		Email token = new Email();
            String jsonString = readJsonFromStream(params.getRequest());
            Map<String, String> jsonObjectMap = new HashMap<String, String>();           
            jsonObjectMap = createJsonObjectMap(jsonString);
			
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
			IbatisUserService userService = new IbatisUserService();
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
    	if (username == null)
    		throw new ActionException("Username for given email is not found.");
    	
    	// Retrieves username for login, if exists in oskari_jaas_users table.
    	String loginUsername = emailService.findUsernameForLogin(username);
    	if (loginUsername == null)
    		return false;
    	else
    		return true;
    }
    
    /**
     * Reads JSON data from stream
     * @param request
     * @return
     */
    private final String readJsonFromStream(HttpServletRequest request) {
    	InputStream inputStream;
    	String jsonString = "";
    	try {
			inputStream = request.getInputStream();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[32];
            int i = 0;
            while (i >= 0) {
            	i = inputStream.read(buffer);
                if (i >= 0)
                	outputStream.write(buffer, 0, i);
            }
            jsonString = new String(outputStream.toByteArray(), "UTF-8");
    	} catch (IOException e) {
			log.debug("Unable to read from stream.");
		}
        return jsonString;
    }
    
    /**
     * Creates JSON object as HashMap.
     * @param query
     * @return
     */
    private final Map<String, String> createJsonObjectMap(String query) {
        String[] params = query.split(",");
        Map<String, String> jsonObjectMap = new HashMap<String, String>();
        for (String param : params) {
            String[] split = param.split(":");
            jsonObjectMap.put(getStringWithoutQuotes(split[0]), getStringWithoutQuotes(split[1]));
        }
        return jsonObjectMap;
    }

    private final String getStringWithoutQuotes(final String value) {
    	String regex = "\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        if (matcher.find())
            return matcher.group(1);
        else
        	return "";
    }
    
    public final int getRequestParameterCount(String query) {   
    	int count = 0;
    	for (int i = 0; i < query.length(); i++){
    		if (query.charAt(i) == '&')
    			++count;
    	}
    	return count;
    }
    
    /**
     * Checks if parameter passed is valid or not.
     * E.g: For password change: action_route=UserPasswordReset&password
     * 		For email: action_route=UserPasswordReset&email=
     * @param params {@link ActionParameters}
     * @return {@link Boolean}
     */
    public final boolean isParameterValid(ActionParameters params) {
    	String paramName = null;
    	String query = params.getRequest().getQueryString(); 
    	if ((params.getHttpParam(PARAM_PASSWORD) != null)) {
    		paramName = query.substring(query.indexOf("&") + 1, query.length());
    		if (paramName.equals(PARAM_PASSWORD))
        		return true;
    		else
    			return false;
    		
    	} else if (params.getHttpParam(PARAM_EMAIL) != null) {
    		return true;
    		
    	} else {
    		return false;
    	}
    }
    
}
