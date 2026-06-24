package com.mipt.hw.service;

import com.mipt.hw.dto.TaskUpdateDto;
import com.mipt.hw.exception.TaskNotFoundException;
import com.mipt.hw.mapper.TaskMapper;
import com.mipt.hw.model.Priority;
import com.mipt.hw.model.Task;
import com.mipt.hw.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  @Mock
  private TaskRepository taskRepository;

  @Mock
  private TaskMapper taskMapper;

  private TaskService taskService;

  @BeforeEach
  void setUp() {
    taskService = new TaskService(taskRepository, taskMapper);
    taskService.init();
  }

  @Test
  void shouldCreateTask() {
    // Given
    Task task = new Task();
    task.setTitle("Test Task");
    task.setDescription("Test Description");
    task.setPriority(Priority.HIGH);

    UUID taskId = UUID.randomUUID();

    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
      Task saved = invocation.getArgument(0);
      saved.setId(taskId);  // Устанавливаем ID при сохранении
      return saved;
    });

    // When
    Task result = taskService.createTask(task);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(taskId);  // Проверяем, что ID установлен
    assertThat(result.getTitle()).isEqualTo("Test Task");
    verify(taskRepository).save(task);
    assertThat(taskService.getTaskCache()).containsKey(taskId);
  }

  @Test
  void shouldThrowExceptionWhenTitleIsEmpty() {
    // Given
    Task task = new Task();
    task.setTitle("");

    // When & Then
    assertThatThrownBy(() -> taskService.createTask(task))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Title cannot be empty");
  }

  @Test
  void shouldThrowExceptionWhenTitleIsNull() {
    // Given
    Task task = new Task();
    task.setTitle(null);

    // When & Then
    assertThatThrownBy(() -> taskService.createTask(task))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Title cannot be empty");
  }

  @Test
  void shouldCreateTaskWithTitleAndDescription() {
    // Given
    when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
      Task t = invocation.getArgument(0);
      t.setId(UUID.randomUUID());
      return t;
    });

    // When
    Task result = taskService.createTask("Test Task", "Test Description");

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("Test Task");
    assertThat(result.getDescription()).isEqualTo("Test Description");
    assertThat(result.getCompleted()).isFalse();
    assertThat(result.getPriority()).isEqualTo(Priority.MEDIUM);
    verify(taskRepository).save(any(Task.class));
  }

  @Test
  void shouldGetTaskById() {
    // Given
    UUID id = UUID.randomUUID();
    Task task = new Task();
    task.setId(id);
    task.setTitle("Test Task");

    when(taskRepository.findById(id)).thenReturn(Optional.of(task));

    // When
    Task result = taskService.getTask(id);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getTitle()).isEqualTo("Test Task");
  }

  @Test
  void shouldThrowExceptionWhenTaskNotFound() {
    // Given
    UUID id = UUID.randomUUID();
    when(taskRepository.findById(id)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> taskService.getTask(id))
      .isInstanceOf(TaskNotFoundException.class)
      .hasMessage("Task not found with id: " + id);
  }

  @Test
  void shouldGetTaskOptional() {
    // Given
    UUID id = UUID.randomUUID();
    Task task = new Task();
    task.setId(id);
    task.setTitle("Test Task");

    when(taskRepository.findById(id)).thenReturn(Optional.of(task));

    // When
    Optional<Task> result = taskService.getTaskOpt(id);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(id);
  }

  @Test
  void shouldReturnEmptyOptionalWhenTaskNotFound() {
    // Given
    UUID id = UUID.randomUUID();
    when(taskRepository.findById(id)).thenReturn(Optional.empty());

    // When
    Optional<Task> result = taskService.getTaskOpt(id);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldGetAllTasks() {
    // Given
    List<Task> tasks = Arrays.asList(new Task(), new Task());
    when(taskRepository.findAll()).thenReturn(tasks);

    // When
    List<Task> result = taskService.getAllTasks();

    // Then
    assertThat(result).hasSize(2);
    verify(taskRepository, times(2)).findAll();
  }

  @Test
  void shouldUpdateTaskWithDto() {
    // Given
    UUID id = UUID.randomUUID();
    Task existingTask = new Task();
    existingTask.setId(id);
    existingTask.setTitle("Old Title");
    existingTask.setDescription("Old Description");

    TaskUpdateDto updateDto = new TaskUpdateDto();
    updateDto.setTitle("New Title");
    updateDto.setDescription("New Description");

    // Мокаем обновление через маппер
    doAnswer(invocation -> {
      TaskUpdateDto dto = invocation.getArgument(0);
      Task task = invocation.getArgument(1);
      if (dto.getTitle() != null) {
        task.setTitle(dto.getTitle());
      }
      if (dto.getDescription() != null) {
        task.setDescription(dto.getDescription());
      }
      if (dto.getCompleted() != null) {
        task.setCompleted(dto.getCompleted());
      }
      if (dto.getPriority() != null) {
        task.setPriority(dto.getPriority());
      }
      if (dto.getDueDate() != null) {
        task.setDueDate(dto.getDueDate());
      }
      if (dto.getTags() != null) {
        task.setTags(dto.getTags());
      }
      return null;
    }).when(taskMapper).updateEntity(any(TaskUpdateDto.class), any(Task.class));

    when(taskRepository.findById(id)).thenReturn(Optional.of(existingTask));
    when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

    // When
    Task result = taskService.updateTask(id, updateDto);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("New Title");
    assertThat(result.getDescription()).isEqualTo("New Description");
    verify(taskMapper).updateEntity(updateDto, existingTask);
    verify(taskRepository).save(existingTask);
    assertThat(taskService.getTaskCache()).containsKey(id);
  }

  @Test
  void shouldUpdateTaskManually() {
    // Given
    UUID id = UUID.randomUUID();
    Task existingTask = new Task();
    existingTask.setId(id);
    existingTask.setTitle("Old Title");
    existingTask.setDescription("Old Description");
    existingTask.setCompleted(false);
    existingTask.setPriority(Priority.LOW);

    when(taskRepository.findById(id)).thenReturn(Optional.of(existingTask));
    when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

    // When
    Task result = taskService.updateTaskManual(
      id,
      "New Title",
      "New Description",
      true,
      Priority.HIGH
    );

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("New Title");
    assertThat(result.getDescription()).isEqualTo("New Description");
    assertThat(result.getCompleted()).isTrue();
    assertThat(result.getPriority()).isEqualTo(Priority.HIGH);
    verify(taskRepository).save(existingTask);
  }

  @Test
  void shouldToggleTaskStatus() {
    // Given
    UUID id = UUID.randomUUID();
    Task task = new Task();
    task.setId(id);
    task.setCompleted(false);

    when(taskRepository.findById(id)).thenReturn(Optional.of(task));
    when(taskRepository.save(any(Task.class))).thenReturn(task);

    // When
    Task result = taskService.toggleTaskStatus(id);

    // Then
    assertThat(result.getCompleted()).isTrue();
    verify(taskRepository).save(task);
  }

  @Test
  void shouldDeleteTask() {
    // Given
    UUID id = UUID.randomUUID();
    when(taskRepository.existsById(id)).thenReturn(true);
    doNothing().when(taskRepository).deleteById(id);

    // When
    taskService.deleteTask(id);

    // Then
    verify(taskRepository).deleteById(id);
    assertThat(taskService.getTaskCache()).doesNotContainKey(id);
  }

  @Test
  void shouldThrowExceptionWhenDeletingNonExistentTask() {
    // Given
    UUID id = UUID.randomUUID();
    when(taskRepository.existsById(id)).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> taskService.deleteTask(id))
      .isInstanceOf(TaskNotFoundException.class)
      .hasMessage("Task not found with id: " + id);
  }

  @Test
  void shouldGetTaskCount() {
    // Given
    when(taskRepository.count()).thenReturn(5L);

    // When
    long count = taskService.getTaskCount();

    // Then
    assertThat(count).isEqualTo(5);
    verify(taskRepository).count();
  }

  @Test
  void shouldGetTasksByStatus() {
    // Given
    Task task1 = new Task();
    task1.setCompleted(true);
    Task task2 = new Task();
    task2.setCompleted(false);
    List<Task> tasks = Arrays.asList(task1, task2);

    when(taskRepository.findAll()).thenReturn(tasks);

    // When
    List<Task> completedTasks = taskService.getTasksByStatus(true);

    // Then
    assertThat(completedTasks).hasSize(1);
    assertThat(completedTasks.get(0).getCompleted()).isTrue();
  }

  @Test
  void shouldCheckTaskExists() {
    // Given
    UUID id = UUID.randomUUID();
    when(taskRepository.existsById(id)).thenReturn(true);

    // When
    boolean exists = taskService.taskExists(id);

    // Then
    assertThat(exists).isTrue();
  }

  @Test
  void shouldDeleteMultipleTasks() {
    // Given
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    when(taskRepository.existsById(any(UUID.class))).thenReturn(true);
    doNothing().when(taskRepository).deleteById(any(UUID.class));

    // When
    taskService.deleteTasks(Arrays.asList(id1, id2));

    // Then
    verify(taskRepository, times(2)).deleteById(any(UUID.class));
  }
}
