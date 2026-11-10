package com.deeptechhub.ollama.stream;

import com.deeptechhub.ollama.tools.OrderTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/stream")
public class StreamingController {
    private final ChatClient chatClient;
    private final OrderTools orderTools;

    public StreamingController(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory, OrderTools orderTools
    ) {
        this.orderTools = orderTools;
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor
                                .builder(chatMemory)
                                .build()
                )
                .build();
    }

    @GetMapping
    public Flux<String> stream(@RequestParam String message) {
        return chatClient
                .prompt()
                .user(message)
                .stream()
                .content();
    }

    @GetMapping(
            value = "/sse",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<String> streamSse(@RequestParam String message) {
        return chatClient
                .prompt()
                .user(message)
                .stream()
                .content();
    }

    @GetMapping(
            value = "/conversation-stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<String> conversationStream(
            @RequestParam String conversationId,
            @RequestParam String message) {

        return chatClient
                .prompt()
                .user(message)
                .advisors(advisor ->
                        advisor.param(
                                ChatMemory.CONVERSATION_ID,
                                conversationId))
                .stream()
                .content();
    }

    @GetMapping(
            value = "/conversation-tools",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<String> conversationTools(
            @RequestParam String conversationId,
            @RequestParam String message) {

        return chatClient
                .prompt()
                .user(message)
                .advisors(advisor ->
                        advisor.param(
                                ChatMemory.CONVERSATION_ID,
                                conversationId))
                .tools(orderTools)
                .stream()
                .content();
    }
}
