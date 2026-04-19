package com.mipt.hw.mapper;

import com.mipt.hw.dto.TaskCreateDto;
import com.mipt.hw.dto.TaskResponseDto;
import com.mipt.hw.dto.TaskUpdateDto;
import com.mipt.hw.model.Task;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TaskMapper {

  Task toEntity(TaskCreateDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntity(TaskUpdateDto dto, @MappingTarget Task task);

  TaskResponseDto toResponseDto(Task task);
}
