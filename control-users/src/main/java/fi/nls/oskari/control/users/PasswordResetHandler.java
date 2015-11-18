package fi.nls.oskari.control.users;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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
	
    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        // TODO should only handle POST requests (but best to do that last, it is easier to develop/debug with GET requests)
        // TODO parse email address from params
        // TODO create password reset token for the email, store it in the database and send a reset link to the address (only if such address is in database)
        // Return SUCCESS status. Do this even if nothing was sent because client should not be allowed to know whether the
        // provided email address exists in the database.
    	
    	requestEmail = params.getRequest().getParameter("email");
    	String uuid = UUID.randomUUID().toString();
    	
    	Email email = new Email();
    	email.setEmail(requestEmail);
    	email.setScreenname("Test");
    	email.setUuid(uuid);
    	email.setExpiryTimestamp(createExpiryTime());
    	
    	IbatisEmailService emailService = new IbatisEmailService();
    	emailService.addEmail(email);
    	
    	sendEmail(requestEmail, uuid);
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
     */
    private void sendEmail(String to, String uuid){
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
    		 mimeMessage.setContent("<h1>Token : " + uuid + " </h1>", "text/html" );
             Transport.send(mimeMessage);
          } catch (MessagingException ex) {
             ex.printStackTrace();
          }
    }

}
