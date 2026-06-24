package com.mipt.hw.repository;

import com.mipt.hw.model.Priority;
import com.mipt.hw.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

  List<Task> findByCompleted(boolean completed);

  List<Task> findByPriority(Priority priority);

  List<Task> findByCompletedAndPriority(boolean completed, Priority priority);

  List<Task> findByDueDateBefore(LocalDate date);

  List<Task> findByTitleContainingIgnoreCase(String keyword);

  @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :startDate AND :endDate")
  List<Task> findTasksDueBetween(@Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

  @Query(value = "SELECT * FROM tasks WHERE due_date BETWEEN CURRENT_DATE AND CURRENT_DATE + 7",
    nativeQuery = true)
  List<Task> findTasksDueWithinNextWeek();

  @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.attachments WHERE t.id = :id")
  Task findByIdWithAttachments(@Param("id") UUID id);

  @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.attachments")
  List<Task> findAllWithAttachments();
}
