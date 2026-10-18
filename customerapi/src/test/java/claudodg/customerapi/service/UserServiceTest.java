package claudodg.customerapi.service;

import claudodg.customerapi.entity.PasswordResetToken;
import claudodg.customerapi.entity.User;
import claudodg.customerapi.reposotorie.PasswordResetTokenRepository;
import claudodg.customerapi.reposotorie.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL = "player@example.com";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private EmailService emailService;
    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Spy
    private SessionRegistry sessionRegistry = new SessionRegistryImpl();

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "baseUrl", "https://azertype.test");
        user = new User();
        user.setEmail(EMAIL);
        user.setPassword(passwordEncoder.encode("OldPassword1"));
    }

    @Test
    void resetRequestStoresOnlyTheHashOfTheTokenSentByEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        userService.createPasswordResetToken(EMAIL, Locale.ENGLISH);

        ArgumentCaptor<PasswordResetToken> saved = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(saved.capture());
        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordResetEmail(eq(EMAIL), link.capture(), eq(Locale.ENGLISH));

        String rawToken = link.getValue().substring(link.getValue().indexOf("token=") + 6);
        assertThat(link.getValue()).startsWith("https://azertype.test/auth/reset-password?token=");
        assertThat(saved.getValue().getToken())
                .isNotEqualTo(rawToken)
                .isEqualTo(UserService.hashToken(rawToken));
        assertThat(saved.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    void unknownEmailSendsNothing() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        userService.createPasswordResetToken("nobody@example.com", Locale.ENGLISH);

        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), any());
    }

    @Test
    void secondRequestWithinCooldownSendsNoEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(tokenRepository.existsByUserAndCreatedAtAfter(eq(user), any())).thenReturn(true);

        userService.createPasswordResetToken(EMAIL, Locale.ENGLISH);

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), any());
    }

    @Test
    void mailFailureIsNotPropagatedToTheCaller() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        doThrow(new MailSendException("SMTP down"))
                .when(emailService).sendPasswordResetEmail(anyString(), anyString(), any());

        assertThatCode(() -> userService.createPasswordResetToken(EMAIL, Locale.ENGLISH)).doesNotThrowAnyException();
    }

    @Test
    void resetPasswordChangesPasswordAndExpiresExistingSessions() {
        PasswordResetToken token = validToken("raw-token");
        when(tokenRepository.findByToken(UserService.hashToken("raw-token"))).thenReturn(Optional.of(token));
        when(tokenRepository.findByUser(user)).thenReturn(List.of(token));

        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername(EMAIL).password("x").roles("USER").build();
        sessionRegistry.registerNewSession("session-1", principal);

        userService.resetPassword("raw-token", "NewPassword1");

        assertThat(passwordEncoder.matches("NewPassword1", user.getPassword())).isTrue();
        assertThat(sessionRegistry.getSessionInformation("session-1").isExpired()).isTrue();
        verify(tokenRepository).deleteAll(List.of(token));
    }

    @Test
    void expiredTokenIsRejected() {
        PasswordResetToken token = validToken("raw-token");
        token.setExpiryDate(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByToken(UserService.hashToken("raw-token"))).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> userService.resetPassword("raw-token", "NewPassword1"))
                .isInstanceOf(RuntimeException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void rawTokenStoredInDatabaseDoesNotWorkAsResetLink() {
        // Simulates a leaked database: the stored value (the hash) must not be usable as a token.
        String storedHash = UserService.hashToken("raw-token");
        when(tokenRepository.findByToken(UserService.hashToken(storedHash))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.resetPassword(storedHash, "NewPassword1"))
                .isInstanceOf(RuntimeException.class);
    }

    private PasswordResetToken validToken(String raw) {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UserService.hashToken(raw));
        token.setUser(user);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiryDate(LocalDateTime.now().plusMinutes(30));
        return token;
    }
}
