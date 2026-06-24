package com.mipt.hw.controller;

import com.mipt.hw.dto.AttachmentResponseDto;
import com.mipt.hw.model.TaskAttachment;
import com.mipt.hw.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Attachment Controller", description = "Управление вложениями задач")
public class AttachmentController {

  private final AttachmentService attachmentService;

  @Value("${api.version:2.0.0}")
  private String apiVersion;

  public AttachmentController(AttachmentService attachmentService) {
    this.attachmentService = attachmentService;
  }

  @Operation(summary = "Загрузить вложение", description = "Загружает файл и прикрепляет его к задаче")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Файл успешно загружен",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = AttachmentResponseDto.class))),
    @ApiResponse(responseCode = "400", description = "Некорректный запрос"),
    @ApiResponse(responseCode = "404", description = "Задача не найдена")
  })
  @PostMapping("/api/tasks/{taskId}/attachments")
  public ResponseEntity<AttachmentResponseDto> uploadAttachment(
    @Parameter(description = "ID задачи", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
    @PathVariable UUID taskId,
    @Parameter(description = "Файл для загрузки", required = true)
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

  @Operation(summary = "Получить все вложения задачи", description = "Возвращает список всех вложений задачи")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Успешное получение списка вложений",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = AttachmentResponseDto.class)))
  })
  @GetMapping("/api/tasks/{taskId}/attachments")
  public ResponseEntity<List<AttachmentResponseDto>> getAttachmentsByTaskId(
    @Parameter(description = "ID задачи", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
    @PathVariable UUID taskId) {
    List<AttachmentResponseDto> attachments = attachmentService.getAttachmentsByTaskId(taskId).stream()
      .map(this::toResponseDto)
      .collect(Collectors.toList());

    return ResponseEntity.ok()
      .header("X-API-Version", apiVersion)
      .body(attachments);
  }

  @Operation(summary = "Скачать вложение", description = "Скачивает файл по ID вложения")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Файл успешно скачан",
      content = @Content(mediaType = "application/octet-stream")),
    @ApiResponse(responseCode = "404", description = "Вложение не найдено")
  })
  @GetMapping("/api/attachments/{attachmentId}")
  public ResponseEntity<Resource> downloadAttachment(
    @Parameter(description = "ID вложения", required = true, example = "1")
    @PathVariable Long attachmentId) {
    TaskAttachment attachment = attachmentService.getAttachment(attachmentId);
    Resource resource = attachmentService.loadAsResource(attachmentId);

    String contentDisposition = "attachment; filename=\"" + attachment.getFileName() + "\"";

    return ResponseEntity.ok()
      .contentType(MediaType.parseMediaType(attachment.getContentType()))
      .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
      .header("X-API-Version", apiVersion)
      .body(resource);
  }

  @Operation(summary = "Удалить вложение", description = "Удаляет вложение и файл с диска")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Вложение успешно удалено"),
    @ApiResponse(responseCode = "404", description = "Вложение не найдено")
  })
  @DeleteMapping("/api/attachments/{attachmentId}")
  public ResponseEntity<Void> deleteAttachment(
    @Parameter(description = "ID вложения", required = true, example = "1")
    @PathVariable Long attachmentId) {
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
