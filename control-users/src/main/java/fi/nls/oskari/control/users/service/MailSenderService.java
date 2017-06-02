package fi.nls.oskari.control.users.service;

import fi.nls.oskari.control.users.model.EmailMessage;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.spring.SpringContextHolder;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.apache.commons.lang.text.StrSubstitutor;
import org.springframework.context.MessageSource;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class MailSenderService {

    private static final Logger log = LogFactory.getLogger(MailSenderService.class);
    private MessageSource messages;
    private static final String linkResetPassword = "link_to_set_password";
    private static final String linkExpiryTime = "link_expiry_time";
    private static final String registrationUserName = "user_name";
    private static final int defaultLinkExpiryTime = 2;

    private MessageSource getMessages() {
        if(messages == null) {
            // "manual autowire"
            messages = SpringContextHolder.getBean(MessageSource.class);

        }
        return messages;
    }

    /**
     * While sending email smtp host and sender should be added to oskari-ext.properties
     * e.g: oskari.email.sender=abc@def.com
     * oskari.email.host=smtp.domain.com
     *
     * @param emailMessage
     * @param uuid          Token number to be sent with email.
     * @param serverAddress Address to include in message
     */
    public final void sendEmail(EmailMessage emailMessage, final String uuid, final String serverAddress, String language) throws ServiceException {
        String from;
        Properties properties;

        try {
            from = PropertyUtil.get("oskari.email.sender");
            properties = System.getProperties();
            properties.setProperty("mail.smtp.host", PropertyUtil.get("oskari.email.host"));
        } catch (Exception e) {
            log.debug("Unable to read the properties for sending email.");
            throw new ServiceException("Unable to read the properties for sending email.");
        }

        Session session = Session.getDefaultInstance(properties);
        try {
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(from));
            mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(emailMessage.getTo()));
            mimeMessage.setSubject(emailMessage.getSubject());
            mimeMessage.setContent(emailMessage.getContent(), "text/html; charset=UTF-8");
            Transport.send(mimeMessage);
        } catch (MessagingException ex) {
            log.debug("Can't send to address: " + emailMessage.getTo());
            throw new ServiceException("Can't send to address: " + emailMessage.getTo());
        }
    }

    public final void sendEmailForRegistrationActivation(User user, String serverAddress, String language) throws ServiceException {
        String emailContent = readFile(PropertyUtil.get("oskari.email.registration." + language));
        Map emailValuesMap = new HashMap();
        emailValuesMap.put(linkResetPassword,
                getMessages().getMessage("oskari.email.link.to.reset", new String[]{serverAddress, user.getUuid()}, new Locale(language)));
        emailValuesMap.put(linkExpiryTime, PropertyUtil.getOptional("oskari.email.link.expirytime", defaultLinkExpiryTime));
        emailValuesMap.put(registrationUserName, user.getScreenname());
        StrSubstitutor emailValuesSubstitutor = new StrSubstitutor(emailValuesMap);
        String resolvedEmailContent = emailValuesSubstitutor.replace(emailContent);
        String subject = PropertyUtil.get("oskari.email.registration.title." + language,
                getMessages().getMessage("user.registration.title", new String[]{serverAddress, user.getUuid()}, new Locale(language)));

        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setTo(user.getEmail());
        emailMessage.setSubject(subject);
        emailMessage.setContent(resolvedEmailContent);
        sendEmail(emailMessage, user.getUuid(), serverAddress, language);
    }

    public final void sendEmailForResetPassword(User user, String uuid, String serverAddress, String language) throws ServiceException {
        String emailContent = readFile(PropertyUtil.get("oskari.email.passwordrecovery." + language));
        Map emailValuesMap = new HashMap();
        emailValuesMap.put(linkResetPassword,
                getMessages().getMessage("oskari.email.link.to.reset", new String[]{serverAddress, user.getUuid()}, new Locale(language)));
        emailValuesMap.put(linkExpiryTime, PropertyUtil.getOptional("oskari.email.link.expirytime", defaultLinkExpiryTime));
        StrSubstitutor emailValuesSubstitutor = new StrSubstitutor(emailValuesMap);
        String resolvedEmailContent = emailValuesSubstitutor.replace(emailContent);
        String subject = PropertyUtil.get("oskari.email.passwordrecovery.title." + language,
                getMessages().getMessage("user.registration.passwordReset.title", new String[]{serverAddress, user.getUuid()}, new Locale(language)));

        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setTo(user.getEmail());
        emailMessage.setSubject(subject);
        emailMessage.setContent(resolvedEmailContent);
        sendEmail(emailMessage, uuid, serverAddress, language);
    }

    public final void sendEmailAlreadyExists(User user, String serverAddress, String language) throws ServiceException {
        String emailContent = readFile(PropertyUtil.get("oskari.email.exists." + language));
        Map emailValuesMap = new HashMap();
        emailValuesMap.put(linkResetPassword,
                getMessages().getMessage("oskari.email.link.to.reset", new String[]{serverAddress, user.getUuid()}, new Locale(language)));
        emailValuesMap.put(linkExpiryTime, PropertyUtil.getOptional("oskari.email.link.expirytime", defaultLinkExpiryTime));
        StrSubstitutor emailValuesSubstitutor = new StrSubstitutor(emailValuesMap);
        String resolvedEmailContent = emailValuesSubstitutor.replace(emailContent);
        String subject = PropertyUtil.get("oskari.email.exists.title." + language,
                getMessages().getMessage("user.registration.email.exists.title", new String[]{serverAddress, user.getUuid()}, new Locale(language)));

        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setTo(user.getEmail());
        emailMessage.setSubject(subject);
        emailMessage.setContent(resolvedEmailContent);
        sendEmail(emailMessage, user.getUuid(), serverAddress, language);
    }
    private String readFile(String file) throws ServiceException {
        InputStream in = null;
        String contents = null;
        try {
            in = PropertyUtil.class.getResourceAsStream("/"+file);
            contents = IOHelper.readString(in);
            IOHelper.close(in);
            return contents;
        } catch (Exception ignored) {
            log.debug("Unable to read the email template file for sending email.");
            throw new ServiceException("Unable to read the properties for sending email.");
        }
    }
}
