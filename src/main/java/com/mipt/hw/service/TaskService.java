package com.mipt.hw.service;

import com.mipt.hw.model.Task;
import com.mipt.hw.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TaskService {

  private final TaskRepository taskRepository;
  private static Logger log = LoggerFactory.getLogger(TaskService.class);
  private final Map<UUID, Task> taskCache = new HashMap<>();

  @Value("${app.name}")
  private String appName;

  @Value("${app.version}")
  private String appVersion;


  @PostConstruct
  public void init() {
    for (Task task : taskRepository.findAll()) {
      taskCache.put(task.getId(), task);
    }
  }

  @PreDestroy
  public void destroy() {
    try {
      long taskCount = getTaskCount();
      System.out.println("Завершение работы с " + taskCount + " задачами в памяти");
    } catch (Exception e) {
      System.err.println("Ошибка при завершении: " + e.getMessage());
    }
  }

  public TaskService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  public static Logger getLog() {
    return log;
  }

  public static void setLog(Logger log) {
    TaskService.log = log;
  }

  public Task createTask(String title, String description) {
    if (title == null || title.trim().isEmpty()) {
      throw new IllegalArgumentException();
    }

    Task task = new Task(null, title, description, false);
    taskRepository.save(task); //ID тут сгенерится
    return task;
  }

  public Task createTask(Task task) {
    if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
      throw new IllegalArgumentException();
    }

    taskRepository.save(task);
    return task;
  }

  public Optional<Task> getTaskOpt(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException();
    }
    return taskRepository.find(id);
  }

  public Task getTask(UUID id) {
    return taskRepository.find(id)
      .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
  }

  public List<Task> getAllTasks() {
    return taskRepository.findAll();
  }

  public Task updateTask(UUID id, Task updatedTask) {
    Task existingTask = getTask(id);

    existingTask.setTitle(updatedTask.getTitle());
    existingTask.setDescription(updatedTask.getDescription());
    existingTask.setCompleted(updatedTask.getCompleted());

    return taskRepository.update(existingTask);
  }

  public Task updateTaskManual(UUID id, String title, String description, Boolean completed) {
    Task existingTask = getTask(id);

    if (title != null && !title.trim().isEmpty()) {
      existingTask.setTitle(title);
    }

    if (description != null) {
      existingTask.setDescription(description);
    }

    if (completed != null) {
      existingTask.setCompleted(completed);
    }

    return taskRepository.update(existingTask);
  }

  public Task toggleTaskStatus(UUID id) {
    Task task = getTask(id);
    task.setCompleted(!task.getCompleted());
    return taskRepository.update(task);
  }

  public void deleteTask(UUID id) {
    if (!taskRepository.existsById(id)) {
      throw new RuntimeException("Task not found with id: " + id);
    }
    taskRepository.delete(id);
  }

  public void deleteTasks(List<UUID> ids) {
    ids.forEach(this::deleteTask);
  }

  public boolean taskExists(UUID id) {
    return taskRepository.existsById(id);
  }

  public long getTaskCount() {
    return taskRepository.findAll().size();
  }

  public List<Task> getTasksByStatus(boolean completed) {
    return taskRepository.findAll().stream()
      .filter(task -> task.getCompleted() == completed)
      .toList();
  }

  public Map<UUID, Task> getTaskCache() {
    return taskCache;
  }
}
