package com.deeptechhub.ollama.stream;

import java.io.IOException;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/multimodal")
public class MultimodalController {

    private final ChatClient chatClient;

    public MultimodalController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping(
            value = "/review",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public String review(
            @RequestParam("question") String question,
            @RequestParam("image") MultipartFile image) throws IOException {

        if (image.isEmpty()) {
            throw new IllegalArgumentException("The uploaded image is empty");
        }

        String contentType = image.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException(
                    "The uploaded file must be an image");
        }

        MimeType mimeType = MimeTypeUtils.parseMimeType(contentType);

        Resource imageResource = new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        };

        return chatClient
                .prompt()
                .options(
                        OllamaChatOptions.builder()
                                .model("qwen3-vl:2b-instruct")
                )
                .user(user -> user
                        .text(question)
                        .media(mimeType, imageResource))
                .call()
                .content();
    }

    @PostMapping(
            value = "/review-stream",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> reviewStream(
            @RequestParam("question") String question,
            @RequestParam("image") MultipartFile image) throws IOException {

        if (image.isEmpty()) {
            throw new IllegalArgumentException("The uploaded image is empty");
        }

        String contentType = image.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException(
                    "The uploaded file must be an image");
        }

        MimeType mimeType = MimeTypeUtils.parseMimeType(contentType);

        Resource imageResource = new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        };

        return chatClient
                .prompt()
                .options(
                        OllamaChatOptions.builder()
                                .model("qwen3-vl:2b-instruct")
                )
                .user(user -> user
                        .text(question)
                        .media(mimeType, imageResource))
                .stream()
                .content();
    }
}
