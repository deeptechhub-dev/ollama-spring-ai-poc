package com.deeptechhub.ollama.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateTimeTools {
    @Tool(description = "Get the current date and time")
    public String getCurrentDateTime() {
        return LocalDateTime
                .now()
                .toString();
    }

}


