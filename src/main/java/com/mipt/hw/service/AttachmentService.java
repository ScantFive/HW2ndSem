package com.mipt.hw.service;

import com.mipt.hw.exception.AttachmentNotFoundException;
import com.mipt.hw.model.TaskAttachment;
import com.mipt.hw.repository.TaskAttachmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {
  private final TaskAttachmentRepository attachmentRepository;
  private final TaskService taskService;
  private final Path uploadDir;

  public AttachmentService(TaskAttachmentRepository attachmentRepository,
                           TaskService taskService,
                           @Value("${app.upload.dir:uploads}") String uploadDirPath) {
    this.attachmentRepository = attachmentRepository;
    this.taskService = taskService;
    this.uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
    initUploadDir();
  }

  private void initUploadDir() {
    try {
      if (!Files.exists(uploadDir)) {
        Files.createDirectories(uploadDir);
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not create upload directory", e);
    }
  }

  public TaskAttachment storeAttachment(UUID taskId, MultipartFile file) throws IOException {
    taskService.getTask(taskId);

    String originalFileName = file.getOriginalFilename();
    String storedFileName = UUID.randomUUID().toString();

    if (originalFileName != null && originalFileName.contains(".")) {
      String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
      storedFileName += extension;
    }

    Path targetLocation = uploadDir.resolve(storedFileName);
    try (InputStream inputStream = file.getInputStream()) {
      Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
    }

    TaskAttachment attachment = new TaskAttachment();
    attachment.setTaskId(taskId);
    attachment.setFileName(originalFileName != null ? originalFileName : "unknown");
    attachment.setStoredFileName(storedFileName);
    attachment.setContentType(file.getContentType());
    attachment.setSize(file.getSize());
    attachment.setUploadedAt(LocalDateTime.now());

    return attachmentRepository.save(attachment);
  }

  public TaskAttachment getAttachment(Long attachmentId) {
    return attachmentRepository.findById(attachmentId)
      .orElseThrow(() -> new AttachmentNotFoundException("Attachment not found with id: " + attachmentId));
  }

  public Resource loadAsResource(Long attachmentId) {
    TaskAttachment attachment = getAttachment(attachmentId);
    try {
      Path filePath = uploadDir.resolve(attachment.getStoredFileName()).normalize();
      Resource resource = new UrlResource(filePath.toUri());

      if (resource.exists() && resource.isReadable()) {
        return resource;
      } else {
        throw new RuntimeException("File not found or not readable: " + attachment.getFileName());
      }
    } catch (MalformedURLException e) {
      throw new RuntimeException("Error loading file: " + attachment.getFileName(), e);
    }
  }

  public void deleteAttachment(Long attachmentId) {
    TaskAttachment attachment = getAttachment(attachmentId);

    try {
      Path filePath = uploadDir.resolve(attachment.getStoredFileName()).normalize();
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      throw new RuntimeException("Could not delete file: " + attachment.getFileName(), e);
    }

    attachmentRepository.deleteById(attachmentId);
  }

  public List<TaskAttachment> getAttachmentsByTaskId(UUID taskId) {
    taskService.getTask(taskId);
    return attachmentRepository.findByTaskId(taskId);
  }
}
