package com.mipt.hw.valid;

import com.mipt.hw.model.Task;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DueDateNotBeforeCreationValidator implements ConstraintValidator<DueDateNotBeforeCreation, TaskUpdateContext> {

  @Override
  public boolean isValid(TaskUpdateContext context, ConstraintValidatorContext validatorContext) {
    if (context == null || context.getDueDate() == null || context.getTaskId() == null) {
      return true;
    }

    Task existingTask = context.getTaskService().getTask(context.getTaskId());
    LocalDate createdAt = existingTask.getCreatedAt().toLocalDate();
    LocalDate dueDate = context.getDueDate();

    if (dueDate.isBefore(createdAt)) {
      validatorContext.disableDefaultConstraintViolation();
      validatorContext.buildConstraintViolationWithTemplate(
        "Due date (" + dueDate + ") cannot be before creation date (" + createdAt + ")"
      ).addConstraintViolation();
      return false;
    }
    return true;
  }
}
