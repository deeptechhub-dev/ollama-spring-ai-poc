package com.deeptechhub.ollama.rag;

import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rag")
public class RagController {
    private final RagService ragService;
    private final KnowledgeIngestionService knowledgeIngestionService;

    public RagController(RagService ragService, KnowledgeIngestionService knowledgeIngestionService) {
        this.ragService = ragService;
        this.knowledgeIngestionService = knowledgeIngestionService;
    }

    @PostMapping("/document")
    public Map<String, String> addDocuments(@RequestBody String content) {
        ragService.addDocument(content);
        return Map.of(
                "message",
                "Sample documents added successfully"
        );
    }

    @PostMapping("/ingest/spring-ai")
    public Map<String, String> ingestSpringKnowledge() {
        knowledgeIngestionService.ingestSpringKnowledge();

        return Map.of(
                "message",
                "Spring AI knowledge ingested successfully"
        );
    }

    @GetMapping("/search")
    public List<Document> search(@RequestParam String query) {
        return ragService.search(query);
    }

    @GetMapping("/controlled-search")
    public List<Document> controlledSearch(@RequestParam String query) {
        return ragService.controlledSearch(query);
    }

    @GetMapping("/ask")
    public Map<String, String> ask(
            @RequestParam String question) {
        String answer = ragService.ask(question);
        return Map.of("answer", answer);
    }

    @GetMapping("/ask-advisor")
    public Map<String, String> askWithAdvisor(@RequestParam String question) {
        String answer = ragService.askWithAdvisor(question);
        return Map.of("answer", answer);
    }

}
