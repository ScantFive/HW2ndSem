package com.mipt.hw.scope;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Component
@Scope("prototype")
public class PrototypeScopedBean {
  private static final Logger log = LoggerFactory.getLogger(PrototypeScopedBean.class);
  private static int instanceCounter = 0;

  private final String beanId;
  private final int instanceNumber;

  public PrototypeScopedBean() {
    this.beanId = UUID.randomUUID().toString();
    this.instanceNumber = ++instanceCounter;

    log.info("PrototypeScopedBean #{} создан с ID: {}", instanceNumber, beanId);
  }

  @PostConstruct
  public void init() {
    log.info("PrototypeScopedBean #{} инициализирован", instanceNumber);
  }

  @PreDestroy
  public void destroy() {
    log.info("PrototypeScopedBean #{} уничтожен", instanceNumber);
  }

  public String generateTaskId() {
    String taskId = UUID.randomUUID().toString();
    log.info("PrototypeScopedBean #{} сгенерировал Task ID: {}", instanceNumber, taskId);
    return taskId;
  }

  public String getBeanId() {
    return beanId;
  }

  public int getInstanceNumber() {
    return instanceNumber;
  }
}