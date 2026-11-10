package com.deeptechhub.ollama.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class StructuredProductTools {
    @Tool(
            description =
                    "Search products using a product query, optional category, minimum price and maximum price"
    )
    public String searchProducts(
            ProductSearchRequest request
    ) {

        return """
                Product search:
                Query: %s
                Category: %s
                Minimum price: %s
                Maximum price: %s
                """.formatted(
                request.query(),
                request.category(),
                request.minPrice(),
                request.maxPrice()
        );
    }
}
