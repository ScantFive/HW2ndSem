package com.mipt.hw.dto;

import com.mipt.hw.model.Priority;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Schema(description = "DTO для ответа с данными задачи")
public class TaskResponseDto {
  @Schema(description = "Уникальный идентификатор задачи", example = "123e4567-e89b-12d3-a456-426614174000")
  private UUID id;

  @Schema(description = "Заголовок задачи", example = "Complete homework")
  private String title;

  @Schema(description = "Описание задачи", example = "Finish all tasks for the semester")
  private String description;

  @Schema(description = "Статус выполнения", example = "false")
  private boolean completed;

  @Schema(description = "Дата и время создания", example = "2026-01-15T10:30:00")
  private LocalDateTime createdAt;

  @Schema(description = "Дата выполнения", example = "2026-12-31")
  private LocalDate dueDate;

  @Schema(description = "Приоритет", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"})
  private Priority priority;

  @Schema(description = "Теги", example = "[\"urgent\", \"important\"]")
  private Set<String> tags;

  public TaskResponseDto() {
  }

  public TaskResponseDto(UUID id, String title, String description, boolean completed,
                         LocalDateTime createdAt, LocalDate dueDate,
                         Priority priority, Set<String> tags) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.completed = completed;
    this.createdAt = createdAt;
    this.dueDate = dueDate;
    this.priority = priority;
    this.tags = tags;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  public Priority getPriority() {
    return priority;
  }

  public void setPriority(Priority priority) {
    this.priority = priority;
  }

  public Set<String> getTags() {
    return tags;
  }

  public void setTags(Set<String> tags) {
    this.tags = tags;
  }
}
