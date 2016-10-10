package fi.nls.oskari.control.users.service;

import fi.nls.oskari.control.users.model.EmailMessage;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spring.SpringContextHolder;
import fi.nls.oskari.util.PropertyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Locale;
import java.util.Properties;

public class MailSenderService {

    private static final Logger log = LogFactory.getLogger(MailSenderService.class);
    private MessageSource messages;

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
    public final void sendEmail(EmailMessage emailMessage, final String uuid, final String serverAddress) {
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
            mimeMessage.setContent(emailMessage.getContent() +
                getMessages().getMessage("oskari.email.link.to.reset", new String[]{serverAddress, uuid}, Locale.ENGLISH),
                "text/html; charset=UTF-8");
            Transport.send(mimeMessage);
        } catch (MessagingException ex) {
            log.debug("Email can't be sent to email address: " + emailMessage.getTo());
        }
    }

    public final void sendEmailForRegistrationActivation(User user, String serverAddress) {

        String subject = getMessages().getMessage("oskari.email.subject.register.user", null, Locale.ENGLISH);
        String content = getMessages().getMessage("oskari.email.body.register.user", null, Locale.ENGLISH);
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setTo(user.getEmail());
        emailMessage.setSubject(subject);
        emailMessage.setContent(content);
        sendEmail(emailMessage, user.getUuid(), serverAddress);
    }

    public final void sendEmailForResetPassword(User user, String uuid, String serverAddress) {
        String subject = getMessages().getMessage("oskari.email.subject.password.change", null, Locale.ENGLISH);
        String content = getMessages().getMessage("oskari.email.body.password.change", new String[]{user.getScreenname()}, Locale.ENGLISH);
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setTo(user.getEmail());
        emailMessage.setSubject(subject);
        emailMessage.setContent(content);
        sendEmail(emailMessage, uuid, serverAddress);
    }
}
