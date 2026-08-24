package com.deeptechhub.ollama.conversation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationMemoryService {

    private final ChatMemory chatMemory;
    private final ChatClient chatClient;
    private final ChatMemory messageWindowChatMemory;
    private static final String SYSTEM_MESSAGE = "You are a senior software architect and a system design expert.";

    public ConversationMemoryService(ChatMemory chatMemory,
                                     ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.chatMemory = chatMemory;
        this.messageWindowChatMemory = MessageWindowChatMemory.builder()
                .maxMessages(2).build(); // tiny window for testing
    }

    public String chatWithConversationHistory(String conversationId, String message) {
        // Get the existing conversation history
        List<Message> conversationHistory = chatMemory.get(conversationId);

        // Call the model with user message and conversation history
        String response = chatClient.prompt()
                .messages(conversationHistory)
                .system(SYSTEM_MESSAGE)
                .user(message)
                .call()
                .content();

        // Add to conversation history
        chatMemory.add(conversationId, new UserMessage(message));
        chatMemory.add(conversationId, new AssistantMessage(response));

        return response;
    }

    public String chatWithLimitedConversationHistory(String conversationId, String message) {
        // Get the existing conversation history
        List<Message> conversationHistory = messageWindowChatMemory.get(conversationId);

        // Call the model with user message and conversation history
        String response = chatClient.prompt()
                .messages(conversationHistory)
                .system(SYSTEM_MESSAGE)
                .user(message)
                .call()
                .content();

        // Add to conversation history
        messageWindowChatMemory.add(conversationId, new UserMessage(message));
        messageWindowChatMemory.add(conversationId, new AssistantMessage(response));

        return response;
    }
}
