package com.mipt.hw.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Task {
  private UUID id;
  private String title;
  private String description;
  private boolean completed;
  private final LocalDateTime createdAt;
  private LocalDate dueDate;
  private Priority priority;
  private Set<String> tags;

  public Task(UUID id, String title, String description, boolean completed, LocalDate dueDate, Priority priority, Set<String> tags) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.completed = completed;
    this.createdAt = LocalDateTime.now();
    this.dueDate = dueDate;
    this.priority = priority;
    this.tags = tags;

  }

  public UUID getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public String getTitle() {
    return title;
  }

  public Boolean getCompleted() {
    return completed;
  }

  public LocalDateTime getCreatedAt() { return createdAt; }

  public LocalDate getDueDate() { return dueDate; }

  public Priority getPriority() {
    return priority;
  }

  public Set<String> getTags() {
    return tags;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

  public void setPriority(Priority priority) {
    this.priority = priority;
  }

  public void setTags(Set<String> tags) {
    this.tags = tags;
  }

  @Override
  public String toString() {
    return "Task{" +
      "id=" + id +
      ", title='" + title + '\'' +
      ", description='" + description + '\'' +
      ", completed=" + completed +
      '}';
  }

  @Override
  public boolean equals(Object object) {
    if (object == null || getClass() != object.getClass()) return false;
    Task task = (Task) object;
    return Objects.equals(id, task.id) && Objects.equals(title, task.title) && Objects.equals(description, task.description) && Objects.equals(completed, task.completed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, description, completed);
  }

}
