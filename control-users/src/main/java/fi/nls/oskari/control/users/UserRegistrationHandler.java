package fi.nls.oskari.control.users;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.users.model.Email;
import fi.nls.oskari.control.users.model.EmailMessage;
import fi.nls.oskari.control.users.service.IbatisEmailService;
import fi.nls.oskari.control.users.service.MailSenderService;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.user.DatabaseUserService;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("UserRegistration")
public class UserRegistrationHandler extends ActionHandler {

	private static final Logger log = LogFactory.getLogger(UserRegistrationHandler.class);
	
    private static final String PARAM_FIRSTNAME = "firstname";
    private static final String PARAM_LASTNAME = "lastname";
    private static final String PARAM_SCREENNAME = "username";
    private static final String PARAM_EMAIL = "email";
    
    private static final String EMAIL_SUBJECT_ACTIVATE_REGISTRATION = "Activate registration";
	private static final String EMAIL_CONTENT_ACTIVATE_REGISTRATION = "Please use this link to "
			+ "activate registration and also change password. The link is active for ONLY 2 days."
			+ "<br>";
    
    private final DatabaseUserService userService = new DatabaseUserService();
    private final IbatisEmailService emailService = new IbatisEmailService();
    private final MailSenderService mailSenderService = new MailSenderService();
    
	@Override
	public void handleAction(ActionParameters params) throws ActionException {
		User user = new User();
		getUserParams(user, params);
		
		if (isEmailAlreadyExist(user.getEmail())) {
			throw new ActionException("Email already exists.");
		}
		if (isUsernameAlreadyExist(user.getScreenname())) {
			throw new ActionException("Username already exists.");
		}
		try {
			userService.createUser(user);
		} catch (ServiceException se) {			
			throw new ActionException(se.getMessage(), se);
		}
		
    	Email emailToken = new Email();
    	emailToken.setEmail(user.getEmail());
    	emailToken.setUuid(user.getUuid());
    	emailToken.setExpiryTimestamp(createExpiryTime());
    	emailService.addEmail(emailToken);
    	
		EmailMessage emailMessage = new EmailMessage();
    	emailMessage.setTo(user.getEmail());
    	emailMessage.setSubject(EMAIL_SUBJECT_ACTIVATE_REGISTRATION);
    	emailMessage.setContent(EMAIL_CONTENT_ACTIVATE_REGISTRATION);
    	mailSenderService.sendEmail(emailMessage, user.getUuid(), params.getRequest());
	}
		
	private final boolean isEmailAlreadyExist(final String emailAddress) {
		if (emailService.findUsernameForEmail(emailAddress) != null)
			return true;
		else 
			return false;
	}
	
	private final boolean isUsernameAlreadyExist(final String username) {
		if (emailService.findEmailForUsername(username) != null)
			return true;
		else 
			return false;
	}
	
	private void getUserParams(User user, ActionParameters params) throws ActionParamsException {
        user.setFirstname(params.getRequiredParam(PARAM_FIRSTNAME));
        user.setLastname(params.getRequiredParam(PARAM_LASTNAME));
        user.setScreenname(params.getRequiredParam(PARAM_SCREENNAME));
        user.setEmail(params.getRequiredParam(PARAM_EMAIL));
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
}
