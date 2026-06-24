package com.mipt.hw.controller;

import com.mipt.hw.dto.TaskResponseDto;
import com.mipt.hw.mapper.TaskMapper;
import com.mipt.hw.model.Task;
import com.mipt.hw.service.FavoritesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "Favorites Controller", description = "Управление избранными задачами")
public class FavoritesController {

  private final FavoritesService favoritesService;
  private final TaskMapper taskMapper;

  @Value("${api.version:2.0.0}")
  private String apiVersion;

  public FavoritesController(FavoritesService favoritesService, TaskMapper taskMapper) {
    this.favoritesService = favoritesService;
    this.taskMapper = taskMapper;
  }

  @Operation(summary = "Добавить в избранное", description = "Добавляет задачу в список избранных")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Задача успешно добавлена в избранное"),
    @ApiResponse(responseCode = "404", description = "Задача не найдена")
  })
  @PostMapping("/{taskId}")
  public ResponseEntity<Void> addToFavorites(
    @Parameter(description = "ID задачи", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
    @PathVariable UUID taskId,
    HttpSession session) {
    favoritesService.addToFavorites(taskId, session);

    return ResponseEntity.ok()
      .header("X-API-Version", apiVersion)
      .build();
  }

  @Operation(summary = "Удалить из избранного", description = "Удаляет задачу из списка избранных")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Задача успешно удалена из избранного"),
    @ApiResponse(responseCode = "404", description = "Задача не найдена")
  })
  @DeleteMapping("/{taskId}")
  public ResponseEntity<Void> removeFromFavorites(
    @Parameter(description = "ID задачи", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
    @PathVariable UUID taskId,
    HttpSession session) {
    favoritesService.removeFromFavorites(taskId, session);

    return ResponseEntity.noContent()
      .header("X-API-Version", apiVersion)
      .build();
  }

  @Operation(summary = "Получить избранные задачи", description = "Возвращает список всех избранных задач")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Успешное получение списка избранных задач",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class)))
  })
  @GetMapping
  public ResponseEntity<List<TaskResponseDto>> getFavorites(HttpSession session) {
    List<Task> favoriteTasks = favoritesService.getFavoriteTasks(session);
    List<TaskResponseDto> responseDtos = favoriteTasks.stream()
      .map(taskMapper::toResponseDto)
      .collect(Collectors.toList());

    return ResponseEntity.ok()
      .header("X-API-Version", apiVersion)
      .body(responseDtos);
  }
}
