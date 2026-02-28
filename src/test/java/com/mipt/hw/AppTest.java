package com.mipt.hw;

import com.mipt.hw.model.Task;
import com.mipt.hw.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AppTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @MockBean
  private TaskService taskService;

  private String baseUrl = "/api/tasks";
  private UUID testId;
  private Task testTask;

  @BeforeEach
  void setUp() {
    testId = UUID.randomUUID();
    testTask = new Task(
      testId,
      "Тестовая задача",
      "Тестовое описание",
      false
    );
  }

  // GET /api/tasks (получить все задачи)

  @Test
  void getAllTasksShouldReturnList() {
    Task task1 = new Task(UUID.randomUUID(), "Задача 1", "Описание 1", false);
    Task task2 = new Task(UUID.randomUUID(), "Задача 2", "Описание 2", true);
    when(taskService.getAllTasks()).thenReturn(List.of(task1, task2));

    ResponseEntity<Task[]> response = restTemplate.getForEntity(baseUrl, Task[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().length).isEqualTo(2);
    verify(taskService, times(1)).getAllTasks();
  }

  @Test
  void getAllTasksShouldReturnEmptyList() {
    when(taskService.getAllTasks()).thenReturn(List.of());

    ResponseEntity<Task[]> response = restTemplate.getForEntity(baseUrl, Task[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().length).isEqualTo(0);
    verify(taskService, times(1)).getAllTasks();
  }

  // GET /api/tasks/{id} (получить одну задачу)

  @Test
  void getTaskByIdShouldReturnTask() {
    when(taskService.getTask(testId)).thenReturn(testTask);

    ResponseEntity<Task> response = restTemplate.getForEntity(baseUrl + "/{id}", Task.class, testId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getId()).isEqualTo(testId);
    assertThat(response.getBody().getTitle()).isEqualTo("Тестовая задача");
    verify(taskService, times(1)).getTask(testId);
  }

  @Test
  void getTaskByIdShouldReturnNotFound_WhenTaskNotFound() {
    UUID invalidId = UUID.randomUUID();
    when(taskService.getTask(invalidId)).thenThrow(new RuntimeException("Task not found"));

    ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/{id}", String.class, invalidId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    verify(taskService, times(1)).getTask(invalidId);
  }

  // POST /api/tasks (создать задачу)

  @Test
  void createTaskShouldCreateNewTask() {
    Task newTask = new Task(null, "Новая задача", "Новое описание", false);
    Task createdTask = new Task(UUID.randomUUID(), "Новая задача", "Новое описание", false);

    when(taskService.createTask(any(Task.class))).thenReturn(createdTask);

    ResponseEntity<Task> response = restTemplate.postForEntity(baseUrl, newTask, Task.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getId()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("Новая задача");
    verify(taskService, times(1)).createTask(any(Task.class));
  }

  @Test
  void createTaskShouldReturnBadRequest_WhenTitleIsEmpty() {
    Task invalidTask = new Task(null, "", "Описание", false);

    when(taskService.createTask(any(Task.class))).thenThrow(new IllegalArgumentException());

    ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, invalidTask, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    verify(taskService, times(1)).createTask(any(Task.class));
  }

  // PUT /api/tasks/{id} (обновить задачу)

  @Test
  void updateTaskShouldUpdateTask() {
    Task updatedTask = new Task(testId, "Обновлено", "Новое описание", true);
    when(taskService.updateTask(eq(testId), any(Task.class))).thenReturn(updatedTask);

    HttpEntity<Task> requestEntity = new HttpEntity<>(updatedTask);
    ResponseEntity<Task> response = restTemplate.exchange(
      baseUrl + "/{id}",
      HttpMethod.PUT,
      requestEntity,
      Task.class,
      testId
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTitle()).isEqualTo("Обновлено");
    assertThat(response.getBody().getCompleted()).isTrue();
    verify(taskService, times(1)).updateTask(eq(testId), any(Task.class));
  }

  @Test
  void updateTaskShouldReturnNotFound_WhenTaskNotFound() {
    UUID invalidId = UUID.randomUUID();
    Task updatedTask = new Task(invalidId, "Обновлено", "Описание", true);

    when(taskService.updateTask(eq(invalidId), any(Task.class)))
      .thenThrow(new RuntimeException("Task not found"));

    HttpEntity<Task> requestEntity = new HttpEntity<>(updatedTask);
    ResponseEntity<String> response = restTemplate.exchange(
      baseUrl + "/{id}",
      HttpMethod.PUT,
      requestEntity,
      String.class,
      invalidId
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    verify(taskService, times(1)).updateTask(eq(invalidId), any(Task.class));
  }

  // DELETE /api/tasks/{id} (удалить задачу)

  @Test
  void deleteTaskShouldDeleteTask() {
    doNothing().when(taskService).deleteTask(testId);

    ResponseEntity<Void> response = restTemplate.exchange(
      baseUrl + "/{id}",
      HttpMethod.DELETE,
      null,
      Void.class,
      testId
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(taskService, times(1)).deleteTask(testId);
  }

  @Test
  void deleteTaskShouldReturnNotFound_WhenTaskNotFound() {
    UUID invalidId = UUID.randomUUID();
    doThrow(new RuntimeException("Task not found")).when(taskService).deleteTask(invalidId);

    ResponseEntity<String> response = restTemplate.exchange(
      baseUrl + "/{id}",
      HttpMethod.DELETE,
      null,
      String.class,
      invalidId
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    verify(taskService, times(1)).deleteTask(invalidId);
  }
}