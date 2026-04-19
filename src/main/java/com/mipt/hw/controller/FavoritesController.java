package com.mipt.hw.controller;

import com.mipt.hw.dto.TaskResponseDto;
import com.mipt.hw.mapper.TaskMapper;
import com.mipt.hw.model.Task;
import com.mipt.hw.service.FavoritesService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
public class FavoritesController {
  private final FavoritesService favoritesService;
  private final TaskMapper taskMapper;

  @Value("${api.version:2.0.0}")
  private String apiVersion;

  public FavoritesController(FavoritesService favoritesService, TaskMapper taskMapper) {
    this.favoritesService = favoritesService;
    this.taskMapper = taskMapper;
  }

  @PostMapping("/{taskId}")
  public ResponseEntity<Void> addToFavorites(@PathVariable UUID taskId, HttpSession session) {
    favoritesService.addToFavorites(taskId, session);

    return ResponseEntity.ok()
      .header("X-API-Version", apiVersion)
      .build();
  }

  @DeleteMapping("/{taskId}")
  public ResponseEntity<Void> removeFromFavorites(@PathVariable UUID taskId, HttpSession session) {
    favoritesService.removeFromFavorites(taskId, session);

    return ResponseEntity.noContent()
      .header("X-API-Version", apiVersion)
      .build();
  }

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
