package com.mipt.hw.mapper;

import com.mipt.hw.dto.AttachmentResponseDto;
import com.mipt.hw.dto.TaskCreateDto;
import com.mipt.hw.dto.TaskResponseDto;
import com.mipt.hw.dto.TaskUpdateDto;
import com.mipt.hw.model.Task;
import com.mipt.hw.model.TaskAttachment;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "completed", constant = "false")
  @Mapping(target = "attachments", ignore = true)
  Task toEntity(TaskCreateDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "attachments", ignore = true)
  void updateEntity(TaskUpdateDto dto, @MappingTarget Task task);

  TaskResponseDto toResponseDto(Task task);
  AttachmentResponseDto attachmentToResponseDto(TaskAttachment attachment);
}