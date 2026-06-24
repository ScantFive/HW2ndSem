package com.mipt.hw.service;

import com.mipt.hw.exception.AttachmentNotFoundException;
import com.mipt.hw.model.Task;
import com.mipt.hw.model.TaskAttachment;
import com.mipt.hw.repository.TaskAttachmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

  @Mock
  private TaskAttachmentRepository attachmentRepository;

  @Mock
  private TaskService taskService;

  @TempDir
  Path tempDir;

  private AttachmentService attachmentService;

  @BeforeEach
  void setUp() {
    attachmentService = new AttachmentService(
      attachmentRepository,
      taskService,
      tempDir.toString()
    );
  }

  @Test
  void shouldThrowExceptionWhenTaskNotFoundForAttachment() throws IOException {
    // Given
    UUID taskId = UUID.randomUUID();
    MultipartFile file = new MockMultipartFile(
      "file",
      "test.txt",
      "text/plain",
      "Test content".getBytes()
    );

    when(taskService.getTask(taskId)).thenThrow(new RuntimeException("Task not found"));

    // When & Then
    assertThatThrownBy(() -> attachmentService.storeAttachment(taskId, file))
      .isInstanceOf(RuntimeException.class)
      .hasMessage("Task not found");
  }

  @Test
  void shouldGetAttachment() {
    // Given
    Long attachmentId = 1L;
    TaskAttachment attachment = new TaskAttachment();
    attachment.setId(attachmentId);

    when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

    // When
    TaskAttachment result = attachmentService.getAttachment(attachmentId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(attachmentId);
  }

  @Test
  void shouldThrowExceptionWhenAttachmentNotFound() {
    // Given
    Long attachmentId = 1L;
    when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> attachmentService.getAttachment(attachmentId))
      .isInstanceOf(AttachmentNotFoundException.class)
      .hasMessage("Attachment not found with id: " + attachmentId);
  }

  @Test
  void shouldDeleteAttachment() throws IOException {
    // Given
    Long attachmentId = 1L;
    String storedFileName = "test-file.txt";

    Path testFile = tempDir.resolve(storedFileName);
    Files.write(testFile, "Test content".getBytes());

    TaskAttachment attachment = new TaskAttachment();
    attachment.setId(attachmentId);
    attachment.setStoredFileName(storedFileName);

    when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
    doNothing().when(attachmentRepository).deleteById(attachmentId);

    // When
    attachmentService.deleteAttachment(attachmentId);

    // Then
    assertThat(testFile).doesNotExist();
    verify(attachmentRepository).deleteById(attachmentId);
  }

  @Test
  void shouldGetAttachmentsByTaskId() {
    // Given
    UUID taskId = UUID.randomUUID();
    Task task = new Task();
    task.setId(taskId);

    TaskAttachment attachment1 = new TaskAttachment();
    attachment1.setId(1L);
    TaskAttachment attachment2 = new TaskAttachment();
    attachment2.setId(2L);

    List<TaskAttachment> attachments = Arrays.asList(attachment1, attachment2);

    when(taskService.getTask(taskId)).thenReturn(task);
    when(attachmentRepository.findByTaskId(taskId)).thenReturn(attachments);

    // When
    List<TaskAttachment> result = attachmentService.getAttachmentsByTaskId(taskId);

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).containsExactly(attachment1, attachment2);
    verify(attachmentRepository).findByTaskId(taskId);
  }

  @Test
  void shouldReturnEmptyListWhenNoAttachments() {
    // Given
    UUID taskId = UUID.randomUUID();
    Task task = new Task();
    task.setId(taskId);

    when(taskService.getTask(taskId)).thenReturn(task);
    when(attachmentRepository.findByTaskId(taskId)).thenReturn(Arrays.asList());

    // When
    List<TaskAttachment> result = attachmentService.getAttachmentsByTaskId(taskId);

    // Then
    assertThat(result).isEmpty();
  }
}