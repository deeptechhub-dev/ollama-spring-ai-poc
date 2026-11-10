package com.deeptechhub.ollama.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class ZonedDateTimeTools {

    @Tool(description = "Get the current time for a specified time zone")
    public String getCurrentTime(
            @ToolParam(description = "Time zone ID, for example Europe/London")
            String zoneId
    ) {
        return LocalDateTime
                .now(ZoneId.of(zoneId))
                .toString();
    }

    @Tool(description = "Get the current date and time for a specified IANA time zone")
    public TimeResult getLocalDateTime(
            @ToolParam(description = "IANA time zone ID, for example Europe/London")  String zoneId) {

        if (zoneId == null || zoneId.isBlank()) {
            return new TimeResult(false, null, "Time zone ID must not be empty");
        }

        try {
            ZonedDateTime time = ZonedDateTime.now(ZoneId.of(zoneId));
            return new TimeResult(true, time.toString(), null);
        } catch (DateTimeException e) {
            return new TimeResult(false, null, "Invalid IANA time zone ID");
        }

    }
}

record TimeResult(
        boolean success,
        String time,
        String error
) {
}