package com.mipt.hw.repository;

import com.mipt.hw.model.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository {

  void save(Task task);

  Task update(Task task);

  void delete(UUID id);

  void init();

  List<Task> findAll();

  Optional<Task> find(UUID id);

  boolean existsById(UUID id);

}
