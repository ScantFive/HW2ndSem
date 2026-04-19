package com.mipt.hw.service;

import com.mipt.hw.dto.TaskUpdateDto;
import com.mipt.hw.exception.TaskNotFoundException;
import com.mipt.hw.mapper.TaskMapper;
import com.mipt.hw.model.Priority;
import com.mipt.hw.model.Task;
import com.mipt.hw.repository.TaskRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TaskService {

  private final TaskRepository taskRepository;
  private final TaskMapper taskMapper;
  private static Logger log = LoggerFactory.getLogger(TaskService.class);
  private final Map<UUID, Task> taskCache = new ConcurrentHashMap<>();

  @Value("${app.name}")
  private String appName;

  @Value("${app.version}")
  private String appVersion;

  public TaskService(TaskRepository taskRepository, TaskMapper taskMapper) {
    this.taskRepository = taskRepository;
    this.taskMapper = taskMapper;
  }

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

  public Task createTask(Task task) {
    if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
      throw new IllegalArgumentException("Title cannot be empty");
    }
    taskRepository.save(task);
    taskCache.put(task.getId(), task);
    return task;
  }

  public Task createTask(String title, String description) {
    if (title == null || title.trim().isEmpty()) {
      throw new IllegalArgumentException("Title cannot be empty");
    }
    Task task = new Task(null, title, description, false, null, null, null);
    taskRepository.save(task);
    taskCache.put(task.getId(), task);
    return task;
  }

  public Optional<Task> getTaskOpt(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("ID cannot be null");
    }
    return taskRepository.find(id);
  }

  public Task getTask(UUID id) {
    return taskRepository.find(id)
      .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
  }

  public List<Task> getAllTasks() {
    return taskRepository.findAll();
  }

  public Task updateTask(UUID id, TaskUpdateDto updateDto) {
    Task existingTask = getTask(id);
    taskMapper.updateEntity(updateDto, existingTask);
    Task updatedTask = taskRepository.update(existingTask);
    taskCache.put(id, updatedTask);
    return updatedTask;
  }

  public Task updateTask(UUID id, Task updatedTask) {
    Task existingTask = getTask(id);
    existingTask.setTitle(updatedTask.getTitle());
    existingTask.setDescription(updatedTask.getDescription());
    existingTask.setCompleted(updatedTask.getCompleted());
    existingTask.setDueDate(updatedTask.getDueDate());
    existingTask.setPriority(updatedTask.getPriority());
    existingTask.setTags(updatedTask.getTags());

    Task savedTask = taskRepository.update(existingTask);
    taskCache.put(id, savedTask);
    return savedTask;
  }

  public Task updateTaskManual(UUID id, String title, String description,
                               Boolean completed, Priority priority) {
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
    if (priority != null) {
      existingTask.setPriority(priority);
    }

    Task updatedTask = taskRepository.update(existingTask);
    taskCache.put(id, updatedTask);
    return updatedTask;
  }

  public Task toggleTaskStatus(UUID id) {
    Task task = getTask(id);
    task.setCompleted(!task.getCompleted());
    Task updatedTask = taskRepository.update(task);
    taskCache.put(id, updatedTask);
    return updatedTask;
  }

  public void deleteTask(UUID id) {
    if (!taskRepository.existsById(id)) {
      throw new RuntimeException("Task not found with id: " + id);
    }
    taskRepository.delete(id);
    taskCache.remove(id);
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

  public static Logger getLog() {
    return log;
  }

  public static void setLog(Logger log) {
    TaskService.log = log;
  }
}
