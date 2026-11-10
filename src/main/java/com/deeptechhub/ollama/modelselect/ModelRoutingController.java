package com.deeptechhub.ollama.modelselect;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/model")
public class ModelRoutingController {
    private final ChatClient chatClient;
    private final ModelRouter modelRouter;

    public ModelRoutingController(ChatClient.Builder chatClientBuilder, ModelRouter modelRouter) {
        this.chatClient = chatClientBuilder.build();
        this.modelRouter = modelRouter;
    }

    @GetMapping("/chat")
    public Flux<String> chat(
            @RequestParam String message,
            @RequestParam ModelType modelType
    ) {
        String model = modelRouter.selectModel(modelType);

        return chatClient.prompt()
                .options(
                        OllamaChatOptions.builder()
                                .model(model)
                )
                .user(message)
                .stream()
                .content();
    }

}
