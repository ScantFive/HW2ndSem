package com.mipt.hw.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.hw.dto.TaskCreateDto;
import com.mipt.hw.dto.TaskResponseDto;
import com.mipt.hw.dto.TaskUpdateDto;
import com.mipt.hw.mapper.TaskMapper;
import com.mipt.hw.model.Priority;
import com.mipt.hw.model.Task;
import com.mipt.hw.service.FavoritesService;
import com.mipt.hw.service.TaskService;
import com.mipt.hw.valid.TaskUpdateContext;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private TaskService taskService;

  @MockBean
  private TaskMapper taskMapper;

  @MockBean
  private FavoritesService favoritesService;

  @MockBean
  private Validator validator;

  // ==================== GET /api/tasks ====================

  @Test
  void shouldGetAllTasks() throws Exception {
    // Given
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    Task task1 = new Task();
    task1.setId(id1);
    task1.setTitle("Task 1");
    task1.setDescription("Description 1");
    task1.setCompleted(false);
    task1.setPriority(Priority.HIGH);
    task1.setCreatedAt(LocalDateTime.now());

    Task task2 = new Task();
    task2.setId(id2);
    task2.setTitle("Task 2");
    task2.setDescription("Description 2");
    task2.setCompleted(true);
    task2.setPriority(Priority.LOW);
    task2.setCreatedAt(LocalDateTime.now());

    TaskResponseDto dto1 = new TaskResponseDto();
    dto1.setId(id1);
    dto1.setTitle("Task 1");
    dto1.setDescription("Description 1");
    dto1.setCompleted(false);
    dto1.setPriority(Priority.HIGH);

    TaskResponseDto dto2 = new TaskResponseDto();
    dto2.setId(id2);
    dto2.setTitle("Task 2");
    dto2.setDescription("Description 2");
    dto2.setCompleted(true);
    dto2.setPriority(Priority.LOW);

    when(taskService.getAllTasks()).thenReturn(Arrays.asList(task1, task2));
    when(taskService.getTaskCount()).thenReturn(2L);
    when(taskMapper.toResponseDto(task1)).thenReturn(dto1);
    when(taskMapper.toResponseDto(task2)).thenReturn(dto2);

    // When & Then
    mockMvc.perform(get("/api/tasks"))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(header().string("X-Total-Count", "2"))
      .andExpect(jsonPath("$", hasSize(2)))
      .andExpect(jsonPath("$[0].id").value(id1.toString()))
      .andExpect(jsonPath("$[0].title").value("Task 1"))
      .andExpect(jsonPath("$[0].completed").value(false))
      .andExpect(jsonPath("$[0].priority").value("HIGH"))
      .andExpect(jsonPath("$[1].id").value(id2.toString()))
      .andExpect(jsonPath("$[1].title").value("Task 2"))
      .andExpect(jsonPath("$[1].completed").value(true))
      .andExpect(jsonPath("$[1].priority").value("LOW"));

    verify(taskService).getAllTasks();
    verify(taskService).getTaskCount();
    verify(taskMapper, times(2)).toResponseDto(any(Task.class));
  }

  @Test
  void shouldReturnEmptyListWhenNoTasks() throws Exception {
    // Given
    when(taskService.getAllTasks()).thenReturn(Arrays.asList());
    when(taskService.getTaskCount()).thenReturn(0L);

    // When & Then
    mockMvc.perform(get("/api/tasks"))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(header().string("X-Total-Count", "0"))
      .andExpect(jsonPath("$", hasSize(0)));

    verify(taskService).getAllTasks();
    verify(taskService).getTaskCount();
  }

  // ==================== GET /api/tasks/{id} ====================

  @Test
  void shouldGetTaskById() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    Task task = new Task();
    task.setId(id);
    task.setTitle("Test Task");
    task.setDescription("Test Description");
    task.setCompleted(false);
    task.setPriority(Priority.MEDIUM);
    task.setCreatedAt(LocalDateTime.now());
    task.setDueDate(LocalDate.now().plusDays(7));

    TaskResponseDto dto = new TaskResponseDto();
    dto.setId(id);
    dto.setTitle("Test Task");
    dto.setDescription("Test Description");
    dto.setCompleted(false);
    dto.setPriority(Priority.MEDIUM);
    dto.setDueDate(LocalDate.now().plusDays(7));

    when(taskService.getTask(id)).thenReturn(task);
    when(taskMapper.toResponseDto(task)).thenReturn(dto);

    // When & Then
    mockMvc.perform(get("/api/tasks/{id}", id))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.id").value(id.toString()))
      .andExpect(jsonPath("$.title").value("Test Task"))
      .andExpect(jsonPath("$.description").value("Test Description"))
      .andExpect(jsonPath("$.completed").value(false))
      .andExpect(jsonPath("$.priority").value("MEDIUM"));

    verify(taskService).getTask(id);
    verify(taskMapper).toResponseDto(task);
  }

  @Test
  void shouldReturn404WhenTaskNotFound() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    when(taskService.getTask(id)).thenThrow(new RuntimeException("Task not found with id: " + id));

    // When & Then
    mockMvc.perform(get("/api/tasks/{id}", id))
      .andExpect(status().isInternalServerError());

    verify(taskService).getTask(id);
  }

  // ==================== GET /api/tasks/{id}/favorite-status ====================

  @Test
  void shouldCheckFavoriteStatus() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    when(favoritesService.isFavorite(eq(id), any())).thenReturn(true);

    // When & Then
    mockMvc.perform(get("/api/tasks/{id}/favorite-status", id))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.isFavorite").value(true));

    verify(favoritesService).isFavorite(eq(id), any());
  }

  @Test
  void shouldCheckFavoriteStatusWhenNotFavorite() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    when(favoritesService.isFavorite(eq(id), any())).thenReturn(false);

    // When & Then
    mockMvc.perform(get("/api/tasks/{id}/favorite-status", id))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.isFavorite").value(false));

    verify(favoritesService).isFavorite(eq(id), any());
  }

  // ==================== POST /api/tasks ====================

  @Test
  void shouldCreateTask() throws Exception {
    // Given
    TaskCreateDto createDto = new TaskCreateDto();
    createDto.setTitle("New Task");
    createDto.setDescription("New Description");
    createDto.setPriority(Priority.HIGH);
    createDto.setDueDate(LocalDate.now().plusDays(7));

    UUID id = UUID.randomUUID();
    Task task = new Task();
    task.setId(id);
    task.setTitle("New Task");
    task.setDescription("New Description");
    task.setPriority(Priority.HIGH);
    task.setCompleted(false);
    task.setCreatedAt(LocalDateTime.now());

    TaskResponseDto responseDto = new TaskResponseDto();
    responseDto.setId(id);
    responseDto.setTitle("New Task");
    responseDto.setDescription("New Description");
    responseDto.setPriority(Priority.HIGH);
    responseDto.setCompleted(false);

    when(taskMapper.toEntity(any(TaskCreateDto.class))).thenReturn(task);
    when(taskService.createTask(any(Task.class))).thenReturn(task);
    when(taskMapper.toResponseDto(task)).thenReturn(responseDto);

    // When & Then
    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createDto)))
      .andExpect(status().isCreated())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.id").value(id.toString()))
      .andExpect(jsonPath("$.title").value("New Task"))
      .andExpect(jsonPath("$.description").value("New Description"))
      .andExpect(jsonPath("$.priority").value("HIGH"))
      .andExpect(jsonPath("$.completed").value(false));

    verify(taskMapper).toEntity(any(TaskCreateDto.class));
    verify(taskService).createTask(any(Task.class));
    verify(taskMapper).toResponseDto(task);
  }



  // ==================== PUT /api/tasks/{id} ====================

  @Test
  void shouldUpdateTask() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    TaskUpdateDto updateDto = new TaskUpdateDto();
    updateDto.setTitle("Updated Title");
    updateDto.setDescription("Updated Description");
    updateDto.setCompleted(true);
    updateDto.setPriority(Priority.HIGH);

    Task existingTask = new Task();
    existingTask.setId(id);
    existingTask.setTitle("Old Title");
    existingTask.setDescription("Old Description");
    existingTask.setCompleted(false);
    existingTask.setPriority(Priority.LOW);

    Task updatedTask = new Task();
    updatedTask.setId(id);
    updatedTask.setTitle("Updated Title");
    updatedTask.setDescription("Updated Description");
    updatedTask.setCompleted(true);
    updatedTask.setPriority(Priority.HIGH);

    TaskResponseDto responseDto = new TaskResponseDto();
    responseDto.setId(id);
    responseDto.setTitle("Updated Title");
    responseDto.setDescription("Updated Description");
    responseDto.setCompleted(true);
    responseDto.setPriority(Priority.HIGH);

    when(taskService.updateTask(eq(id), any(TaskUpdateDto.class))).thenReturn(updatedTask);
    when(taskMapper.toResponseDto(updatedTask)).thenReturn(responseDto);

    // When & Then
    mockMvc.perform(put("/api/tasks/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateDto)))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.id").value(id.toString()))
      .andExpect(jsonPath("$.title").value("Updated Title"))
      .andExpect(jsonPath("$.description").value("Updated Description"))
      .andExpect(jsonPath("$.completed").value(true))
      .andExpect(jsonPath("$.priority").value("HIGH"));

    verify(taskService).updateTask(eq(id), any(TaskUpdateDto.class));
    verify(taskMapper).toResponseDto(updatedTask);
  }

  @Test
  void shouldReturn404WhenUpdateNonExistentTask() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    TaskUpdateDto updateDto = new TaskUpdateDto();
    updateDto.setTitle("Updated Title");

    when(taskService.updateTask(eq(id), any(TaskUpdateDto.class)))
      .thenThrow(new RuntimeException("Task not found with id: " + id));

    // When & Then
    mockMvc.perform(put("/api/tasks/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateDto)))
      .andExpect(status().isInternalServerError());

    verify(taskService).updateTask(eq(id), any(TaskUpdateDto.class));
  }

  @Test
  void shouldUpdateTaskWithPartialData() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    TaskUpdateDto updateDto = new TaskUpdateDto();
    updateDto.setTitle("New Title Only");
    // Other fields are null - should only update title

    Task existingTask = new Task();
    existingTask.setId(id);
    existingTask.setTitle("Old Title");
    existingTask.setDescription("Existing Description");
    existingTask.setCompleted(false);
    existingTask.setPriority(Priority.LOW);

    Task updatedTask = new Task();
    updatedTask.setId(id);
    updatedTask.setTitle("New Title Only");
    updatedTask.setDescription("Existing Description");
    updatedTask.setCompleted(false);
    updatedTask.setPriority(Priority.LOW);

    TaskResponseDto responseDto = new TaskResponseDto();
    responseDto.setId(id);
    responseDto.setTitle("New Title Only");
    responseDto.setDescription("Existing Description");
    responseDto.setCompleted(false);
    responseDto.setPriority(Priority.LOW);

    when(taskService.updateTask(eq(id), any(TaskUpdateDto.class))).thenReturn(updatedTask);
    when(taskMapper.toResponseDto(updatedTask)).thenReturn(responseDto);

    // When & Then
    mockMvc.perform(put("/api/tasks/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateDto)))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$.id").value(id.toString()))
      .andExpect(jsonPath("$.title").value("New Title Only"))
      .andExpect(jsonPath("$.description").value("Existing Description"))
      .andExpect(jsonPath("$.completed").value(false))
      .andExpect(jsonPath("$.priority").value("LOW"));

    verify(taskService).updateTask(eq(id), any(TaskUpdateDto.class));
  }

  // ==================== DELETE /api/tasks/{id} ====================

  @Test
  void shouldDeleteTask() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    doNothing().when(taskService).deleteTask(id);

    // When & Then
    mockMvc.perform(delete("/api/tasks/{id}", id))
      .andExpect(status().isNoContent())
      .andExpect(header().string("X-API-Version", "2.0.0"));

    verify(taskService).deleteTask(id);
  }

  @Test
  void shouldReturn404WhenDeleteNonExistentTask() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    doThrow(new RuntimeException("Task not found with id: " + id))
      .when(taskService).deleteTask(id);

    // When & Then
    mockMvc.perform(delete("/api/tasks/{id}", id))
      .andExpect(status().isInternalServerError());

    verify(taskService).deleteTask(id);
  }

  // ==================== Интеграционные сценарии ====================

  @Test
  void shouldHandleCompleteTaskLifecycle() throws Exception {
    // 1. Create task
    UUID id = UUID.randomUUID();
    TaskCreateDto createDto = new TaskCreateDto();
    createDto.setTitle("Lifecycle Task");
    createDto.setPriority(Priority.MEDIUM);

    Task task = new Task();
    task.setId(id);
    task.setTitle("Lifecycle Task");
    task.setPriority(Priority.MEDIUM);
    task.setCompleted(false);

    TaskResponseDto responseDto = new TaskResponseDto();
    responseDto.setId(id);
    responseDto.setTitle("Lifecycle Task");
    responseDto.setPriority(Priority.MEDIUM);
    responseDto.setCompleted(false);

    when(taskMapper.toEntity(any(TaskCreateDto.class))).thenReturn(task);
    when(taskService.createTask(any(Task.class))).thenReturn(task);
    when(taskMapper.toResponseDto(task)).thenReturn(responseDto);

    // When - Create
    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createDto)))
      .andExpect(status().isCreated());

    // 2. Get task
    when(taskService.getTask(id)).thenReturn(task);
    when(taskMapper.toResponseDto(task)).thenReturn(responseDto);

    mockMvc.perform(get("/api/tasks/{id}", id))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(id.toString()))
      .andExpect(jsonPath("$.title").value("Lifecycle Task"));

    // 3. Update task
    TaskUpdateDto updateDto = new TaskUpdateDto();
    updateDto.setTitle("Updated Lifecycle Task");
    updateDto.setCompleted(true);

    Task updatedTask = new Task();
    updatedTask.setId(id);
    updatedTask.setTitle("Updated Lifecycle Task");
    updatedTask.setPriority(Priority.MEDIUM);
    updatedTask.setCompleted(true);

    TaskResponseDto updatedResponse = new TaskResponseDto();
    updatedResponse.setId(id);
    updatedResponse.setTitle("Updated Lifecycle Task");
    updatedResponse.setPriority(Priority.MEDIUM);
    updatedResponse.setCompleted(true);

    when(taskService.updateTask(eq(id), any(TaskUpdateDto.class))).thenReturn(updatedTask);
    when(taskMapper.toResponseDto(updatedTask)).thenReturn(updatedResponse);

    mockMvc.perform(put("/api/tasks/{id}", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateDto)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.title").value("Updated Lifecycle Task"))
      .andExpect(jsonPath("$.completed").value(true));

    // 4. Delete task
    doNothing().when(taskService).deleteTask(id);

    mockMvc.perform(delete("/api/tasks/{id}", id))
      .andExpect(status().isNoContent());

    verify(taskService).deleteTask(id);
  }

  @Test
  void shouldHandleHeadersCorrectly() throws Exception {
    // Given
    UUID id = UUID.randomUUID();
    Task task = new Task();
    task.setId(id);
    task.setTitle("Header Test");

    TaskResponseDto dto = new TaskResponseDto();
    dto.setId(id);
    dto.setTitle("Header Test");

    when(taskService.getTask(id)).thenReturn(task);
    when(taskMapper.toResponseDto(task)).thenReturn(dto);
    when(taskService.getAllTasks()).thenReturn(Arrays.asList(task));
    when(taskService.getTaskCount()).thenReturn(1L);
    when(taskMapper.toResponseDto(any(Task.class))).thenReturn(dto);

    // When & Then - Check headers on GET all
    mockMvc.perform(get("/api/tasks"))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(header().string("X-Total-Count", "1"));

    // Check headers on GET by ID
    mockMvc.perform(get("/api/tasks/{id}", id))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"));

    // Check headers on POST
    TaskCreateDto createDto = new TaskCreateDto();
    createDto.setTitle("New Task");
    createDto.setPriority(Priority.HIGH);

    when(taskMapper.toEntity(any(TaskCreateDto.class))).thenReturn(task);
    when(taskService.createTask(any(Task.class))).thenReturn(task);

    mockMvc.perform(post("/api/tasks")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createDto)))
      .andExpect(status().isCreated())
      .andExpect(header().string("X-API-Version", "2.0.0"));
  }
}
