package com.deeptechhub.ollama.tools;

public record ProductSearchRequest(
        String query,
        String category,
        Double minPrice,
        Double maxPrice
) { }
