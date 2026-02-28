package com.mipt.hw.controller;

import com.mipt.hw.model.Task;
import com.mipt.hw.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
  private final TaskService taskService;

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  // GET
  @GetMapping
  public ResponseEntity<List<Task>> getAllTasks() {
    List<Task> tasks = taskService.getAllTasks();
    return new ResponseEntity<>(tasks, HttpStatus.OK);
  }

  // GET
  @GetMapping("/{id}")
  public ResponseEntity<Task> getTaskById(@PathVariable UUID id) {
    try {
      Task task = taskService.getTask(id);
      return new ResponseEntity<>(task, HttpStatus.OK);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  // POST
  @PostMapping
  public ResponseEntity<Task> createTask(@RequestBody Task task) {
    try {
      Task createdTask = taskService.createTask(task);
      return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  // PUT
  @PutMapping("/{id}")
  public ResponseEntity<Task> updateTask(@PathVariable UUID id, @RequestBody Task task) {
    try {
      Task updatedTask = taskService.updateTask(id, task);
      return new ResponseEntity<>(updatedTask, HttpStatus.OK);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  // DELETE
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
    try {
      taskService.deleteTask(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }
}
