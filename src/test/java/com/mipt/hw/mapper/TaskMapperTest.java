package com.mipt.hw.mapper;

import com.mipt.hw.dto.TaskCreateDto;
import com.mipt.hw.dto.TaskResponseDto;
import com.mipt.hw.dto.TaskUpdateDto;
import com.mipt.hw.model.Priority;
import com.mipt.hw.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TaskMapperTest {

  private TaskMapper taskMapper;

  @BeforeEach
  void setUp() {
    taskMapper = Mappers.getMapper(TaskMapper.class);
  }

  @Test
  void shouldMapCreateDtoToEntity() {
    TaskCreateDto dto = new TaskCreateDto();
    dto.setTitle("Test Task");
    dto.setDescription("Test Description");
    dto.setDueDate(LocalDate.of(2026, 12, 31));
    dto.setPriority(Priority.HIGH);
    dto.setTags(Set.of("urgent", "important"));

    Task task = taskMapper.toEntity(dto);

    assertThat(task).isNotNull();
    assertThat(task.getId()).isNull();
    assertThat(task.getTitle()).isEqualTo("Test Task");
    assertThat(task.getDescription()).isEqualTo("Test Description");
    assertThat(task.getDueDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    assertThat(task.getPriority()).isEqualTo(Priority.HIGH);
    assertThat(task.getTags()).containsExactlyInAnyOrder("urgent", "important");
    assertThat(task.getCompleted()).isFalse();
    assertThat(task.getCreatedAt()).isNull();
    assertThat(task.getUpdatedAt()).isNull();
  }

  @Test
  void shouldMapUpdateDtoToEntity() {
    Task existingTask = new Task();
    existingTask.setId(UUID.randomUUID());
    existingTask.setTitle("Old Title");
    existingTask.setDescription("Old Description");
    existingTask.setCompleted(false);
    existingTask.setPriority(Priority.LOW);

    TaskUpdateDto updateDto = new TaskUpdateDto();
    updateDto.setTitle("New Title");
    updateDto.setDescription("New Description");
    updateDto.setCompleted(true);
    updateDto.setPriority(Priority.HIGH);
    updateDto.setDueDate(LocalDate.of(2026, 12, 31));
    updateDto.setTags(Set.of("updated"));

    taskMapper.updateEntity(updateDto, existingTask);

    assertThat(existingTask.getTitle()).isEqualTo("New Title");
    assertThat(existingTask.getDescription()).isEqualTo("New Description");
    assertThat(existingTask.getCompleted()).isTrue();
    assertThat(existingTask.getPriority()).isEqualTo(Priority.HIGH);
    assertThat(existingTask.getDueDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    assertThat(existingTask.getTags()).containsExactly("updated");
    assertThat(existingTask.getId()).isNotNull();
  }

  @Test
  void shouldIgnoreNullFieldsWhenUpdating() {
    Task existingTask = new Task();
    existingTask.setId(UUID.randomUUID());
    existingTask.setTitle("Original Title");
    existingTask.setDescription("Original Description");
    existingTask.setCompleted(false);
    existingTask.setPriority(Priority.LOW);

    TaskUpdateDto updateDto = new TaskUpdateDto();
    updateDto.setTitle("New Title");

    taskMapper.updateEntity(updateDto, existingTask);

    assertThat(existingTask.getTitle()).isEqualTo("New Title");
    assertThat(existingTask.getDescription()).isEqualTo("Original Description");
    assertThat(existingTask.getCompleted()).isFalse();
    assertThat(existingTask.getPriority()).isEqualTo(Priority.LOW);
  }

  @Test
  void shouldMapEntityToResponseDto() {
    UUID id = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    Task task = new Task();
    task.setId(id);
    task.setTitle("Test Task");
    task.setDescription("Test Description");
    task.setCompleted(true);
    task.setCreatedAt(now);
    task.setDueDate(LocalDate.of(2026, 12, 31));
    task.setPriority(Priority.MEDIUM);
    task.setTags(Set.of("tag1", "tag2"));

    TaskResponseDto dto = taskMapper.toResponseDto(task);

    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(id);
    assertThat(dto.getTitle()).isEqualTo("Test Task");
    assertThat(dto.getDescription()).isEqualTo("Test Description");
    assertThat(dto.isCompleted()).isTrue();
    assertThat(dto.getCreatedAt()).isEqualTo(now);
    assertThat(dto.getDueDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    assertThat(dto.getPriority()).isEqualTo(Priority.MEDIUM);
    assertThat(dto.getTags()).containsExactlyInAnyOrder("tag1", "tag2");
  }

  @Test
  void shouldMapCreateDtoWithNullTags() {
    TaskCreateDto dto = new TaskCreateDto();
    dto.setTitle("Test Task");
    dto.setPriority(Priority.LOW);

    Task task = taskMapper.toEntity(dto);

    assertThat(task).isNotNull();
    if (task.getTags() != null) {
      assertThat(task.getTags()).isEmpty();  // или assertThat(task.getTags()).isNull();
    }
  }
}

