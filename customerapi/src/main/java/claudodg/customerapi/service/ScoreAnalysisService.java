package claudodg.customerapi.service;

import claudodg.customerapi.entity.Score;
import claudodg.customerapi.entity.User;
import claudodg.customerapi.reposotorie.ScoreRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.stream.Collectors;

import java.util.List;
import java.util.Map;

@Service
    public class ScoreAnalysisService {

        private final RestTemplate restTemplate;
        private final ScoreRepository scoreRepository;

        @Value("${python.service.url:http://localhost:5000}")
        private String pythonServiceUrl;

        public ScoreAnalysisService(ScoreRepository scoreRepository) {
            this.restTemplate = new RestTemplate();
            this.scoreRepository = scoreRepository;
        }

        public Map<String, Object> analyzeUserScores(User user) {
            // Fetch scores from DB
            List<Score> scores = scoreRepository.findByUserOrderByDateAsc(user);

            // Build payload for Python
            List<Map<String, ? extends Serializable>> payload = scores.stream()
                    .map(s -> Map.of(
                            "score", s.getValeur(),
                            "played_at", s.getDate().toString()
                    ))
                    .collect(Collectors.toList());

            // Call Python microservice
            Map<String, Object> request = Map.of("scores", payload);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    pythonServiceUrl + "/analyze",
                    request,
                    Map.class
            );

            return response.getBody();
        }
    }

