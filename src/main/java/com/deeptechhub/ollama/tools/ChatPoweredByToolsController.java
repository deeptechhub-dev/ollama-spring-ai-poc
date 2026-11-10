package com.deeptechhub.ollama.tools;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tools")
public class ChatPoweredByToolsController {
    private final ChatClient chatClient;
    private final DateTimeTools dateTimeTools;
    private final ProductTools productTools;
    private final OrderTools orderTools;
    private final StructuredProductTools structuredProductTools;

    public ChatPoweredByToolsController(ChatClient.Builder chatClientBuilder,
                                        DateTimeTools dateTimeTools, ProductTools productTools, OrderTools orderTools,
                                        StructuredProductTools structuredProductTools) {
        this.chatClient = chatClientBuilder.build();
        this.dateTimeTools = dateTimeTools;
        this.productTools = productTools;
        this.orderTools = orderTools;
        this.structuredProductTools = structuredProductTools;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return chatClient
                .prompt(message)
                .tools(dateTimeTools)
                .call()
                .content();

    }

    @GetMapping("/chat-with-tools")
    public String chatWithTools(@RequestParam String message) {
        return chatClient
                .prompt(message)
                .tools(dateTimeTools, productTools, orderTools)
                .call()
                .content();
    }

    @GetMapping("/chat-structured")
    public String chatStructured(@RequestParam String message) {
        return chatClient
                .prompt(message)
                .tools(structuredProductTools)
                .call()
                .content();
    }
}
