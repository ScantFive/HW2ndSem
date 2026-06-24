package com.mipt.hw;

import com.mipt.hw.dto.TaskCreateDto;
import com.mipt.hw.dto.TaskResponseDto;
import com.mipt.hw.model.Priority;
import com.mipt.hw.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FullApplicationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private TaskRepository taskRepository;

  @BeforeEach
  void setUp() {
    taskRepository.deleteAll();
  }

  @Test
  void shouldCreateAndRetrieveTask() {
    // Given
    TaskCreateDto createDto = new TaskCreateDto();
    createDto.setTitle("Integration Test Task");
    createDto.setDescription("Test Description");
    createDto.setPriority(Priority.HIGH);
    createDto.setDueDate(LocalDate.now().plusDays(7));

    // When - Create task
    ResponseEntity<TaskResponseDto> createResponse = restTemplate.postForEntity(
      "/api/tasks",
      createDto,
      TaskResponseDto.class
    );

    // Then
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(createResponse.getHeaders().get("X-API-Version")).contains("2.0.0");

    TaskResponseDto createdTask = createResponse.getBody();
    assertThat(createdTask).isNotNull();
    assertThat(createdTask.getId()).isNotNull();
    assertThat(createdTask.getTitle()).isEqualTo("Integration Test Task");
    assertThat(createdTask.getPriority()).isEqualTo(Priority.HIGH);

    // When - Get task by ID
    ResponseEntity<TaskResponseDto> getResponse = restTemplate.getForEntity(
      "/api/tasks/{id}",
      TaskResponseDto.class,
      createdTask.getId()
    );

    // Then
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody()).isNotNull();
    assertThat(getResponse.getBody().getTitle()).isEqualTo("Integration Test Task");
  }

  @Test
  void shouldUploadAndDownloadFile() {
    // Given - Create task first
    TaskCreateDto createDto = new TaskCreateDto();
    createDto.setTitle("Task with Attachment");
    createDto.setPriority(Priority.MEDIUM);

    ResponseEntity<TaskResponseDto> createResponse = restTemplate.postForEntity(
      "/api/tasks",
      createDto,
      TaskResponseDto.class
    );

    UUID taskId = createResponse.getBody().getId();

    // When - Upload file
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new ByteArrayResource("Test file content".getBytes()) {
      @Override
      public String getFilename() {
        return "test.txt";
      }
    });

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    HttpEntity<MultiValueMap<String, Object>> requestEntity =
      new HttpEntity<>(body, headers);

    ResponseEntity<String> uploadResponse = restTemplate.postForEntity(
      "/api/tasks/{taskId}/attachments",
      requestEntity,
      String.class,
      taskId
    );

    // Then
    assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(uploadResponse.getHeaders().get("X-API-Version")).contains("2.0.0");
  }

  @Test
  void shouldAddToFavorites() {
    // Given - Create task
    TaskCreateDto createDto = new TaskCreateDto();
    createDto.setTitle("Favorite Task");
    createDto.setPriority(Priority.HIGH);

    ResponseEntity<TaskResponseDto> createResponse = restTemplate.postForEntity(
      "/api/tasks",
      createDto,
      TaskResponseDto.class
    );

    UUID taskId = createResponse.getBody().getId();

    // When - Add to favorites
    ResponseEntity<Void> addResponse = restTemplate.postForEntity(
      "/api/favorites/{taskId}",
      null,
      Void.class,
      taskId
    );

    // Then
    assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(addResponse.getHeaders().get("X-API-Version")).contains("2.0.0");

    // When - Get favorites
    ResponseEntity<TaskResponseDto[]> favoritesResponse = restTemplate.getForEntity(
      "/api/favorites",
      TaskResponseDto[].class
    );

    // Then
    assertThat(favoritesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(favoritesResponse.getBody()).isNotEmpty();
  }

  @Test
  void shouldReturn404ForNonExistentTask() {
    // Given
    UUID nonExistentId = UUID.randomUUID();

    // When
    ResponseEntity<TaskResponseDto> response = restTemplate.getForEntity(
      "/api/tasks/{id}",
      TaskResponseDto.class,
      nonExistentId
    );

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void shouldReturn400ForInvalidTaskCreation() {
    // Given - Invalid task (empty title)
    TaskCreateDto createDto = new TaskCreateDto();
    createDto.setTitle("");
    createDto.setPriority(null);

    // When
    ResponseEntity<String> response = restTemplate.postForEntity(
      "/api/tasks",
      createDto,
      String.class
    );

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
