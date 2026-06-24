package com.mipt.hw.valid;

import com.mipt.hw.service.TaskService;

import java.time.LocalDate;
import java.util.UUID;

public class TaskUpdateContext {
  private final UUID taskId;
  private final LocalDate dueDate;
  private final TaskService taskService;

  public TaskUpdateContext(UUID taskId, LocalDate dueDate, TaskService taskService) {
    this.taskId = taskId;
    this.dueDate = dueDate;
    this.taskService = taskService;
  }

  public UUID getTaskId() {
    return taskId;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public TaskService getTaskService() {
    return taskService;
  }
}