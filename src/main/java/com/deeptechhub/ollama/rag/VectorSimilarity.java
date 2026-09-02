package com.deeptechhub.ollama.rag;

public class VectorSimilarity {

    public static double cosineSimilarity(float[] vectorA, float[] vectorB) {
        // Check dimension
        if(vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Dimensions of vectors do not match");
        }

        // Calculate Dot
        double dotProduct = 0;
        double squareSumA = 0;
        double squareSumB = 0;
        for(int i=0; i< vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];

            // Find square sum
            squareSumA += Math.pow(vectorA[i], 2);
            squareSumB += Math.pow(vectorB[i], 2);
        }

        double absA = Math.sqrt(squareSumA);
        double absB = Math.sqrt(squareSumB);

        return dotProduct / (absA * absB);
    }
}
