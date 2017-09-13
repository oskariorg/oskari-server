package fi.nls.oskari.control.users.service;

import fi.nls.oskari.control.users.model.EmailMessage;
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
    private static final String KEY_LINK_TO_SET_PASSWORD = "link_to_continue";
    private static final String KEY_LINK_EXPIRY_TIME = "days_to_expire";
    private static final int DEFAULT_LINK_EXPIRY_TIME = 2;

    public final void sendEmailForRegistrationActivation(String email, String token, String serverAddress, String language) throws ServiceException {
        EmailMessage emailMessage = emailTo(email);

        String subject = getMessage("user.registration.email.registration.subject", language);
        emailMessage.setSubject(subject);

        Map params = getDefaultParams(serverAddress, token);
        // set return url
        params.put(KEY_LINK_TO_SET_PASSWORD, serverAddress + "/user/" + token);
        String content = constructMail(getTemplateFor("oskari.email.registration.tpl", language), params);
        emailMessage.setContent(content);
        sendEmail(emailMessage);
    }

    public final void sendEmailForPasswordResetWithoutAccount(String email, String token, String serverAddress, String language) throws ServiceException {
        EmailMessage emailMessage = emailTo(email);

        String subject = getMessage("user.registration.email.passwordrecovery.subject", language);
        emailMessage.setSubject(subject);

        Map params = getDefaultParams(serverAddress, token);
        // set return url
        params.put(KEY_LINK_TO_SET_PASSWORD, serverAddress + "/user/" + token);
        String content = constructMail(getTemplateFor("oskari.email.passwordrecovery.noaccount.tpl", language), params);
        emailMessage.setContent(content);
        sendEmail(emailMessage);
    }

    //

    public final void sendEmailForResetPassword(String email, String token, String serverAddress, String language) throws ServiceException {
        EmailMessage emailMessage = emailTo(email);

        String subject = getMessage("user.registration.email.passwordrecovery.subject", language);
        emailMessage.setSubject(subject);
        Map params = getDefaultParams(serverAddress, token);
        // set return url
        params.put(KEY_LINK_TO_SET_PASSWORD, serverAddress + "/user/reset/" + token);
        String content = constructMail(getTemplateFor("oskari.email.passwordrecovery.tpl", language), params);
        emailMessage.setContent(content);
        sendEmail(emailMessage);
    }

    public final void sendEmailAlreadyExists(String email, String serverAddress, String language) throws ServiceException {
        EmailMessage emailMessage = emailTo(email);

        String subject = getMessage("user.registration.email.already.registered.subject", language);
        emailMessage.setSubject(subject);

        Map params = getDefaultParams(serverAddress);
        // set return url
        params.put(KEY_LINK_TO_SET_PASSWORD, serverAddress + "/user/reset");
        String content = constructMail(getTemplateFor("oskari.email.exists.tpl", language), params);
        emailMessage.setContent(content);
        sendEmail(emailMessage);
    }
    /**
     * While sending email smtp host and sender should be added to oskari-ext.properties
     * e.g: oskari.email.sender=abc@def.com
     * oskari.email.host=smtp.domain.com
     *
     * @param emailMessage
     */
    public void sendEmail(EmailMessage emailMessage) throws ServiceException {
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
            log.warn(ex, "Can't send email to: " + emailMessage.getTo());
            throw new ServiceException("Can't send email to: " + emailMessage.getTo());
        }
    }

    private EmailMessage emailTo(String address) {
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setTo(address);
        return emailMessage;
    }

    private MessageSource getMessages() {
        if(messages == null) {
            // "manual autowire"
            messages = SpringContextHolder.getBean(MessageSource.class);

        }
        return messages;
    }
    private String getMessage(String key, String language) {
        return getMessages().getMessage(key, new String[]{}, new Locale(language));
    }


    private Map getDefaultParams(String serverAddress) {
        return getDefaultParams(serverAddress, "");
    }

    private Map getDefaultParams(String serverAddress, String token) {
        Map params = new HashMap();
        params.put(KEY_LINK_TO_SET_PASSWORD, serverAddress + "/user/" + token);
        params.put(KEY_LINK_EXPIRY_TIME, PropertyUtil.getOptional("oskari.email.link.expirytime", DEFAULT_LINK_EXPIRY_TIME));
        return params;
    }


    private String constructMail(String template, Map params) {
        StrSubstitutor emailValuesSubstitutor = new StrSubstitutor(params);
        return emailValuesSubstitutor.replace(template);
    }

    private String getTemplateFor(String key, String language) throws ServiceException {
        // try customized language version
        String emailContent = readTemplateFile(PropertyUtil.get(key + "." + language));
        if(emailContent != null && !emailContent.isEmpty()) {
            return emailContent;
        }
        // try customized default
        emailContent = readTemplateFile(PropertyUtil.get(key));
        if(emailContent != null && !emailContent.isEmpty()) {
            return emailContent;
        }
        // try generic language version
        emailContent = readTemplateFile(key + "." + language + ".html");
        if(emailContent != null && !emailContent.isEmpty()) {
            return emailContent;
        }
        emailContent = readTemplateFile(key + ".html");
        if(emailContent != null && !emailContent.isEmpty()) {
            return emailContent;
        }
        throw new ServiceException("Couldn't find template for key: " + key);
    }

    private String readTemplateFile(String file) throws ServiceException {
        try (InputStream in = getClass().getResourceAsStream(file)){
            return IOHelper.readString(in);
        } catch (Exception e) {
            log.debug("Unable to read the email template file for sending email. File:", file, "Reason:", e.getMessage());
            throw new ServiceException("Unable to read the properties for sending email.");
        }
    }
}
