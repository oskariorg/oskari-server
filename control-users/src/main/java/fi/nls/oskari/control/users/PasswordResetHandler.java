package fi.nls.oskari.control.users;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Calendar;
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

import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.users.model.Email;
import fi.nls.oskari.control.users.service.IbatisEmailService;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("UserPasswordReset")
public class PasswordResetHandler extends ActionHandler {

	private String requestEmail = "";
	
	private static final String PARAM_UUID = "uuid";
	private static final String PARAM_EMAIL = "email";
	private static final String PARAM_PASSWORD = "password";
	
    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        // TODO should only handle POST requests (but best to do that last, it is easier to develop/debug with GET requests)
        // TODO parse email address from params
        // TODO create password reset token for the email, store it in the database and send a reset link to the address (only if such address is in database)
        // Return SUCCESS status. Do this even if nothing was sent because client should not be allowed to know whether the
        // provided email address exists in the database.
    	
    	requestEmail = params.getRequest().getParameter(PARAM_EMAIL);
    	IbatisEmailService emailService = new IbatisEmailService();
    	
    	if (requestEmail != null && !requestEmail.isEmpty()) {
    		String uuid = UUID.randomUUID().toString();
        	
        	Email email = new Email();
        	email.setEmail(requestEmail);
        	email.setUuid(uuid);
        	email.setExpiryTimestamp(createExpiryTime());
        	
        	emailService.addEmail(email);
        	
        	sendEmail(requestEmail, uuid, params.getRequest());
            
    	} else if (params.getRequest().getQueryString().contains(PARAM_PASSWORD)) {
    		Email tempEmail = new Email();
            String jsonString = readJsonFromStream(params.getRequest());
            Map<String, String> jsonObjectMap;
			try {
				jsonObjectMap = createJsonObjectMap(jsonString);
				  //JSON object ONLY need to have 2 attributes: 'uuid' and 'password'
	            if (jsonObjectMap.size() != 2) {
	            	 ResponseHelper.writeError(params);
	            	 return;
	            }
	            for (Map.Entry<String, String> entry : jsonObjectMap.entrySet()) {
	            	if(entry.getKey().equals(PARAM_PASSWORD) || entry.getKey().equals(PARAM_UUID)){
	            		if(entry.getKey().equals(PARAM_PASSWORD)) {
		        			tempEmail.setPassword(entry.getValue());
	            		} else {
	            			tempEmail.setUuid(entry.getValue());
	            		}
	            	} else {
	            		 ResponseHelper.writeError(params);
		            	 return;
	            	}
	        		
	        	}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
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
