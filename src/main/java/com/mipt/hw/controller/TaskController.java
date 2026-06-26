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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Task Controller", description = "Управление задачами (CRUD операции)")
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

  @Operation(summary = "Получить все задачи", description = "Возвращает список всех задач с общим количеством")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Успешное получение списка задач",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class)))
  })
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

  @Operation(summary = "Получить задачу по ID", description = "Возвращает задачу с указанным идентификатором")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Успешное получение задачи",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class))),
    @ApiResponse(responseCode = "404", description = "Задача не найдена")
  })
  @GetMapping("/{id}")
  public ResponseEntity<TaskResponseDto> getTaskById(
    @Parameter(description = "ID задачи", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
    @PathVariable UUID id) {
    Task task = taskService.getTask(id);
    TaskResponseDto responseDto = taskMapper.toResponseDto(task);

    return ResponseEntity.ok()
      .header("X-API-Version", apiVersion)
      .body(responseDto);
  }

  @Operation(summary = "Проверить статус избранного", description = "Проверяет, добавлена ли задача в избранное")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Успешная проверка статуса")
  })
  @GetMapping("/{id}/favorite-status")
  public ResponseEntity<Map<String, Boolean>> checkFavoriteStatus(
    @Parameter(description = "ID задачи", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
    @PathVariable UUID id,
    HttpSession session) {
    boolean isFavorite = favoritesService.isFavorite(id, session);
    Map<String, Boolean> response = new HashMap<>();
    response.put("isFavorite", isFavorite);

    return ResponseEntity.ok()
      .header("X-API-Version", apiVersion)
      .body(response);
  }

  @Operation(summary = "Создать новую задачу", description = "Создает задачу с переданными данными")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Задача успешно создана",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class))),
    @ApiResponse(responseCode = "400", description = "Некорректные данные")
  })
  @PostMapping
  public ResponseEntity<TaskResponseDto> createTask(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Данные для создания задачи", required = true)
    @Validated(OnCreate.class) @RequestBody TaskCreateDto createDto) {
    Task task = taskMapper.toEntity(createDto);
    Task createdTask = taskService.createTask(task);
    TaskResponseDto responseDto = taskMapper.toResponseDto(createdTask);

    return ResponseEntity.status(HttpStatus.CREATED)
      .header("X-API-Version", apiVersion)
      .body(responseDto);
  }

  @Operation(summary = "Обновить задачу", description = "Обновляет задачу с указанным ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Задача успешно обновлена",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class))),
    @ApiResponse(responseCode = "400", description = "Некорректные данные"),
    @ApiResponse(responseCode = "404", description = "Задача не найдена")
  })
  @PutMapping("/{id}")
  public ResponseEntity<TaskResponseDto> updateTask(
    @Parameter(description = "ID задачи", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
    @PathVariable UUID id,
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Данные для обновления задачи", required = true)
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

  @Operation(summary = "Удалить задачу", description = "Удаляет задачу с указанным ID")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Задача успешно удалена"),
    @ApiResponse(responseCode = "404", description = "Задача не найдена")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTask(
    @Parameter(description = "ID задачи", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
    @PathVariable UUID id) {
    taskService.deleteTask(id);

    return ResponseEntity.noContent()
      .header("X-API-Version", apiVersion)
      .build();
  }
}
