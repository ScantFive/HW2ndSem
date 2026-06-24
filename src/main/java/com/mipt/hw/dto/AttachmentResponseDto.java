package com.mipt.hw.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "DTO для ответа с информацией о вложении")
public class AttachmentResponseDto {
  @Schema(description = "Уникальный идентификатор вложения", example = "1")
  private Long id;

  @Schema(description = "Оригинальное имя файла", example = "document.pdf")
  private String fileName;

  @Schema(description = "Размер файла в байтах", example = "1024")
  private long size;

  @Schema(description = "Дата и время загрузки", example = "2026-01-15T10:30:00")
  private LocalDateTime uploadedAt;

  public AttachmentResponseDto(Long id, String fileName, long size, LocalDateTime uploadedAt) {
    this.id = id;
    this.fileName = fileName;
    this.size = size;
    this.uploadedAt = uploadedAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public LocalDateTime getUploadedAt() {
    return uploadedAt;
  }

  public void setUploadedAt(LocalDateTime uploadedAt) {
    this.uploadedAt = uploadedAt;
  }
}
