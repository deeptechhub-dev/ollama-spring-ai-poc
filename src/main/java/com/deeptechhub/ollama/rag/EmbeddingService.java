package com.deeptechhub.ollama.rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {
    private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public float[] generateEmbedding(String text) {
        return embeddingModel.embed(text);
    }

    public double calculateSimilarity(String textA, String textB) {
        float[] embeddingA = generateEmbedding(textA);
        float[] embeddingB = generateEmbedding(textB);

        return VectorSimilarity.cosineSimilarity(embeddingA, embeddingB);
    }
}
