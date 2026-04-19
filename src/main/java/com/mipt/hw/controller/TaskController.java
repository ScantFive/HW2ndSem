package com.mipt.hw.controller;

import com.mipt.hw.dto.TaskCreateDto;
import com.mipt.hw.dto.TaskResponseDto;
import com.mipt.hw.dto.TaskUpdateDto;
import com.mipt.hw.mapper.TaskMapper;
import com.mipt.hw.model.Task;
import com.mipt.hw.service.FavoritesService;
import com.mipt.hw.service.TaskService;
import com.mipt.hw.valid.OnCreate;
import com.mipt.hw.valid.OnUpdate;
import com.mipt.hw.valid.TaskUpdateContext;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final TaskService taskService;
  private final TaskMapper taskMapper;
  private final Validator validator;
  private final FavoritesService favoritesService;

  @Value("${api.version:2.0.0}")
  private String apiVersion;

  public TaskController(TaskService taskService, TaskMapper taskMapper,
                        Validator validator, FavoritesService favoritesService) {
    this.taskService = taskService;
    this.taskMapper = taskMapper;
    this.validator = validator;
    this.favoritesService = favoritesService;
  }

  @GetMapping
  public ResponseEntity<List<TaskResponseDto>> getAllTasks() {
    List<TaskResponseDto> tasks = taskService.getAllTasks().stream()
      .map(taskMapper::toResponseDto)
      .collect(Collectors.toList());

    long totalCount = taskService.getTaskCount();

    return ResponseEntity.ok()
      .header("X-API-Version", apiVersion)
      .header("X-Total-Count", String.valueOf(totalCount))
      .body(tasks);
  }

  @GetMapping("/{id}")
  public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable UUID id) {
    Task task = taskService.getTask(id);
    TaskResponseDto responseDto = taskMapper.toResponseDto(task);

    return ResponseEntity.ok()
      .header("X-API-Version", apiVersion)
      .body(responseDto);
  }

  @GetMapping("/{id}/favorite-status")
  public ResponseEntity<Map<String, Boolean>> checkFavoriteStatus(
    @PathVariable UUID id,
    HttpSession session) {
    boolean isFavorite = favoritesService.isFavorite(id, session);
    Map<String, Boolean> response = new HashMap<>();
    response.put("isFavorite", isFavorite);

    return ResponseEntity.ok()
      .header("X-API-Version", apiVersion)
      .body(response);
  }

  @PostMapping
  public ResponseEntity<TaskResponseDto> createTask(
    @Validated(OnCreate.class) @RequestBody TaskCreateDto createDto) {
    Task task = taskMapper.toEntity(createDto);
    Task createdTask = taskService.createTask(task);
    TaskResponseDto responseDto = taskMapper.toResponseDto(createdTask);

    return ResponseEntity.status(HttpStatus.CREATED)
      .header("X-API-Version", apiVersion)
      .body(responseDto);
  }

  @PutMapping("/{id}")
  public ResponseEntity<TaskResponseDto> updateTask(
    @PathVariable UUID id,
    @Validated(OnUpdate.class) @RequestBody TaskUpdateDto updateDto) {
    if (updateDto.getDueDate() != null) {
      TaskUpdateContext context = new TaskUpdateContext(id, updateDto.getDueDate(), taskService);
      var violations = validator.validate(context);
      if (!violations.isEmpty()) {
        throw new IllegalArgumentException(violations.iterator().next().getMessage());
      }
    }

    Task updatedTask = taskService.updateTask(id, updateDto);
    TaskResponseDto responseDto = taskMapper.toResponseDto(updatedTask);

    return ResponseEntity.ok()
      .header("X-API-Version", apiVersion)
      .body(responseDto);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
    taskService.deleteTask(id);

    return ResponseEntity.noContent()
      .header("X-API-Version", apiVersion)
      .build();
  }
}
