package com.mipt.hw.model;

import java.util.Objects;
import java.util.UUID;

public class Task {
  private UUID id;
  private String title;
  private String description;
  private boolean completed;

  public Task(UUID id, String title, String description, boolean completed) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.completed = completed;
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

  public void setId(UUID id) {
    this.id = id;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setCompleted(Boolean completed) {
    this.completed = completed;
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
