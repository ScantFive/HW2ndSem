package com.mipt.hw.service;

import com.mipt.hw.dto.TaskUpdateDto;
import com.mipt.hw.exception.TaskNotFoundException;
import com.mipt.hw.model.Priority;
import com.mipt.hw.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TaskServiceIntegrationTest {

  @Autowired
  private TaskService taskService;

  @Autowired
  private AttachmentService attachmentService;

  private Task createTestTask(String title) {
    Task task = new Task();
    task.setTitle(title);
    task.setDescription("Test description");
    task.setPriority(Priority.MEDIUM);
    return taskService.createTask(task);
  }

  @BeforeEach
  void setUp() {
    List<Task> allTasks = taskService.getAllTasks();
    for (Task task : allTasks) {
      taskService.deleteTask(task.getId());
    }
  }

  @Test
  void shouldCreateTask() {
    // given
    Task task = new Task();
    task.setTitle("New Task");
    task.setDescription("Description");
    task.setPriority(Priority.HIGH);

    // when
    Task created = taskService.createTask(task);

    // then
    assertThat(created.getId()).isNotNull();
    assertThat(created.getTitle()).isEqualTo("New Task");
    assertThat(created.getCompleted()).isFalse();
    assertThat(created.getCreatedAt()).isNotNull();
    assertThat(created.getUpdatedAt()).isNotNull();
  }

  @Test
  void shouldGetTask() {
    // given
    Task created = createTestTask("Test Task");

    // when
    Task found = taskService.getTask(created.getId());

    // then
    assertThat(found).isNotNull();
    assertThat(found.getTitle()).isEqualTo("Test Task");
  }

  @Test
  void shouldThrowExceptionWhenTaskNotFound() {
    // given
    UUID nonExistentId = UUID.randomUUID();

    // when & then
    assertThatThrownBy(() -> taskService.getTask(nonExistentId))
      .isInstanceOf(TaskNotFoundException.class)
      .hasMessageContaining("Task not found with id: " + nonExistentId);
  }

  @Test
  void shouldUpdateTask() {
    // given
    Task created = createTestTask("Old Title");
    TaskUpdateDto updateDto = new TaskUpdateDto();
    updateDto.setTitle("New Title");
    updateDto.setPriority(Priority.HIGH);

    // when
    Task updated = taskService.updateTask(created.getId(), updateDto);

    // then
    assertThat(updated.getTitle()).isEqualTo("New Title");
    assertThat(updated.getPriority()).isEqualTo(Priority.HIGH);
    assertThat(updated.getUpdatedAt()).isNotNull();
  }

  @Test
  void shouldDeleteTask() {
    // given
    Task created = createTestTask("Task to delete");
    UUID id = created.getId();

    // when
    taskService.deleteTask(id);

    // then
    assertThatThrownBy(() -> taskService.getTask(id))
      .isInstanceOf(TaskNotFoundException.class);
  }

  // ===== ТЕСТ 6: Получение всех задач =====
  @Test
  void shouldGetAllTasks() {
    // given
    createTestTask("Task 1");
    createTestTask("Task 2");

    // when
    List<Task> allTasks = taskService.getAllTasks();

    // then
    assertThat(allTasks).hasSize(2);
  }

  @Test
  void shouldRollbackTransactionOnError() {
    // given
    Task task1 = createTestTask("Task 1");
    Task task2 = createTestTask("Task 2");
    List<UUID> ids = List.of(task1.getId(), task2.getId(), UUID.randomUUID());

    // when & then
    assertThatThrownBy(() -> taskService.bulkCompleteTasks(ids))
      .isInstanceOf(TaskNotFoundException.class);

    // Проверяем, что задачи НЕ обновились (откат сработал)
    Task found1 = taskService.getTask(task1.getId());
    Task found2 = taskService.getTask(task2.getId());

    assertThat(found1.getCompleted()).isFalse();
    assertThat(found2.getCompleted()).isFalse();
  }

  @Test
  void shouldBulkCompleteTasksSuccessfully() {
    // given
    Task task1 = createTestTask("Task 1");
    Task task2 = createTestTask("Task 2");
    List<UUID> ids = List.of(task1.getId(), task2.getId());

    // when
    taskService.bulkCompleteTasks(ids);

    // then
    Task found1 = taskService.getTask(task1.getId());
    Task found2 = taskService.getTask(task2.getId());

    assertThat(found1.getCompleted()).isTrue();
    assertThat(found2.getCompleted()).isTrue();
  }

  @Test
  void shouldUpdateCacheAfterTaskUpdate() {
    // given
    Task created = createTestTask("Original Title");

    // when
    TaskUpdateDto updateDto = new TaskUpdateDto();
    updateDto.setTitle("Updated Title");
    taskService.updateTask(created.getId(), updateDto);

    Task cached = taskService.getTaskCache().get(created.getId());

    // then
    assertThat(cached).isNotNull();
    assertThat(cached.getTitle()).isEqualTo("Updated Title");
  }
}
