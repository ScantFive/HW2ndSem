package com.mipt.hw.repository;

import com.mipt.hw.model.Task;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Primary
public class InMemoryTaskRepository implements TaskRepository {
  private final Map<UUID, Task> tasks = new ConcurrentHashMap<>();

  @Override
  public List<Task> findAll() {
    return new ArrayList<>(tasks.values());
  }

  @Override
  public Optional<Task> find(UUID id) {
    return Optional.ofNullable(tasks.get(id));
  }

  @Override
  public boolean existsById(UUID id) {
    return tasks.containsKey(id);
  }

  @Override
  public void save(Task task) {
    if (task.getId() == null) {
      task.setId(UUID.randomUUID());
    }
    tasks.put(task.getId(), task);
  }

  @Override
  public Task update(Task task) {
    if (task.getId() != null && tasks.containsKey(task.getId())) {
      tasks.put(task.getId(), task);
      return task;
    }
    throw new IllegalArgumentException("Task not found with id: " + task.getId());
  }

  public void delete(UUID id) {
    tasks.remove(id);
  }

  @Override
  public void init() {

  }
}
