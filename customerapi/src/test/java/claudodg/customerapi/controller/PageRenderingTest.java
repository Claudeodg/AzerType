package claudodg.customerapi.controller;

import claudodg.customerapi.entity.User;
import claudodg.customerapi.reposotorie.ScoreRepository;
import claudodg.customerapi.reposotorie.UserRepository;
import claudodg.customerapi.security.CustomUserDetailsService;
import claudodg.customerapi.security.SecurityConfig;
import claudodg.customerapi.service.ScoreAnalysisService;
import claudodg.customerapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Checks that the practice and progress pages render in both languages
 * and for every state of the analysis (data, not enough data, service down).
 */
@WebMvcTest(controllers = {PageController.class, AuthController.class})
@Import({SecurityConfig.class, CustomUserDetailsService.class})
class PageRenderingTest {

    private static final String EMAIL = "player@example.com";

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
        User player = new User();
        player.setEmail(EMAIL);
        when(userService.getByEmail(EMAIL)).thenReturn(player);
    }

    static Map<String, Object> sampleAnalysis() {
        return Map.of(
                "best_score", 18,
                "worst_score", 9,
                "average_score", 13.5,
                "trend", "improving",
                "slope", 1.2,
                "predicted_7days", 21.4,
                "level", "beginner",
                "total_sessions", 4,
                "scores", List.of(
                        Map.of("score", 9, "played_at", "2026-10-10T10:00:00"),
                        Map.of("score", 12, "played_at", "2026-10-12T18:30:00"),
                        Map.of("score", 15, "played_at", "2026-10-15T09:15:00"),
                        Map.of("score", 18, "played_at", "2026-10-18T20:45:00")));
    }

    @Test
    void practicePageRendersInEnglish() throws Exception {
        mockMvc.perform(get("/azerType").with(user(EMAIL)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(">Practice<")))
                .andExpect(content().string(containsString(">Sentences<")))
                .andExpect(content().string(containsString(">Log out<")));
    }

    @Test
    void practicePageRendersInGerman() throws Exception {
        mockMvc.perform(get("/azerType").param("lang", "de").with(user(EMAIL)))
                .andExpect(content().string(containsString(">Sätze<")))
                .andExpect(content().string(containsString(">Fortschritt<")))
                .andExpect(content().string(containsString("Richtig")));
    }

    @Test
    void progressPageShowsStatisticsAndTranslatedValues() throws Exception {
        when(scoreAnalysisService.analyzeUserScores(any())).thenReturn(sampleAnalysis());

        mockMvc.perform(get("/progression").param("lang", "de").with(user(EMAIL)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(">Bestleistung<")))
                .andExpect(content().string(containsString(">Steigend<")))
                .andExpect(content().string(containsString(">Anfänger<")))
                .andExpect(content().string(containsString("13,5")))
                .andExpect(content().string(containsString("scoreChart")));
    }

    @Test
    void progressPageShowsEmptyStateWithFewerThanTwoGames() throws Exception {
        when(scoreAnalysisService.analyzeUserScores(any())).thenReturn(Map.of("error", "Not enough data"));

        mockMvc.perform(get("/progression").with(user(EMAIL)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Not enough data yet")))
                .andExpect(content().string(not(containsString("scoreChart"))));
    }

    @Test
    void progressPageExplainsWhenAnalysisServiceIsDown() throws Exception {
        when(scoreAnalysisService.analyzeUserScores(any())).thenThrow(new RuntimeException("Connection refused"));

        mockMvc.perform(get("/progression").with(user(EMAIL)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Statistics are unavailable")))
                .andExpect(content().string(not(containsString("Not enough data yet"))));
    }

    @Test
    void unknownPageReturns404WithTranslatedErrorPage() throws Exception {
        mockMvc.perform(get("/does-not-exist").with(user(EMAIL)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Page not found")))
                .andExpect(content().string(containsString("Error 404")))
                .andExpect(content().string(containsString(">Back to practice<")));

        mockMvc.perform(get("/does-not-exist").param("lang", "de").with(user(EMAIL)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Seite nicht gefunden")));
    }

    @Test
    void unexpectedErrorReturns500WithGenericErrorPage() throws Exception {
        when(userService.getByEmail(EMAIL)).thenThrow(new RuntimeException("Database down"));

        mockMvc.perform(get("/progression").with(user(EMAIL)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Something went wrong")))
                .andExpect(content().string(not(containsString("Database down"))))
                // Logged-in user keeps the app navigation on the error page.
                .andExpect(content().string(containsString(">Log out<")));
    }
}
