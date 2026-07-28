package com.training.redmath.chat.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeTools {

  @Tool(description = "Get the current date and time for a given IANA timezone, e.g. Asia/Karachi or UTC")
  public String getCurrentDateTime(
      @ToolParam(description = "IANA timezone id, e.g. 'Asia/Karachi'. Defaults to UTC if omitted.")
      String timezone) {

    ZoneId zoneId = (timezone == null || timezone.isBlank())
        ? ZoneId.of("UTC")
        : ZoneId.of(timezone);

    return LocalDateTime.now(zoneId)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  }
}