package com.mipt.hw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.mipt.hw.repository.StubTaskRepository;
import com.mipt.hw.repository.TaskRepository;

@Configuration
public class RepositoryConfig {

  @Bean
  public TaskRepository stubTaskRepository() {
    StubTaskRepository repository = new StubTaskRepository();
    repository.init();
    return repository;
  }
}
