package com.mipt.hw.controller;

import com.mipt.hw.dto.TaskResponseDto;
import com.mipt.hw.mapper.TaskMapper;
import com.mipt.hw.model.Priority;
import com.mipt.hw.model.Task;
import com.mipt.hw.service.FavoritesService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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

@WebMvcTest(FavoritesController.class)
class FavoritesControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private FavoritesService favoritesService;

  @MockBean
  private TaskMapper taskMapper;

  @MockBean
  private HttpSession session;

  @Test
  void shouldAddToFavorites() throws Exception {
    // Given
    UUID taskId = UUID.randomUUID();
    doNothing().when(favoritesService).addToFavorites(eq(taskId), any(HttpSession.class));

    // When & Then
    mockMvc.perform(post("/api/favorites/{taskId}", taskId))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"));
  }

  @Test
  void shouldReturn404WhenAddingNonExistentTask() throws Exception {
    // Given
    UUID taskId = UUID.randomUUID();
    doThrow(new RuntimeException("Task not found"))
      .when(favoritesService).addToFavorites(eq(taskId), any(HttpSession.class));

    // When & Then
    mockMvc.perform(post("/api/favorites/{taskId}", taskId))
      .andExpect(status().isInternalServerError());
  }

  @Test
  void shouldRemoveFromFavorites() throws Exception {
    // Given
    UUID taskId = UUID.randomUUID();
    doNothing().when(favoritesService).removeFromFavorites(eq(taskId), any(HttpSession.class));

    // When & Then
    mockMvc.perform(delete("/api/favorites/{taskId}", taskId))
      .andExpect(status().isNoContent())
      .andExpect(header().string("X-API-Version", "2.0.0"));
  }

  @Test
  void shouldGetFavorites() throws Exception {
    // Given
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    Task task1 = new Task();
    task1.setId(id1);
    task1.setTitle("Task 1");
    task1.setPriority(Priority.HIGH);
    task1.setCreatedAt(LocalDateTime.now());

    Task task2 = new Task();
    task2.setId(id2);
    task2.setTitle("Task 2");
    task2.setPriority(Priority.MEDIUM);
    task2.setCreatedAt(LocalDateTime.now());

    TaskResponseDto dto1 = new TaskResponseDto();
    dto1.setId(id1);
    dto1.setTitle("Task 1");
    dto1.setPriority(Priority.HIGH);

    TaskResponseDto dto2 = new TaskResponseDto();
    dto2.setId(id2);
    dto2.setTitle("Task 2");
    dto2.setPriority(Priority.MEDIUM);

    List<Task> favoriteTasks = Arrays.asList(task1, task2);
    List<TaskResponseDto> expectedDtos = Arrays.asList(dto1, dto2);

    when(favoritesService.getFavoriteTasks(any(HttpSession.class))).thenReturn(favoriteTasks);
    when(taskMapper.toResponseDto(task1)).thenReturn(dto1);
    when(taskMapper.toResponseDto(task2)).thenReturn(dto2);

    // When & Then
    mockMvc.perform(get("/api/favorites"))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$[0].id").value(id1.toString()))
      .andExpect(jsonPath("$[0].title").value("Task 1"))
      .andExpect(jsonPath("$[1].id").value(id2.toString()))
      .andExpect(jsonPath("$[1].title").value("Task 2"));
  }

  @Test
  void shouldReturnEmptyListWhenNoFavorites() throws Exception {
    // Given
    when(favoritesService.getFavoriteTasks(any(HttpSession.class))).thenReturn(Arrays.asList());

    // When & Then
    mockMvc.perform(get("/api/favorites"))
      .andExpect(status().isOk())
      .andExpect(header().string("X-API-Version", "2.0.0"))
      .andExpect(jsonPath("$").isEmpty());
  }
}
