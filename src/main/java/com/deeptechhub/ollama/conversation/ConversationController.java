package com.deeptechhub.ollama.conversation;

import com.deeptechhub.ollama.prompt.AiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ConversationController {
    private final AiService aiService;
    private final TempConversationService tempConversationService;
    private final ConversationMemoryService conversationMemoryService;
    private final PersistentConversationMemoryService persistentConversationMemoryService;

    public ConversationController(AiService aiService,
                                  TempConversationService tempConversationService,
                                  ConversationMemoryService conversationMemoryService,
                                  PersistentConversationMemoryService persistentConversationMemoryService) {
        this.aiService = aiService;
        this.tempConversationService = tempConversationService;
        this.conversationMemoryService = conversationMemoryService;
        this.persistentConversationMemoryService = persistentConversationMemoryService;
    }

    @GetMapping("/conversation-basic")
    public String conversationBasic(@RequestParam String message) {
        return aiService.chatBasic(message);
    }

    @GetMapping("/conversation-with-temp-history")
    public String conversationWithTempHistory(@RequestParam String message) {
        return tempConversationService.chatBasic(message);
    }

    @GetMapping("/conversation-memory")
    public String conversationMemory(
            @RequestParam String conversationId,
            @RequestParam String message
    ) {
        return conversationMemoryService.chatWithConversationHistory(conversationId, message);
    }

    @GetMapping("/conversation-memory-limited")
    public String conversationMemoryLimited(
            @RequestParam String conversationId,
            @RequestParam String message
    ) {
        return conversationMemoryService.chatWithLimitedConversationHistory(conversationId, message);
    }

    @GetMapping("/conversation-memory-jdbc")
    public String conversationMemoryJdbc(
            @RequestParam String conversationId,
            @RequestParam String message
    ) {
        return persistentConversationMemoryService.chat(conversationId, message);
    }

}
