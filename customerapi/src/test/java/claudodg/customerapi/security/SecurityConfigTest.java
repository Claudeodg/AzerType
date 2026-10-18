package claudodg.customerapi.security;

import claudodg.customerapi.controller.AuthController;
import claudodg.customerapi.controller.PageController;
import claudodg.customerapi.entity.User;
import claudodg.customerapi.reposotorie.ScoreRepository;
import claudodg.customerapi.reposotorie.UserRepository;
import claudodg.customerapi.service.ScoreAnalysisService;
import claudodg.customerapi.service.UserService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, PageController.class})
@Import({SecurityConfig.class, CustomUserDetailsService.class})
class SecurityConfigTest {

    private static final String EMAIL = "player@example.com";
    private static final String PASSWORD = "Passw0rd123";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private ScoreRepository scoreRepository;
    @MockitoBean
    private ScoreAnalysisService scoreAnalysisService;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail(EMAIL);
        user.setPassword(new BCryptPasswordEncoder().encode(PASSWORD));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userService.getByEmail(EMAIL)).thenReturn(user);
    }

    @Test
    void publicPagesAreAccessibleWithoutLogin() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/auth/login")).andExpect(status().isOk());
        mockMvc.perform(get("/auth/register")).andExpect(status().isOk());
        mockMvc.perform(get("/auth/forgot-password")).andExpect(status().isOk());
    }

    @Test
    void homePageShowsLoginAndSignUpButtonsInEnglishByDefault() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("lang=\"en\"")))
                .andExpect(content().string(containsString(">Log in<")))
                .andExpect(content().string(containsString(">Sign up<")));
    }

    @Test
    void languageCanBeSwitchedToGermanAndIsRemembered() throws Exception {
        mockMvc.perform(get("/").param("lang", "de"))
                .andExpect(content().string(containsString(">Anmelden<")))
                .andExpect(content().string(containsString(">Registrieren<")))
                .andExpect(cookie().value("lang", "de"));

        mockMvc.perform(get("/auth/login").cookie(new Cookie("lang", "de")))
                .andExpect(content().string(containsString("Passwort vergessen?")));
    }

    @Test
    void germanBrowserGetsGermanByDefault() throws Exception {
        mockMvc.perform(get("/").header("Accept-Language", "de-DE,de;q=0.9"))
                .andExpect(content().string(containsString(">Anmelden<")));
    }

    @Test
    void unsupportedLanguageFallsBackToEnglish() throws Exception {
        mockMvc.perform(get("/").param("lang", "fr"))
                .andExpect(content().string(containsString(">Log in<")))
                .andExpect(cookie().doesNotExist("lang"));
    }

    @Test
    void homePageShowsPlayAndLogoutWhenLoggedIn() throws Exception {
        mockMvc.perform(get("/").with(user(EMAIL)))
                .andExpect(content().string(containsString(">Play<")))
                .andExpect(content().string(containsString(">Log out<")));
    }

    @Test
    void protectedPageRedirectsToLoginWhenAnonymous() throws Exception {
        mockMvc.perform(get("/azerType"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
        mockMvc.perform(get("/progression"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    void apiEndpointReturns401WhenAnonymous() throws Exception {
        mockMvc.perform(post("/score").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"score\":5}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/gemini/generate").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithCorrectPasswordAuthenticatesAgainstDatabaseUser() throws Exception {
        mockMvc.perform(formLogin("/auth/login").userParameter("email").user(EMAIL).password(PASSWORD))
                .andExpect(authenticated().withUsername(EMAIL))
                .andExpect(redirectedUrl("/azerType"));
    }

    @Test
    void loginWithWrongPasswordFails() throws Exception {
        mockMvc.perform(formLogin("/auth/login").userParameter("email").user(EMAIL).password("wrong"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/auth/login?error=invalid"));
    }

    @Test
    void loginWithUnknownEmailFailsWithSameError() throws Exception {
        mockMvc.perform(formLogin("/auth/login").userParameter("email").user("nobody@example.com").password(PASSWORD))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/auth/login?error=invalid"));
    }

    @Test
    void loginWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/auth/login").param("email", EMAIL).param("password", PASSWORD))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedUserCanSaveScore() throws Exception {
        mockMvc.perform(post("/score").with(user(EMAIL)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"score\":5}"))
                .andExpect(status().isOk());
    }

    @Test
    void logoutRedirectsToLoginPage() throws Exception {
        mockMvc.perform(post("/auth/logout").with(user(EMAIL)).with(csrf()))
                .andExpect(redirectedUrl("/auth/login?logout"));
    }
}
