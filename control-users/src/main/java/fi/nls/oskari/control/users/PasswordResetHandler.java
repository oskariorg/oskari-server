package fi.nls.oskari.control.users;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.users.model.Email;
import fi.nls.oskari.control.users.service.IbatisEmailService;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.user.IbatisUserService;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("UserPasswordReset")
public class PasswordResetHandler extends ActionHandler {
	
	private static final Logger log = LogFactory.getLogger(PasswordResetHandler.class);
	
	private String requestEmail = "";
	
	private static final String PARAM_UUID = "uuid";
	private static final String PARAM_EMAIL = "email";
	private static final String PARAM_PASSWORD = "password";
	
	private final IbatisEmailService emailService = new IbatisEmailService();
	
    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        // TODO should only handle POST requests (but best to do that last, it is easier to develop/debug with GET requests)
        // TODO parse email address from params
        // TODO create password reset token for the email, store it in the database and send a reset link to the address (only if such address is in database)
        // Return SUCCESS status. Do this even if nothing was sent because client should not be allowed to know whether the
        // provided email address exists in the database.
    	
    	requestEmail = params.getRequest().getParameter(PARAM_EMAIL);
    	
    	if (requestEmail != null && !requestEmail.isEmpty()) {
    		if (isUsernameExistsForLogin(requestEmail)) {
    			String uuid = UUID.randomUUID().toString();
            	Email email = new Email();
            	email.setEmail(requestEmail);
            	email.setUuid(uuid);
            	email.setExpiryTimestamp(createExpiryTime());
            	emailService.addEmail(email);
            	sendEmail(requestEmail, uuid, params.getRequest());
    		} else {
    			log.info("Username for login doesn't exist for email address: " + requestEmail);
    		}
            
    	} else if (params.getRequest().getQueryString().contains(PARAM_PASSWORD)) {
    		Email token = new Email();
            String jsonString = readJsonFromStream(params.getRequest());
            Map<String, String> jsonObjectMap;
			try {
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
	            		 ResponseHelper.writeError(params);
		            	 return;
	            	}
	        	}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
						
			String username = emailService.findUsernameForEmail(token.getEmail());
			IbatisUserService userService = new IbatisUserService();
			if (username != null && !username.isEmpty()) {
				String password = userService.getPassword(username);
				if (password == null)
					throw new ActionException("Username is not found in Jaas users.");
				
				//TODO: Need to change encryption method to BCrypt. Currently oskari_jaas_users table has password field of length of 50, which is not enough of BCrypt. Default(60)
				final String hashedPass = "MD5:" + DigestUtils.md5Hex(token.getPassword());
				userService.updatePassword(username, hashedPass);
				//After password update, delete the entry related to token from database
				emailService.deleteEmailToken(token.getUuid());
			}
				
    	} else {
    		
    	}
    	
    	JSONObject result = new JSONObject();
        try {
            result.put("status", "SUCCESS");
        } catch (JSONException e) {
            throw new ActionException("Could not construct JSON", e);
        }
        ResponseHelper.writeResponse(params, result);
    }
    
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
     * While sending email smtp host and sender should be added to oskari-ext.properties
     * e.g: oskari.email.sender=abc@def.com
     * 		oskari.email.host=smtp.domain.com
     * @param to Receiver's email address
     * @param uuid Token number to be sent with email.
     * @param request HttpServletRequest.
     */
    private void sendEmail(String to, String uuid, HttpServletRequest request){
    	String from;
    	Properties properties;
    	
    	try {
    		from = PropertyUtil.get("oskari.email.sender");
        	properties = System.getProperties();
        	properties.setProperty("mail.smtp.host", PropertyUtil.get("oskari.email.host"));
    	} catch (Exception e) {
    		e.printStackTrace();
    		return;
    	}
    	
    	Session session = Session.getDefaultInstance(properties);
    	 try {
    		 MimeMessage mimeMessage = new MimeMessage(session);
    		 mimeMessage.setFrom(new InternetAddress(from));
    		 mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
    		 mimeMessage.setSubject("TEST");
    		 String serverAddress = getServerAddress(request);
    		 mimeMessage.setContent("<h3> Please use this link to reset your password : "
    				 + serverAddress + "/resetPassword/" + uuid + " </h3>", "text/html" );
             Transport.send(mimeMessage);
          } catch (MessagingException ex) {
             ex.printStackTrace();
          }
    }
    
    private final String getServerAddress(final HttpServletRequest request) {
    	return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }
    
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
				e.printStackTrace();
		}
        return jsonString;
    }
    
    private final Map<String, String> createJsonObjectMap(String query) throws UnsupportedEncodingException {
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
}
