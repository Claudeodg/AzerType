package claudodg.customerapi.controller;

import claudodg.customerapi.entity.Score;
import claudodg.customerapi.entity.User;
import claudodg.customerapi.reposotorie.ScoreRepository;
import claudodg.customerapi.service.ScoreAnalysisService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
public class PageController {

    private final ScoreAnalysisService analysisService;
    private final ScoreRepository  scoreRepository;

    public PageController(ScoreAnalysisService analysisService,ScoreRepository  scoreRepository) {
        this.analysisService = analysisService;
        this.scoreRepository = scoreRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/auth/register";
    }

    @GetMapping("/azerType")
    public String azertype(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/auth/login";
        }
        return "azerType";
    }

    @GetMapping("/progression")
    public String scoresPage(HttpSession session, Model model) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/auth/login";
        }
        try {

        Map<String, Object> analysis = analysisService.analyzeUserScores(user);
        model.addAttribute("analysis", analysis);
        return "progression";

        } catch (Exception e) {
            model.addAttribute("analysis", Map.of("error", "Service unavailable"));
            return "progression";
        }
    }

    @PostMapping("/score")
    public ResponseEntity<String> saveScore(
            @RequestBody Map<String, Integer> body,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).build();
        Score score = new Score();
        score.setValeur(body.get("score"));
        score.setDate(LocalDateTime.now());
        score.setUser(user);
        scoreRepository.save(score);
        return ResponseEntity.ok("Score saved");
    }


}
