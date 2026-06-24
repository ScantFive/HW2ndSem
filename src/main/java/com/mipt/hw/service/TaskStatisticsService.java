package com.mipt.hw.service;

import com.mipt.hw.model.Task;
import com.mipt.hw.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TaskStatisticsService {
  private final TaskRepository primaryTaskRepository;
  private final TaskRepository stubTaskRepository;

  public TaskStatisticsService(
    TaskRepository primaryTaskRepository,
    @Qualifier("stubTaskRepository") TaskRepository stubTaskRepository
  ) {
    this.primaryTaskRepository = primaryTaskRepository;
    this.stubTaskRepository = stubTaskRepository;
  }

  public void compareRepositories() {
    List<Task> primaryTasks = primaryTaskRepository.findAll();
    List<Task> stubTasks = stubTaskRepository.findAll();

    System.out.println("=== Сравнение репозиториев ===");
    System.out.println("Основной репозиторий (" + primaryTaskRepository.getClass().getSimpleName() + "): "
      + primaryTasks.size() + " задач");
    System.out.println("Stub репозиторий (" + stubTaskRepository.getClass().getSimpleName() + "): "
      + stubTasks.size() + " задач");

    if (!primaryTasks.isEmpty()) {
      System.out.println("Первая задача из основного репозитория: " + primaryTasks.getFirst().getTitle());
    }
    if (!stubTasks.isEmpty()) {
      System.out.println("Первая задача из stub репозитория: " + stubTasks.getFirst().getTitle());
    }
  }

  public Map<String, Long> getStatisticsByRepository() {
    return Map.of(
      "primary", (long) primaryTaskRepository.findAll().size(),
      "stub", (long) stubTaskRepository.findAll().size()
    );
  }

  public List<Task> getAllTasksFromPrimary() {
    return primaryTaskRepository.findAll();
  }

  public List<Task> getAllTasksFromStub() {
    return stubTaskRepository.findAll();
  }

  public void showAllTasks() {
    System.out.println("=== Задачи из основного репозитория ===");
    primaryTaskRepository.findAll().forEach(System.out::println);

    System.out.println("\n=== Задачи из stub репозитория ===");
    stubTaskRepository.findAll().forEach(System.out::println);
  }
}