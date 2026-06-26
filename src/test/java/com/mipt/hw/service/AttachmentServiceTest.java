package com.mipt.hw.service;

import com.mipt.hw.model.Priority;
import com.mipt.hw.model.Task;
import com.mipt.hw.model.TaskAttachment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AttachmentServiceTest {

  @Autowired
  private AttachmentService attachmentService;

  @Autowired
  private TaskService taskService;

  @Test
  void shouldStoreAttachment() throws Exception {
    Task task = new Task();
    task.setTitle("Task with attachment");
    task.setPriority(Priority.MEDIUM);
    Task created = taskService.createTask(task);

    MockMultipartFile file = new MockMultipartFile(
      "file",
      "test.txt",
      "text/plain",
      "Hello World".getBytes()
    );

    TaskAttachment attachment = attachmentService.storeAttachment(created.getId(), file);

    assertThat(attachment).isNotNull();
    assertThat(attachment.getId()).isNotNull();
    assertThat(attachment.getFileName()).isEqualTo("test.txt");
    assertThat(attachment.getSize()).isEqualTo(11);
  }
}
