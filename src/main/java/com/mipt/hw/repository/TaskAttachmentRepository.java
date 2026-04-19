package com.mipt.hw.repository;

import com.mipt.hw.model.TaskAttachment;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class TaskAttachmentRepository {
  private final Map<Long, TaskAttachment> attachments = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  public TaskAttachment save(TaskAttachment attachment) {
    if (attachment.getId() == null) {
      attachment.setId(idGenerator.getAndIncrement());
    }
    attachments.put(attachment.getId(), attachment);
    return attachment;
  }

  public Optional<TaskAttachment> findById(Long id) {
    return Optional.ofNullable(attachments.get(id));
  }

  public List<TaskAttachment> findByTaskId(UUID taskId) {
    return attachments.values().stream()
      .filter(attachment -> attachment.getTaskId().equals(taskId))
      .collect(Collectors.toList());
  }

  public void deleteById(Long id) {
    attachments.remove(id);
  }

  public List<TaskAttachment> findAll() {
    return new ArrayList<>(attachments.values());
  }
}
