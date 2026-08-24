package com.deeptechhub.ollama.conversation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersistentConversationMemoryService {
    private final ChatMemory chatMemory;
    private final ChatClient chatClient;

    private static final String SYSTEM_MESSAGE = "You are a senior software architect and a system design expert.";

    public PersistentConversationMemoryService(ChatMemory chatMemory, ChatClient.Builder chatClientBuilder) {
        this.chatMemory = chatMemory;
        this.chatClient = chatClientBuilder.build();
    }

    public String chat(String conversationId, String message) {

        List<Message> conversationHistory = chatMemory.get(conversationId);

        String response = chatClient.prompt()
                .system(SYSTEM_MESSAGE)
                .messages(conversationHistory)
                .user(message)
                .call()
                .content();

        chatMemory.add(conversationId, new UserMessage(message));
        chatMemory.add(conversationId, new AssistantMessage(response));

        return response;
    }
}
