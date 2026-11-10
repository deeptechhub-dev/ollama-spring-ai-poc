package com.deeptechhub.ollama.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class ProductTools {

    @Tool(description = "Search the product catalogue using a product name or keyword")
    public String searchProducts(@ToolParam(description = "Product name or keyword to search for") String query) {
        return "Results for: " + query;
    }
}
