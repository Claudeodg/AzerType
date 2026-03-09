package claudodg.customerapi.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;


/**
 * manage  the Api of gemini
 */
@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String geminiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    /**
     * Acts as a proxy between the frontend and the Google Gemini API.
     * Receives the prompt from the JavaScript, forwards it to Gemini
     * with the API key in the headers, and returns the generated response.
     *
     * @param body the request body sent by apigemini.js,
     *             containing the prompt and generation configuration
     * @return 200 OK with Gemini's response as JSON string
     */

    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<String> generate(@RequestBody Map<String, Object> body) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                geminiUrl,
                entity,
                String.class
        );
        return ResponseEntity.ok(response.getBody());
    }
}

