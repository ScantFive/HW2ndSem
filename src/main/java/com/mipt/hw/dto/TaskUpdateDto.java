package com.mipt.hw.dto;

import com.mipt.hw.model.Priority;
import com.mipt.hw.valid.OnUpdate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

@Schema(description = "DTO для обновления задачи")
public class TaskUpdateDto {

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public Boolean getCompleted() { return completed; }
  public void setCompleted(Boolean completed) { this.completed = completed; }
  public LocalDate getDueDate() { return dueDate; }
  public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
  public Priority getPriority() { return priority; }
  public void setPriority(Priority priority) { this.priority = priority; }
  public Set<String> getTags() { return tags; }
  public void setTags(Set<String> tags) { this.tags = tags; }

  @Schema(description = "Заголовок задачи", example = "Updated homework title")
  @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters", groups = OnUpdate.class)
  private String title;

  @Schema(description = "Описание задачи", example = "Updated description")
  @Size(max = 500, message = "Description cannot exceed 500 characters", groups = OnUpdate.class)
  private String description;

  @Schema(description = "Статус выполнения", example = "true")
  private Boolean completed;

  @Schema(description = "Дата выполнения", example = "2026-12-31")
  @FutureOrPresent(message = "Due date must be in the present or future", groups = OnUpdate.class)
  private LocalDate dueDate;

  @Schema(description = "Приоритет задачи", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"})
  private Priority priority;

  @Schema(description = "Теги задачи", example = "[\"urgent\", \"important\", \"updated\"]")
  @Size(max = 5, message = "Maximum 5 tags allowed", groups = OnUpdate.class)
  private Set<String> tags;

  public TaskUpdateDto() {
  }

  public TaskUpdateDto(String title, String description, Boolean completed,
                       LocalDate dueDate, Priority priority, Set<String> tags) {
    this.title = title;
    this.description = description;
    this.completed = completed;
    this.dueDate = dueDate;
    this.priority = priority;
    this.tags = tags;
  }
}
