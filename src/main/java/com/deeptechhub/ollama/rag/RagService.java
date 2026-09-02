package com.deeptechhub.ollama.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {
    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public RagService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClientBuilder.build();
    }

    public void addDocument(String content) {
        Document document = new Document(content);
        vectorStore.add(List.of(document));
    }

    public List<Document> search(String query) {
        return vectorStore.similaritySearch(query);
    }

    public List<Document> controlledSearch(String query) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(3)
                .similarityThreshold(0.70)
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }

    public String ask(String question) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .similarityThreshold(0.70)
                .topK(3)
                .build();

        List<Document> documents =
                vectorStore.similaritySearch(searchRequest);

        if(documents.isEmpty()) {
            return "No relevant information could be found";
        }

        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        return chatClient.prompt()
                .user("""
                        Answer the question using only the provided context.
                        
                        Context:
                        %s
                        
                        Question:
                        %s
                        """.formatted(context, question))
                .call()
                .content();
    }

    public String askWithAdvisor(String question) {
        QuestionAnswerAdvisor advisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(
                        SearchRequest.builder()
                        .topK(3)
                        .similarityThreshold(0.70)
                        .build()
                )
                .build();

        return chatClient.prompt()
                .advisors(advisor)
                .user(question)
                .call()
                .content();
    }

}
