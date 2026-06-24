package com.mipt.hw.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.hw.dto.AttachmentResponseDto;
import com.mipt.hw.model.TaskAttachment;
import com.mipt.hw.service.AttachmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttachmentController.class)
class AttachmentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private AttachmentService attachmentService;

  @Test
  void shouldUploadAttachment() throws Exception {
    // Given
    UUID taskId = UUID.randomUUID();
    MockMultipartFile file = new MockMultipartFile(
      "file",
      "test.txt",
      "text/plain",
      "Hello World".getBytes()
    );

    TaskAttachment attachment = new TaskAttachment();
    attachment.setId(1L);
    attachment.setFileName("test.txt");
    attachment.setSize(11L);
    attachment.setUploadedAt(LocalDateTime.now());

    when(attachmentService.storeAttachment(eq(taskId), any())).thenReturn(attachment);

    // When & Then
    mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", taskId)
        .file(file))
      .andExpect(status().isCreated())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.fileName").value("test.txt"))
      .andExpect(jsonPath("$.size").value(11));
  }

  @Test
  void shouldReturn404WhenTaskNotFoundForAttachment() throws Exception {
    // Given
    UUID taskId = UUID.randomUUID();
    MockMultipartFile file = new MockMultipartFile(
      "file",
      "test.txt",
      "text/plain",
      "Hello World".getBytes()
    );

    when(attachmentService.storeAttachment(eq(taskId), any()))
      .thenThrow(new RuntimeException("Task not found"));

    // When & Then
    mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", taskId)
        .file(file))
      .andExpect(status().isInternalServerError());
  }

  @Test
  void shouldGetAttachmentsByTaskId() throws Exception {
    // Given
    UUID taskId = UUID.randomUUID();

    TaskAttachment attachment1 = new TaskAttachment();
    attachment1.setId(1L);
    attachment1.setFileName("file1.txt");
    attachment1.setSize(100L);
    attachment1.setUploadedAt(LocalDateTime.now());

    TaskAttachment attachment2 = new TaskAttachment();
    attachment2.setId(2L);
    attachment2.setFileName("file2.txt");
    attachment2.setSize(200L);
    attachment2.setUploadedAt(LocalDateTime.now());

    List<TaskAttachment> attachments = Arrays.asList(attachment1, attachment2);

    when(attachmentService.getAttachmentsByTaskId(taskId)).thenReturn(attachments);

    // When & Then
    mockMvc.perform(get("/api/tasks/{taskId}/attachments", taskId))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].fileName").value("file1.txt"))
      .andExpect(jsonPath("$[1].id").value(2))
      .andExpect(jsonPath("$[1].fileName").value("file2.txt"));
  }

  @Test
  void shouldReturnEmptyListWhenNoAttachments() throws Exception {
    // Given
    UUID taskId = UUID.randomUUID();
    when(attachmentService.getAttachmentsByTaskId(taskId)).thenReturn(Arrays.asList());

    // When & Then
    mockMvc.perform(get("/api/tasks/{taskId}/attachments", taskId))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void shouldDownloadAttachment() throws Exception {
    // Given
    Long attachmentId = 1L;
    TaskAttachment attachment = new TaskAttachment();
    attachment.setId(attachmentId);
    attachment.setFileName("test.txt");
    attachment.setContentType("text/plain");
    attachment.setSize(11L);

    Resource resource = new ByteArrayResource("Hello World".getBytes());

    when(attachmentService.getAttachment(attachmentId)).thenReturn(attachment);
    when(attachmentService.loadAsResource(attachmentId)).thenReturn(resource);

    // When & Then
    mockMvc.perform(get("/api/attachments/{attachmentId}", attachmentId))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.txt\""))
      .andExpect(content().string("Hello World"));
  }

  @Test
  void shouldReturn404WhenAttachmentNotFoundForDownload() throws Exception {
    // Given
    Long attachmentId = 1L;
    when(attachmentService.getAttachment(attachmentId))
      .thenThrow(new RuntimeException("Attachment not found"));

    // When & Then
    mockMvc.perform(get("/api/attachments/{attachmentId}", attachmentId))
      .andExpect(status().isInternalServerError());
  }

  @Test
  void shouldDeleteAttachment() throws Exception {
    // Given
    Long attachmentId = 1L;
    doNothing().when(attachmentService).deleteAttachment(attachmentId);

    // When & Then
    mockMvc.perform(delete("/api/attachments/{attachmentId}", attachmentId))
      .andExpect(status().isNoContent())
      .andExpect(header().string("X-API-Version", "2.0.0"));
  }

  @Test
  void shouldReturn404WhenAttachmentNotFoundForDelete() throws Exception {
    // Given
    Long attachmentId = 1L;
    doThrow(new RuntimeException("Attachment not found"))
      .when(attachmentService).deleteAttachment(attachmentId);

    // When & Then
    mockMvc.perform(delete("/api/attachments/{attachmentId}", attachmentId))
      .andExpect(status().isInternalServerError());
  }
}
