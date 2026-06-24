package com.mipt.hw.repository;

import com.mipt.hw.model.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {

  List<TaskAttachment> findByTaskId(UUID taskId);

  @Modifying
  @Query("DELETE FROM TaskAttachment a WHERE a.task.id = :taskId")
  void deleteByTaskId(@Param("taskId") UUID taskId);

  @Query("SELECT a FROM TaskAttachment a JOIN FETCH a.task WHERE a.id = :id")
  TaskAttachment findByIdWithTask(@Param("id") Long id);
}