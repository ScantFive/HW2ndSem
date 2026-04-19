package com.mipt.hw.controller;

import com.mipt.hw.dto.AttachmentResponseDto;
import com.mipt.hw.model.TaskAttachment;
import com.mipt.hw.service.AttachmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class AttachmentController {
  private final AttachmentService attachmentService;

  @Value("${api.version:2.0.0}")
  private String apiVersion;

  public AttachmentController(AttachmentService attachmentService) {
    this.attachmentService = attachmentService;
  }

  @PostMapping("/api/tasks/{taskId}/attachments")
  public ResponseEntity<AttachmentResponseDto> uploadAttachment(
    @PathVariable UUID taskId,
    @RequestParam("file") MultipartFile file) {
    try {
      TaskAttachment attachment = attachmentService.storeAttachment(taskId, file);
      AttachmentResponseDto responseDto = toResponseDto(attachment);

      return ResponseEntity.status(HttpStatus.CREATED)
        .header("X-API-Version", apiVersion)
        .body(responseDto);
    } catch (IOException e) {
      throw new RuntimeException("Failed to store file", e);
    }
  }

  @GetMapping("/api/tasks/{taskId}/attachments")
  public ResponseEntity<List<AttachmentResponseDto>> getAttachmentsByTaskId(@PathVariable UUID taskId) {
    List<AttachmentResponseDto> attachments = attachmentService.getAttachmentsByTaskId(taskId).stream()
      .map(this::toResponseDto)
      .collect(Collectors.toList());

    return ResponseEntity.ok()
      .header("X-API-Version", apiVersion)
      .body(attachments);
  }

  @GetMapping("/api/attachments/{attachmentId}")
  public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
    TaskAttachment attachment = attachmentService.getAttachment(attachmentId);
    Resource resource = attachmentService.loadAsResource(attachmentId);

    String contentDisposition = "attachment; filename=\"" + attachment.getFileName() + "\"";

    return ResponseEntity.ok()
      .contentType(MediaType.parseMediaType(attachment.getContentType()))
      .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
      .header("X-API-Version", apiVersion)
      .body(resource);
  }

  @DeleteMapping("/api/attachments/{attachmentId}")
  public ResponseEntity<Void> deleteAttachment(@PathVariable Long attachmentId) {
    attachmentService.deleteAttachment(attachmentId);

    return ResponseEntity.noContent()
      .header("X-API-Version", apiVersion)
      .build();
  }

  private AttachmentResponseDto toResponseDto(TaskAttachment attachment) {
    return new AttachmentResponseDto(
      attachment.getId(),
      attachment.getFileName(),
      attachment.getSize(),
      attachment.getUploadedAt()
    );
  }
}
