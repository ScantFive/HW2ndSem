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
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class TaskService {

  private final TaskRepository taskRepository;
  private final TaskMapper taskMapper;
  private static final Logger log = LoggerFactory.getLogger(TaskService.class);
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
    log.info("Initializing TaskService with app: {}, version: {}", appName, appVersion);
    refreshCache();
  }

  private void refreshCache() {
    taskCache.clear();
    for (Task task : taskRepository.findAll()) {
      taskCache.put(task.getId(), task);
    }
    log.info("Cache refreshed with {} tasks", taskCache.size());
  }

  @PreDestroy
  public void destroy() {
    try {
      long taskCount = getTaskCount();
      log.info("Shutting down TaskService with {} tasks in cache", taskCount);
    } catch (Exception e) {
      log.error("Error during shutdown: {}", e.getMessage());
    }
  }

  @Transactional
  public Task createTask(Task task) {
    if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
      throw new IllegalArgumentException("Title cannot be empty");
    }

    Task savedTask = taskRepository.save(task);
    taskCache.put(savedTask.getId(), savedTask);

    log.info("Created task: id={}, title={}", savedTask.getId(), savedTask.getTitle());
    return savedTask;
  }

  @Transactional
  public Task createTask(String title, String description) {
    if (title == null || title.trim().isEmpty()) {
      throw new IllegalArgumentException("Title cannot be empty");
    }
    Task task = new Task();
    task.setTitle(title);
    task.setDescription(description);
    task.setCompleted(false);
    task.setPriority(Priority.MEDIUM);

    return createTask(task);
  }

  public Optional<Task> getTaskOpt(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("ID cannot be null");
    }
    Task cached = taskCache.get(id);
    if (cached != null) {
      return Optional.of(cached);
    }
    return taskRepository.findById(id);
  }

  public Task getTask(UUID id) {
    return getTaskOpt(id)
      .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
  }

  public List<Task> getAllTasks() {
    return taskRepository.findAll();
  }

  @Transactional
  public Task updateTask(UUID id, TaskUpdateDto updateDto) {
    Task existingTask = getTask(id);
    taskMapper.updateEntity(updateDto, existingTask);

    Task updatedTask = taskRepository.save(existingTask);
    taskCache.put(id, updatedTask);

    log.info("Updated task: id={}, title={}", updatedTask.getId(), updatedTask.getTitle());
    return updatedTask;
  }

  @Transactional
  public Task updateTask(UUID id, Task updatedTask) {
    Task existingTask = getTask(id);

    existingTask.setTitle(updatedTask.getTitle());
    existingTask.setDescription(updatedTask.getDescription());
    existingTask.setCompleted(updatedTask.getCompleted());
    existingTask.setDueDate(updatedTask.getDueDate());
    existingTask.setPriority(updatedTask.getPriority());
    existingTask.setTags(updatedTask.getTags());

    Task savedTask = taskRepository.save(existingTask);
    taskCache.put(id, savedTask);

    return savedTask;
  }

  @Transactional
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

    Task savedTask = taskRepository.save(existingTask);
    taskCache.put(id, savedTask);

    return savedTask;
  }

  @Transactional
  public Task toggleTaskStatus(UUID id) {
    Task task = getTask(id);
    task.setCompleted(!task.getCompleted());

    Task updatedTask = taskRepository.save(task);
    taskCache.put(id, updatedTask);

    return updatedTask;
  }

  @Transactional
  public void deleteTask(UUID id) {
    if (!taskRepository.existsById(id)) {
      throw new TaskNotFoundException("Task not found with id: " + id);
    }
    taskRepository.deleteById(id);
    taskCache.remove(id);

    log.info("Deleted task: id={}", id);
  }

  @Transactional
  public void deleteTasks(List<UUID> ids) {
    for (UUID id : ids) {
      deleteTask(id);
    }
  }

  public boolean taskExists(UUID id) {
    return taskRepository.existsById(id);
  }

  public long getTaskCount() {
    return taskRepository.count();
  }

  public List<Task> getTasksByStatus(boolean completed) {
    return taskRepository.findAll().stream()
      .filter(task -> task.getCompleted() == completed)
      .toList();
  }

  @Transactional(rollbackFor = Exception.class)
  public void bulkCompleteTasks(List<UUID> ids) {
    if (ids == null || ids.isEmpty()) {
      throw new IllegalArgumentException("Task IDs list cannot be empty");
    }

    List<Task> tasksToUpdate = new ArrayList<>();
    for (UUID id : ids) {
      Task task = taskRepository.findById(id)
        .orElseThrow(() -> new TaskNotFoundException("Task not found: " + id));
      tasksToUpdate.add(task);
    }

    for (Task task : tasksToUpdate) {
      task.setCompleted(true);
    }

    taskRepository.saveAll(tasksToUpdate);

    tasksToUpdate.forEach(task -> taskCache.put(task.getId(), task));
  }

  public Map<UUID, Task> getTaskCache() {
    return taskCache;
  }
}
