package claudodg.customerapi.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Tracks the open sessions of each user, so they can be closed after a password reset. */
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /** Notifies the SessionRegistry when a session ends, so it does not keep stale entries. */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SessionRegistry sessionRegistry) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Public: auth pages (register, login, password reset), error page, static assets
                        .requestMatchers("/", "/auth/**", "/error").permitAll()
                        .requestMatchers("/*.css", "/*.js", "/favicon.ico").permitAll()
                        // Everything else (game, progression, score, Gemini proxy) needs a logged-in user
                        .anyRequest().authenticated()
                )
                // Login is handled by Spring Security: the form on /auth/login posts
                // email + password here, checked via CustomUserDetailsService + BCrypt.
                // The session id is rotated on success (protects against session fixation).
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/azerType", true)
                        // Generic error on purpose: never reveal whether the email exists.
                        .failureUrl("/auth/login?error=invalid")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                // Unlimited sessions per user, but registered so they can be expired
                // (see UserService.resetPassword). An expired session is sent back to the login page.
                .sessionManagement(session -> session
                        .maximumSessions(-1)
                        .sessionRegistry(sessionRegistry)
                        .expiredUrl("/auth/login?expired")
                )
                // Anonymous access: fetch() calls (JSON endpoints) get a 401,
                // every other page redirects to the login page.
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                SecurityConfig::isApiRequest)
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/auth/login"),
                                request -> true)
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // Plain (unmasked) handler: JS reads the raw XSRF-TOKEN cookie
                        // value and must be able to send that exact value back as a
                        // header. The default Xor handler BREACH-masks the value used
                        // in server-rendered forms, which would not match the cookie.
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                );
        return http.build();
    }

    private static boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/api/") || "/score".equals(uri);
    }
}
