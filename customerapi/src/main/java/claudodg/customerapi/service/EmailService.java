package claudodg.customerapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MessageSource messageSource;

    /**
     * Sends the password reset link, written in the given language
     * (texts come from messages.properties / messages_de.properties).
     */
    public void sendPasswordResetEmail(String to, String resetLink, Locale locale) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(messageSource.getMessage("email.reset.subject", null, locale));
        message.setText(messageSource.getMessage("email.reset.body", new Object[]{resetLink}, locale));
        mailSender.send(message);
    }
}
