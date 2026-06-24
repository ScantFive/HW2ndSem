package com.mipt.hw.service;

import com.mipt.hw.model.Task;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FavoritesService {
  private static final String FAVORITE_TASK_IDS_ATTR = "favoriteTaskIds";
  private final TaskService taskService;

  public FavoritesService(TaskService taskService) {
    this.taskService = taskService;
  }

  public void addToFavorites(UUID taskId, HttpSession session) {
    taskService.getTask(taskId);

    Set<UUID> favorites = getFavoritesSet(session);
    favorites.add(taskId);
    session.setAttribute(FAVORITE_TASK_IDS_ATTR, favorites);
  }

  public void removeFromFavorites(UUID taskId, HttpSession session) {
    Set<UUID> favorites = getFavoritesSet(session);
    favorites.remove(taskId);
    session.setAttribute(FAVORITE_TASK_IDS_ATTR, favorites);
  }

  public List<Task> getFavoriteTasks(HttpSession session) {
    Set<UUID> favorites = getFavoritesSet(session);
    List<Task> favoriteTasks = new ArrayList<>();

    for (UUID taskId : favorites) {
      taskService.getTaskOpt(taskId).ifPresent(favoriteTasks::add);
    }

    return favoriteTasks;
  }

  public boolean isFavorite(UUID taskId, HttpSession session) {
    Set<UUID> favorites = getFavoritesSet(session);
    return favorites.contains(taskId);
  }

  @SuppressWarnings("unchecked")
  private Set<UUID> getFavoritesSet(HttpSession session) {
    Object attribute = session.getAttribute(FAVORITE_TASK_IDS_ATTR);
    if (attribute == null) {
      return new HashSet<>();
    }
    return new HashSet<>((Set<UUID>) attribute);
  }
}
