package com.deeptechhub.ollama.conversation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class TempConversationService {
    private final ChatClient chatClient;
    private final List<Message> conversationHistory;

    public TempConversationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.conversationHistory = new LinkedList<>();
    }

    public String chatBasic(String message) {

        String response = chatClient.prompt()
                .messages(conversationHistory)
                .user(message)
                .system("You are a senior software architect and system design expert.")
                .call()
                .content();


        conversationHistory.add(new UserMessage(message));
        conversationHistory.add(new AssistantMessage(response));
        return response;
    }
}
