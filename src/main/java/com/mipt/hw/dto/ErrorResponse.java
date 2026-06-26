package com.mipt.hw.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "DTO для ответа с ошибкой")
public class ErrorResponse {
  @Schema(description = "Время ошибки", example = "2026-01-15T10:30:00Z")
  private Instant timestamp;

  @Schema(description = "HTTP статус", example = "400")
  private int status;

  @Schema(description = "Краткое описание ошибки", example = "Bad Request")
  private String error;

  @Schema(description = "Детальное сообщение для клиента", example = "Invalid request parameters")
  private String message;

  @Schema(description = "Путь запроса", example = "/api/tasks")
  private String path;

  @Schema(description = "Дополнительные детали")
  private Map<String, Object> details;

  public ErrorResponse() {
  }

  public ErrorResponse(Instant timestamp, int status, String error,
                       String message, String path, Map<String, Object> details) {
    this.timestamp = timestamp;
    this.status = status;
    this.error = error;
    this.message = message;
    this.path = path;
    this.details = details;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Map<String, Object> getDetails() {
    return details;
  }

  public void setDetails(Map<String, Object> details) {
    this.details = details;
  }

  public static ErrorResponseBuilder builder() {
    return new ErrorResponseBuilder();
  }

  public static class ErrorResponseBuilder {
    private Instant timestamp = Instant.now();
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, Object> details;

    public ErrorResponseBuilder timestamp(Instant timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public ErrorResponseBuilder status(int status) {
      this.status = status;
      return this;
    }

    public ErrorResponseBuilder error(String error) {
      this.error = error;
      return this;
    }

    public ErrorResponseBuilder message(String message) {
      this.message = message;
      return this;
    }

    public ErrorResponseBuilder path(String path) {
      this.path = path;
      return this;
    }

    public ErrorResponseBuilder details(Map<String, Object> details) {
      this.details = details;
      return this;
    }

    public ErrorResponse build() {
      return new ErrorResponse(timestamp, status, error, message, path, details);
    }
  }
}
