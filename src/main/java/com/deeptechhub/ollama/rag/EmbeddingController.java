package com.deeptechhub.ollama.rag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class EmbeddingController {
    private final EmbeddingService embeddingService;

    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @GetMapping("/embedding")
    public Map<String, Object> generateEmbedding(@RequestParam String text) {
        float[] embedding = embeddingService.generateEmbedding(text);

        return Map.of(
                "text", text,
                "dimensions", embedding.length,
                "embedding", embedding
        );
    }

    @GetMapping("/similarity")
    public Map<String, Object> calculateSimilarity(@RequestParam String textA,
                                                   @RequestParam String textB) {
        double similarity = embeddingService.calculateSimilarity(textA, textB);
        return Map.of(
                "textA", textA,
                "textB", textB,
                "similarity", similarity
        );
    }

}
