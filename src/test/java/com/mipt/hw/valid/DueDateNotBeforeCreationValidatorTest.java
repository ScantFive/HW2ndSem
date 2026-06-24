package com.mipt.hw.valid;

import com.mipt.hw.model.Task;
import com.mipt.hw.service.TaskService;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DueDateNotBeforeCreationValidatorTest {

  private DueDateNotBeforeCreationValidator validator;

  @Mock
  private TaskService taskService;

  @Mock
  private ConstraintValidatorContext context;

  @Mock
  private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

  @BeforeEach
  void setUp() {
    validator = new DueDateNotBeforeCreationValidator();
  }

  @Test
  void shouldReturnTrueWhenDueDateIsNull() {
    // Given
    TaskUpdateContext context = new TaskUpdateContext(
      UUID.randomUUID(),
      null,
      taskService
    );

    // When
    boolean result = validator.isValid(context, this.context);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnTrueWhenTaskIdIsNull() {
    // Given
    TaskUpdateContext context = new TaskUpdateContext(
      null,
      LocalDate.now().plusDays(1),
      taskService
    );

    // When
    boolean result = validator.isValid(context, this.context);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnTrueWhenDueDateIsAfterCreationDate() {
    // Given
    UUID taskId = UUID.randomUUID();
    LocalDate creationDate = LocalDate.of(2026, 1, 1);
    LocalDate dueDate = LocalDate.of(2026, 1, 15);

    Task task = new Task();
    task.setCreatedAt(creationDate.atStartOfDay());

    when(taskService.getTask(taskId)).thenReturn(task);

    TaskUpdateContext context = new TaskUpdateContext(
      taskId,
      dueDate,
      taskService
    );

    // When
    boolean result = validator.isValid(context, this.context);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnTrueWhenDueDateIsSameAsCreationDate() {
    // Given
    UUID taskId = UUID.randomUUID();
    LocalDate creationDate = LocalDate.of(2026, 1, 1);
    LocalDate dueDate = LocalDate.of(2026, 1, 1);

    Task task = new Task();
    task.setCreatedAt(creationDate.atStartOfDay());

    when(taskService.getTask(taskId)).thenReturn(task);

    TaskUpdateContext context = new TaskUpdateContext(
      taskId,
      dueDate,
      taskService
    );

    // When
    boolean result = validator.isValid(context, this.context);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnFalseWhenDueDateIsBeforeCreationDate() {
    // Given
    UUID taskId = UUID.randomUUID();
    LocalDate creationDate = LocalDate.of(2026, 1, 15);
    LocalDate dueDate = LocalDate.of(2026, 1, 1);

    Task task = new Task();
    task.setCreatedAt(creationDate.atStartOfDay());

    when(taskService.getTask(taskId)).thenReturn(task);
    when(context.buildConstraintViolationWithTemplate(anyString()))
      .thenReturn(violationBuilder);

    TaskUpdateContext context = new TaskUpdateContext(
      taskId,
      dueDate,
      taskService
    );

    // When
    boolean result = validator.isValid(context, this.context);

    // Then
    assertThat(result).isFalse();
  }
}
