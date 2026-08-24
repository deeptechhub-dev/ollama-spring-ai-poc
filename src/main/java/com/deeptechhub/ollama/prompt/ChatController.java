package com.deeptechhub.ollama.prompt;

import com.deeptechhub.ollama.prompt.dto.DesignReview;
import com.deeptechhub.ollama.prompt.dto.DesignReviewRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChatController {

    private final AiService aiService;

    public ChatController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return aiService.chat(message);
    }

    @GetMapping("/chat-model")
    public String chatModel(@RequestParam("message") String message) {
        return aiService.chatModel(message);
    }

    @GetMapping("/chat-with-options")
    public String chatWithOptions(@RequestParam String message) {
        return aiService.chatWithOptions(message);
    }

    @GetMapping("/design-review")
    public String designReview(
            @RequestParam String topic,
            @RequestParam String difficulty,
            @RequestParam String focus) {
        return aiService.designReview(topic, difficulty, focus);
    }

    @GetMapping("/design-review-few-shots")
    public String designReviewFewShots(
            @RequestParam String topic,
            @RequestParam String difficulty,
            @RequestParam String focus) {
        return aiService.designReviewFewShot(topic, difficulty, focus);
    }

    @GetMapping("/design-review-json")
    public String designReviewJson(
            @RequestParam String topic,
            @RequestParam String difficulty,
            @RequestParam String focus) {
        return aiService.designReviewJson(topic, difficulty, focus);
    }

    @GetMapping("/design-review-structured")
    public DesignReview designReviewStructured(
            @RequestParam String topic,
            @RequestParam String difficulty,
            @RequestParam String focus) {
        return aiService.designReviewStructured(topic, difficulty, focus);
    }

    @GetMapping("/design-review-valid-structured")
    public DesignReview designReviewValidStructured(
            @RequestParam String topic,
            @RequestParam String difficulty,
            @RequestParam String focus) {
        return aiService.designReviewValidStructured(topic, difficulty, focus);
    }

    @PostMapping("/design-review")
    public DesignReview doDesignReview(@RequestBody @Valid DesignReviewRequest designReviewRequest) {
        return aiService.doDesignReview(designReviewRequest);
    }

}
