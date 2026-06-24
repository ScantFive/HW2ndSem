package com.mipt.hw.dto;

import com.mipt.hw.model.Priority;
import com.mipt.hw.valid.OnCreate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

@Schema(description = "DTO для создания новой задачи")
public class TaskCreateDto {

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public LocalDate getDueDate() { return dueDate; }
  public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
  public Priority getPriority() { return priority; }
  public void setPriority(Priority priority) { this.priority = priority; }
  public Set<String> getTags() { return tags; }
  public void setTags(Set<String> tags) { this.tags = tags; }

  @Schema(description = "Заголовок задачи", example = "Complete homework", required = true)
  @NotBlank(message = "Title is required", groups = OnCreate.class)
  @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters", groups = OnCreate.class)
  private String title;

  @Schema(description = "Описание задачи", example = "Finish all tasks for the semester")
  @Size(max = 500, message = "Description cannot exceed 500 characters", groups = OnCreate.class)
  private String description;

  @Schema(description = "Дата выполнения", example = "2026-12-31")
  @FutureOrPresent(message = "Due date must be in the present or future", groups = OnCreate.class)
  private LocalDate dueDate;

  @Schema(description = "Приоритет задачи", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"})
  @NotNull(message = "Priority is required", groups = OnCreate.class)
  private Priority priority;

  @Schema(description = "Теги задачи", example = "[\"urgent\", \"important\"]")
  @Size(max = 5, message = "Maximum 5 tags allowed", groups = OnCreate.class)
  private Set<String> tags;

  public TaskCreateDto() {
  }

  public TaskCreateDto(String title, String description, LocalDate dueDate,
                       Priority priority, Set<String> tags) {
    this.title = title;
    this.description = description;
    this.dueDate = dueDate;
    this.priority = priority;
    this.tags = tags;
  }
}
