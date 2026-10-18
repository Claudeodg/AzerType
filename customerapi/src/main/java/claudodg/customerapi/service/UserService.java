package claudodg.customerapi.service;

import claudodg.customerapi.entity.PasswordResetToken;
import claudodg.customerapi.entity.User;
import claudodg.customerapi.reposotorie.PasswordResetTokenRepository;
import claudodg.customerapi.reposotorie.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final int RESET_TOKEN_VALIDITY_MINUTES = 30;
    /** Minimum delay between two reset emails for the same account (anti email-flooding). */
    private static final int RESET_REQUEST_COOLDOWN_MINUTES = 2;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SessionRegistry sessionRegistry;

    @Value("${app.base-url}")
    private String baseUrl;

    public void register(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    /**
     * Returns the user with the given email. Login itself is handled by Spring
     * Security (see SecurityConfig / CustomUserDetailsService); this is used to
     * load the entity of the currently authenticated user.
     */
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Generates a password reset token and emails the reset link, if the email
     * belongs to a registered user. Silently does nothing otherwise.
 * The email is written in the given language.
     * <p>
     * Runs asynchronously and never throws: the caller always returns the same
     * response at the same speed, so neither the response time nor an error
     * (e.g. SMTP down) reveals whether the email is registered.
     */
    @Async
    public void createPasswordResetToken(String email, Locale locale) {
        try {
            userRepository.findByEmail(email).ifPresent(user -> sendResetLink(user, locale));
        } catch (Exception e) {
            log.error("Password reset request failed", e);
        }
    }

    private void sendResetLink(User user, Locale locale) {
        LocalDateTime now = LocalDateTime.now();

        if (passwordResetTokenRepository.existsByUserAndCreatedAtAfter(
                user, now.minusMinutes(RESET_REQUEST_COOLDOWN_MINUTES))) {
            log.info("Password reset ignored: a link was already sent less than {} minutes ago",
                    RESET_REQUEST_COOLDOWN_MINUTES);
            return;
        }

        passwordResetTokenRepository.deleteAll(passwordResetTokenRepository.findByUserAndUsedFalse(user));

        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        // Only the hash is stored: a leaked database does not give usable reset links.
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(hashToken(token));
        resetToken.setUser(user);
        resetToken.setCreatedAt(now);
        resetToken.setExpiryDate(now.plusMinutes(RESET_TOKEN_VALIDITY_MINUTES));
        passwordResetTokenRepository.save(resetToken);

        String resetLink = baseUrl + "/auth/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink, locale);
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(hashToken(token))
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset link"));

        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Invalid or expired reset link");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Remove all of this user's tokens (this one and any other still pending).
        passwordResetTokenRepository.deleteAll(passwordResetTokenRepository.findByUser(user));

        expireSessionsOf(user.getEmail());
    }

    /** Logs the user out everywhere: sessions opened with the old password stop working. */
    private void expireSessionsOf(String email) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof UserDetails details && details.getUsername().equals(email)) {
                for (SessionInformation session : sessionRegistry.getAllSessions(principal, false)) {
                    session.expireNow();
                }
            }
        }
    }

    static String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
