package fi.nls.oskari.control.users.service;

import fi.nls.oskari.control.users.model.EmailMessage;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailSenderService {

    private static final Logger log = LogFactory.getLogger(MailSenderService.class);

    private static final String EMAIL_SUBJECT_ACTIVATE_REGISTRATION = "Rekisteröinnin aktivointi";
    private static final String EMAIL_CONTENT_ACTIVATE_REGISTRATION = "Vahvista rekisteröitymisesi "
            + "ja aseta salasanasi alla olevalla linkillä. Linkki on voimassa vain 2 päivää. <br>";
    private static final String EMAIL_SUBJECT_PASSWORD_CHANGE = "Salasanan vaihto";
    private static final String EMAIL_CONTENT_PASSWORD_CHANGE = "Vaihda salasanasi alla olevalla "
            + "linkillä. Linkki on voimassa vain 2 päivää.<br>";

    /**
     * While sending email smtp host and sender should be added to oskari-ext.properties
     * e.g: oskari.email.sender=abc@def.com
     * oskari.email.host=smtp.domain.com
     *
     * @param emailMessage
     * @param uuid          Token number to be sent with email.
     * @param serverAddress Address to include in message
     */
    public final void sendEmail(EmailMessage emailMessage, String uuid, String serverAddress) {
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
            mimeMessage.setContent(emailMessage.getContent() + "<br>" + serverAddress + "/resetPassword/"
                    + uuid, "text/html; charset=UTF-8");
            Transport.send(mimeMessage);
        } catch (MessagingException ex) {
            log.debug("Email can't be sent to email address: " + emailMessage.getTo());
        }
    }

    public final void sendEmailForRegistrationActivation(User user, String serverAddress) {
        String subject = PropertyUtil.get("oskari.email.subject.register.user", EMAIL_SUBJECT_ACTIVATE_REGISTRATION);
        String content = PropertyUtil.get("oskari.email.body.register.user", EMAIL_CONTENT_ACTIVATE_REGISTRATION);
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setTo(user.getEmail());
        emailMessage.setSubject(subject);
        emailMessage.setContent(content);
        sendEmail(emailMessage, user.getUuid(), serverAddress);
    }

    public final void sendEmailForResetPassword(User user, String uuid, String serverAddress) {
        String subject = PropertyUtil.get("oskari.email.subject.password.change", EMAIL_SUBJECT_PASSWORD_CHANGE);
        String content = PropertyUtil.get("oskari.email.body.password.change", EMAIL_CONTENT_PASSWORD_CHANGE)
                + "<br> Käyttäjä/Username : " + user.getScreenname();
        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setTo(user.getEmail());
        emailMessage.setSubject(subject);
        emailMessage.setContent(content);
        sendEmail(emailMessage, uuid, serverAddress);
    }
}
