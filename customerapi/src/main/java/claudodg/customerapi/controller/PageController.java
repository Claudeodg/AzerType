package claudodg.customerapi.controller;

import claudodg.customerapi.entity.Score;
import claudodg.customerapi.entity.User;
import claudodg.customerapi.reposotorie.ScoreRepository;
import claudodg.customerapi.service.ScoreAnalysisService;
import claudodg.customerapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
public class PageController {

    private final ScoreAnalysisService analysisService;
    private final ScoreRepository  scoreRepository;
    private final UserService userService;

    public PageController(ScoreAnalysisService analysisService, ScoreRepository scoreRepository, UserService userService) {
        this.analysisService = analysisService;
        this.scoreRepository = scoreRepository;
        this.userService = userService;
    }

    /** Public home page with a typing demo that works without an account. */
    @GetMapping("/")
    public String home() {
        return "home";
    }

    // Login requirement for these routes is enforced by Spring Security
    // (see security.SecurityConfig), so principal is never null here.

    @GetMapping("/azerType")
    public String azertype() {
        return "azerType";
    }

    @GetMapping("/progression")
    public String scoresPage(Principal principal, Model model) {

        User user = userService.getByEmail(principal.getName());

        try {
            Map<String, Object> analysis = analysisService.analyzeUserScores(user);
            model.addAttribute("analysis", analysis != null ? analysis : Map.of());
            return "progression";

        } catch (Exception e) {
            // The Python analysis service is down or unreachable.
            model.addAttribute("analysis", Map.of());
            model.addAttribute("serviceUnavailable", true);
            return "progression";
        }
    }

    @PostMapping("/score")
    public ResponseEntity<String> saveScore(
            @RequestBody Map<String, Integer> body,
            Principal principal) {
        User user = userService.getByEmail(principal.getName());
        Score score = new Score();
        score.setValeur(body.get("score"));
        score.setDate(LocalDateTime.now());
        score.setUser(user);
        scoreRepository.save(score);
        return ResponseEntity.ok("Score saved");
    }


}
