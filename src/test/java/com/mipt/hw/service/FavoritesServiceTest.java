package com.mipt.hw.service;

import com.mipt.hw.model.Task;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoritesServiceTest {

  @Mock
  private TaskService taskService;

  @Mock
  private HttpSession session;

  private FavoritesService favoritesService;

  @BeforeEach
  void setUp() {
    favoritesService = new FavoritesService(taskService);
  }

  @Test
  void shouldAddToFavorites() {
    // Given
    UUID taskId = UUID.randomUUID();
    Task task = new Task();
    task.setId(taskId);

    when(session.getAttribute("favoriteTaskIds")).thenReturn(null);
    when(taskService.getTask(taskId)).thenReturn(task);

    // When
    favoritesService.addToFavorites(taskId, session);

    // Then
    verify(session).setAttribute(eq("favoriteTaskIds"), any(Set.class));
  }

  @Test
  void shouldRemoveFromFavorites() {
    // Given
    UUID taskId = UUID.randomUUID();
    Set<UUID> favorites = new HashSet<>(Set.of(taskId));
    when(session.getAttribute("favoriteTaskIds")).thenReturn(favorites);

    // When
    favoritesService.removeFromFavorites(taskId, session);

    // Then
    verify(session).setAttribute(eq("favoriteTaskIds"), any(Set.class));

    ArgumentCaptor<Set<UUID>> captor = ArgumentCaptor.forClass(Set.class);
    verify(session).setAttribute(eq("favoriteTaskIds"), captor.capture());
    Set<UUID> capturedSet = captor.getValue();
    assertThat(capturedSet).doesNotContain(taskId);
    assertThat(capturedSet).isEmpty();
  }

  @Test
  void shouldGetFavoriteTasks() {
    // Given
    UUID taskId1 = UUID.randomUUID();
    UUID taskId2 = UUID.randomUUID();
    Set<UUID> favorites = new HashSet<>(Set.of(taskId1, taskId2));

    Task task1 = new Task();
    task1.setId(taskId1);
    Task task2 = new Task();
    task2.setId(taskId2);

    when(session.getAttribute("favoriteTaskIds")).thenReturn(favorites);
    when(taskService.getTaskOpt(taskId1)).thenReturn(Optional.of(task1));
    when(taskService.getTaskOpt(taskId2)).thenReturn(Optional.of(task2));

    // When
    List<Task> result = favoritesService.getFavoriteTasks(session);

    // Then
    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyInAnyOrder(task1, task2);
  }

  @Test
  void shouldReturnEmptyListWhenNoFavorites() {
    // Given
    when(session.getAttribute("favoriteTaskIds")).thenReturn(null);

    // When
    List<Task> result = favoritesService.getFavoriteTasks(session);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void shouldCheckIfTaskIsFavorite() {
    // Given
    UUID taskId = UUID.randomUUID();
    Set<UUID> favorites = new HashSet<>(Set.of(taskId));
    when(session.getAttribute("favoriteTaskIds")).thenReturn(favorites);

    // When
    boolean result = favoritesService.isFavorite(taskId, session);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void shouldReturnFalseWhenTaskNotFavorite() {
    // Given
    UUID taskId = UUID.randomUUID();
    Set<UUID> favorites = new HashSet<>();
    when(session.getAttribute("favoriteTaskIds")).thenReturn(favorites);

    // When
    boolean result = favoritesService.isFavorite(taskId, session);

    // Then
    assertThat(result).isFalse();
  }
}
