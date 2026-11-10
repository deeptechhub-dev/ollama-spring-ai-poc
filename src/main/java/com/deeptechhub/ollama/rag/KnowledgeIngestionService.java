package com.deeptechhub.ollama.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeIngestionService {
    private final VectorStore vectorStore;

    public KnowledgeIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void ingestSpringKnowledge() {
        Resource resource = new ClassPathResource("knowledge/spring-ai.txt");

        TextReader textReader = new TextReader(resource);
        textReader.getCustomMetadata().put("type", "spring-ai-doc");
        List<Document> documents = textReader.read();

        vectorStore.add(documents);
    }

    public void ingestSpringKnowledgeInChunks() {
        Resource resource = new ClassPathResource("knowledge/spring-ai.txt");

        TextReader textReader = new TextReader(resource);
        textReader.getCustomMetadata().put("type", "spring-ai-doc");

        List<Document> documents = textReader.read();
        TokenTextSplitter textSplitter = TokenTextSplitter.builder()
                .build();

        List<Document> chunks = textSplitter.apply(documents);
        vectorStore.add(chunks);
    }

}
