package com.mipt.hw.repository;

import com.mipt.hw.model.Priority;
import com.mipt.hw.model.Task;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Repository
@Primary
public class InMemoryTaskRepository implements TaskRepository {
  private final Map<UUID, Task> tasks = new ConcurrentHashMap<>();

  @Override
  public <S extends Task> List<S> saveAll(Iterable<S> entities) {
    return List.of();
  }

  @Override
  public List<Task> findAll() {
    return new ArrayList<>(tasks.values());
  }

  @Override
  public List<Task> findAllById(Iterable<UUID> uuids) {
    return List.of();
  }

  @Override
  public long count() {
    return 0;
  }

  @Override
  public Optional<Task> findById(UUID id) {
    return Optional.ofNullable(tasks.get(id));
  }

  @Override
  public boolean existsById(UUID id) {
    return tasks.containsKey(id);
  }

  @Override
  public Task save(Task task) {
    if (task.getId() == null) {
      task.setId(UUID.randomUUID());
    }
    tasks.put(task.getId(), task);
    return task;
  }

  public Task update(Task task) {
    if (task.getId() != null && tasks.containsKey(task.getId())) {
      tasks.put(task.getId(), task);
      return task;
    }
    throw new IllegalArgumentException("Task not found with id: " + task.getId());
  }

  @Override
  public void deleteById(UUID id) {
    tasks.remove(id);
  }

  @Override
  public void delete(Task entity) {

  }

  @Override
  public void deleteAllById(Iterable<? extends UUID> uuids) {

  }

  @Override
  public void deleteAll(Iterable<? extends Task> entities) {

  }

  @Override
  public void deleteAll() {

  }

  public void init() {
    Task task1 = new Task();
    task1.setTitle("Task 1");
    task1.setDescription("Description 1");
    save(task1);

    Task task2 = new Task();
    task2.setTitle("Task 2");
    task2.setDescription("Description 2");
    task2.setCompleted(true);
    save(task2);
  }

  @Override
  public List<Task> findByCompleted(boolean completed) {
    return List.of();
  }

  @Override
  public List<Task> findByPriority(Priority priority) {
    return List.of();
  }

  @Override
  public List<Task> findByCompletedAndPriority(boolean completed, Priority priority) {
    return List.of();
  }

  @Override
  public List<Task> findByDueDateBefore(LocalDate date) {
    return List.of();
  }

  @Override
  public List<Task> findByTitleContainingIgnoreCase(String keyword) {
    return List.of();
  }

  @Override
  public List<Task> findTasksDueBetween(LocalDate startDate, LocalDate endDate) {
    return List.of();
  }

  @Override
  public List<Task> findTasksDueWithinNextWeek() {
    return List.of();
  }

  @Override
  public Task findByIdWithAttachments(UUID id) {
    return null;
  }

  @Override
  public List<Task> findAllWithAttachments() {
    return List.of();
  }

  @Override
  public void flush() {

  }

  @Override
  public <S extends Task> S saveAndFlush(S entity) {
    return null;
  }

  @Override
  public <S extends Task> List<S> saveAllAndFlush(Iterable<S> entities) {
    return List.of();
  }

  @Override
  public void deleteAllInBatch(Iterable<Task> entities) {

  }

  @Override
  public void deleteAllByIdInBatch(Iterable<UUID> uuids) {

  }

  @Override
  public void deleteAllInBatch() {

  }

  @Override
  public Task getOne(UUID uuid) {
    return null;
  }

  @Override
  public Task getById(UUID uuid) {
    return null;
  }

  @Override
  public Task getReferenceById(UUID uuid) {
    return null;
  }

  @Override
  public <S extends Task> Optional<S> findOne(Example<S> example) {
    return Optional.empty();
  }

  @Override
  public <S extends Task> List<S> findAll(Example<S> example) {
    return List.of();
  }

  @Override
  public <S extends Task> List<S> findAll(Example<S> example, Sort sort) {
    return List.of();
  }

  @Override
  public <S extends Task> Page<S> findAll(Example<S> example, Pageable pageable) {
    return null;
  }

  @Override
  public <S extends Task> long count(Example<S> example) {
    return 0;
  }

  @Override
  public <S extends Task> boolean exists(Example<S> example) {
    return false;
  }

  @Override
  public <S extends Task, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
    return null;
  }

  @Override
  public List<Task> findAll(Sort sort) {
    return List.of();
  }

  @Override
  public Page<Task> findAll(Pageable pageable) {
    return null;
  }
}
