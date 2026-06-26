package com.mipt.hw.repository;

import com.mipt.hw.model.Priority;
import com.mipt.hw.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

  @Autowired
  private TaskRepository taskRepository;

  private Task createTask(String title, Priority priority, boolean completed, LocalDate dueDate) {
    Task task = new Task();
    task.setTitle(title);
    task.setPriority(priority);
    task.setCompleted(completed);
    task.setDueDate(dueDate);
    task.setCreatedAt(LocalDateTime.now());
    task.setUpdatedAt(LocalDateTime.now());
    return task;
  }

  @BeforeEach
  void setUp() {
    taskRepository.deleteAll();
  }

  @Test
  void shouldFindByCompleted() {
    // given
    Task completedTask = createTask("Completed Task", Priority.HIGH, true, LocalDate.now());
    Task notCompletedTask = createTask("Not Completed Task", Priority.MEDIUM, false, LocalDate.now());
    taskRepository.saveAll(List.of(completedTask, notCompletedTask));


    List<Task> completedTasks = taskRepository.findByCompleted(true);


    assertThat(completedTasks).hasSize(1);
    assertThat(completedTasks.get(0).getTitle()).isEqualTo("Completed Task");
  }

  @Test
  void shouldFindByPriority() {
    // given
    Task highPriorityTask = createTask("High Priority", Priority.HIGH, false, LocalDate.now());
    Task mediumPriorityTask = createTask("Medium Priority", Priority.MEDIUM, false, LocalDate.now());
    taskRepository.saveAll(List.of(highPriorityTask, mediumPriorityTask));


    List<Task> highPriorityTasks = taskRepository.findByPriority(Priority.HIGH);


    assertThat(highPriorityTasks).hasSize(1);
    assertThat(highPriorityTasks.get(0).getPriority()).isEqualTo(Priority.HIGH);
  }

  @Test
  void shouldFindByCompletedAndPriority() {
    // given
    Task task1 = createTask("Completed High", Priority.HIGH, true, LocalDate.now());
    Task task2 = createTask("Not Completed High", Priority.HIGH, false, LocalDate.now());
    Task task3 = createTask("Completed Medium", Priority.MEDIUM, true, LocalDate.now());
    taskRepository.saveAll(List.of(task1, task2, task3));


    List<Task> found = taskRepository.findByCompletedAndPriority(true, Priority.HIGH);


    assertThat(found).hasSize(1);
    assertThat(found.get(0).getTitle()).isEqualTo("Completed High");
  }

  @Test
  void shouldFindByDueDateBefore() {
    // given
    Task pastTask = createTask("Past Task", Priority.HIGH, false, LocalDate.now().minusDays(5));
    Task futureTask = createTask("Future Task", Priority.MEDIUM, false, LocalDate.now().plusDays(5));
    taskRepository.saveAll(List.of(pastTask, futureTask));


    List<Task> found = taskRepository.findByDueDateBefore(LocalDate.now());


    assertThat(found).hasSize(1);
    assertThat(found.get(0).getTitle()).isEqualTo("Past Task");
  }

  @Test
  void shouldFindByTitleContainingIgnoreCase() {
    // given
    Task task1 = createTask("Important Task", Priority.HIGH, false, LocalDate.now());
    Task task2 = createTask("Another Task", Priority.MEDIUM, false, LocalDate.now());
    taskRepository.saveAll(List.of(task1, task2));


    List<Task> found = taskRepository.findByTitleContainingIgnoreCase("important");


    assertThat(found).hasSize(1);
    assertThat(found.get(0).getTitle()).isEqualTo("Important Task");
  }

  @Test
  void shouldFindTasksDueBetween() {
    // given
    LocalDate start = LocalDate.now().plusDays(1);
    LocalDate end = LocalDate.now().plusDays(10);

    Task task1 = createTask("Task 1", Priority.HIGH, false, LocalDate.now().plusDays(5));
    Task task2 = createTask("Task 2", Priority.MEDIUM, false, LocalDate.now().plusDays(15));
    taskRepository.saveAll(List.of(task1, task2));


    List<Task> found = taskRepository.findTasksDueBetween(start, end);


    assertThat(found).hasSize(1);
    assertThat(found.get(0).getTitle()).isEqualTo("Task 1");
  }

  @Test
  void shouldFindTasksDueWithinNextWeek() {
    // given
    Task task1 = createTask("Due in 3 days", Priority.HIGH, false, LocalDate.now().plusDays(3));
    Task task2 = createTask("Due in 10 days", Priority.MEDIUM, false, LocalDate.now().plusDays(10));
    taskRepository.saveAll(List.of(task1, task2));


    List<Task> found = taskRepository.findTasksDueWithinNextWeek();


    assertThat(found).isNotEmpty();
    assertThat(found.get(0).getTitle()).isEqualTo("Due in 3 days");
  }

  @Test
  void shouldFindByIdWithAttachments() {
    Task task = createTask("Task with attachments", Priority.HIGH, false, LocalDate.now());
    taskRepository.save(task);


    Task found = taskRepository.findByIdWithAttachments(task.getId());


    assertThat(found).isNotNull();
    assertThat(found.getId()).isEqualTo(task.getId());
    assertThat(found.getAttachments()).isEmpty(); // Нет вложений
  }

  @Test
  void shouldFindAllWithAttachments() {
    // given
    Task task1 = createTask("Task 1", Priority.HIGH, false, LocalDate.now());
    Task task2 = createTask("Task 2", Priority.MEDIUM, false, LocalDate.now());
    taskRepository.saveAll(List.of(task1, task2));


    List<Task> found = taskRepository.findAllWithAttachments();


    assertThat(found).hasSize(2);
    assertThat(found.get(0).getAttachments()).isNotNull();
  }
}
