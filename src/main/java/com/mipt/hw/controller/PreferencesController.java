package com.mipt.hw.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/preferences")
@Tag(name = "Preferences Controller", description = "Управление пользовательскими настройками")
public class PreferencesController {

  private static final String VIEW_PREFERENCE_COOKIE = "viewPreference";
  private static final String DEFAULT_VIEW_MODE = "detailed";
  private static final int COOKIE_MAX_AGE = 30 * 24 * 60 * 60;

  @Value("${api.version:2.0.0}")
  private String apiVersion;

  @Operation(summary = "Получить настройку отображения", description = "Возвращает текущий режим отображения из куки")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Успешное получение настройки",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
  })
  @GetMapping("/view")
  public ResponseEntity<Map<String, String>> getViewPreference(
    @CookieValue(value = VIEW_PREFERENCE_COOKIE, defaultValue = DEFAULT_VIEW_MODE)
    @Parameter(description = "Режим отображения", example = "detailed") String viewMode) {
    Map<String, String> response = new HashMap<>();
    response.put("viewMode", viewMode);

    return ResponseEntity.ok()
      .header("X-API-Version", apiVersion)
      .body(response);
  }

  @Operation(summary = "Установить настройку отображения", description = "Устанавливает режим отображения в куке")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Настройка успешно обновлена",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
    @ApiResponse(responseCode = "400", description = "Некорректный режим")
  })
  @PostMapping("/view")
  public ResponseEntity<Map<String, String>> setViewPreference(
    @Parameter(description = "Режим отображения (compact или detailed)", required = true, example = "compact")
    @RequestParam String mode,
    HttpServletResponse response) {

    if (!isValidMode(mode)) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", "Invalid mode. Use 'compact' or 'detailed'");

      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .header("X-API-Version", apiVersion)
        .body(errorResponse);
    }

    Cookie cookie = new Cookie(VIEW_PREFERENCE_COOKIE, mode);
    cookie.setMaxAge(COOKIE_MAX_AGE);
    cookie.setHttpOnly(false);
    cookie.setPath("/");
    response.addCookie(cookie);

    Map<String, String> responseBody = new HashMap<>();
    responseBody.put("viewMode", mode);
    responseBody.put("message", "View preference updated successfully");

    return ResponseEntity.ok()
      .header("X-API-Version", apiVersion)
      .body(responseBody);
  }

  @Operation(summary = "Сбросить настройку отображения", description = "Сбрасывает настройку к значению по умолчанию")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Настройка успешно сброшена",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
  })
  @DeleteMapping("/view")
  public ResponseEntity<Map<String, String>> resetViewPreference(HttpServletResponse response) {
    Cookie cookie = new Cookie(VIEW_PREFERENCE_COOKIE, DEFAULT_VIEW_MODE);
    cookie.setMaxAge(COOKIE_MAX_AGE);
    cookie.setHttpOnly(false);
    cookie.setPath("/");
    response.addCookie(cookie);

    Map<String, String> responseBody = new HashMap<>();
    responseBody.put("viewMode", DEFAULT_VIEW_MODE);
    responseBody.put("message", "View preference reset to default");

    return ResponseEntity.ok()
      .header("X-API-Version", apiVersion)
      .body(responseBody);
  }

  @Operation(summary = "Получить все настройки", description = "Возвращает все настройки пользователя")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Успешное получение настроек",
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
  })
  @GetMapping("/all")
  public ResponseEntity<Map<String, Object>> getAllPreferences(
    @CookieValue(value = VIEW_PREFERENCE_COOKIE, defaultValue = DEFAULT_VIEW_MODE)
    @Parameter(description = "Режим отображения", example = "detailed") String viewMode,
    HttpSession session) {

    Map<String, Object> preferences = new HashMap<>();
    preferences.put("viewMode", viewMode);
    preferences.put("favoritesCount", getFavoritesCount(session));
    preferences.put("sessionId", session.getId());

    return ResponseEntity.ok()
      .header("X-API-Version", apiVersion)
      .body(preferences);
  }

  private boolean isValidMode(String mode) {
    return "compact".equals(mode) || "detailed".equals(mode);
  }

  @SuppressWarnings("unchecked")
  private int getFavoritesCount(HttpSession session) {
    Object favorites = session.getAttribute("favoriteTaskIds");
    if (favorites instanceof java.util.Set) {
      return ((java.util.Set<?>) favorites).size();
    }
    return 0;
  }
}
