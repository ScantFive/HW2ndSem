package com.mipt.hw.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestScopedBean {
  private static final Logger log = LoggerFactory.getLogger(RequestScopedBean.class);
  private static int instanceCounter = 0;

  private final String requestId;
  private final LocalDateTime startTime;
  private final int instanceNumber;

  public RequestScopedBean() {
    this.requestId = UUID.randomUUID().toString();
    this.startTime = LocalDateTime.now();
    this.instanceNumber = ++instanceCounter;

    log.info("RequestScopedBean #{} создан с ID: {}", instanceNumber, requestId);
  }

  @PostConstruct
  public void init() {
    log.info("RequestScopedBean #{} инициализирован для запроса: {}",
      instanceNumber, requestId);
  }

  @PreDestroy
  public void destroy() {
    log.info("RequestScopedBean #{} уничтожен. Запрос {} завершен. Время жизни: {} мс",
      instanceNumber, requestId, getElapsedTimeMs());
  }

  public String getRequestId() {
    return requestId;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public String getFormattedStartTime() {
    return startTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
  }

  public long getElapsedTimeMs() {
    return java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();
  }

  public void logRequestInfo(String message) {
    log.info("[Request #{} | {}ms] {}", requestId.substring(0, 8),
      getElapsedTimeMs(), message);
  }
}
