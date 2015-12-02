package fi.nls.oskari.control.users.service;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import fi.nls.oskari.control.users.model.EmailMessage;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;

public class MailSenderService {

	private static final Logger log = LogFactory.getLogger(MailSenderService.class);
	
	private static final String EMAIL_SUBJECT_ACTIVATE_REGISTRATION = "Activate registration";
	private static final String EMAIL_CONTENT_ACTIVATE_REGISTRATION = "Please use this link to "
				+ "activate registration and also change password. The link is active for ONLY 2 days."
				+ "<br>";
		
	 /**
     * While sending email smtp host and sender should be added to oskari-ext.properties
     * e.g: oskari.email.sender=abc@def.com
     * 		oskari.email.host=smtp.domain.com
     * @param to Receiver's email address
     * @param uuid Token number to be sent with email.
     * @param request HttpServletRequest.
     */
    public final void sendEmail(EmailMessage emailMessage, String uuid, HttpServletRequest request){
    	String from;
    	Properties properties;
    	
    	try {
    		from = PropertyUtil.get("oskari.email.sender");
        	properties = System.getProperties();
        	properties.setProperty("mail.smtp.host", PropertyUtil.get("oskari.email.host"));
    	} catch (Exception e) {
    		log.debug("Unable to read the properties for sending email.");
    		return;
    	}
    	
    	Session session = Session.getDefaultInstance(properties);
    	 try {
    		 MimeMessage mimeMessage = new MimeMessage(session);
    		 mimeMessage.setFrom(new InternetAddress(from));
    		 mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(emailMessage.getTo()));
    		 mimeMessage.setSubject(emailMessage.getSubject());
    		 String serverAddress = getServerAddress(request);
    		 mimeMessage.setContent(emailMessage.getContent() + "<br>" + serverAddress + "/resetPassword/" 
    				 + uuid, "text/html" );
             Transport.send(mimeMessage);
          } catch (MessagingException ex) {
             log.debug("Email can't be sent to email address: " + emailMessage.getTo());
          }
    }
    
    private final String getServerAddress(final HttpServletRequest request) {
    	return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }
    
    public final void sendEmailForRegistrationActivation(User user, HttpServletRequest request) {
    	EmailMessage emailMessage = new EmailMessage();
    	emailMessage.setTo(user.getEmail());
    	emailMessage.setSubject(EMAIL_SUBJECT_ACTIVATE_REGISTRATION);
    	emailMessage.setContent(EMAIL_CONTENT_ACTIVATE_REGISTRATION);
    	sendEmail(emailMessage, user.getUuid(), request);
    }
}
