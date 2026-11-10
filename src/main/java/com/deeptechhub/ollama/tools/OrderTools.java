package com.deeptechhub.ollama.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class OrderTools {

    @Tool(description = "Get the current status of an order using its order ID")
    public String getOrderStatus(@ToolParam(description = "The order ID") String orderId) {
        return "Order " + orderId + " is being processed";
    }
}
