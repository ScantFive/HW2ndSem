package com.mipt.hw.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {
  private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

  @Pointcut("within(com.mipt.hw.service..*)")
  public void serviceLayer() {}

  @Around("serviceLayer()")
  public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    Object[] args = joinPoint.getArgs();

    log.info("Начало выполнения: {}.{}() | Аргументы: {}",
      className, methodName, Arrays.toString(args));

    long startTime = System.currentTimeMillis();
    Object result = null;

    try {
      result = joinPoint.proceed();
      long endTime = System.currentTimeMillis();

      if (result != null) {
        log.info("Завершение: {}.{}() | Результат: {} | Время выполнения: {} мс",
          className, methodName, result, (endTime - startTime));
      } else {
        log.info("Завершение: {}.{}() | Результат: void | Время выполнения: {} мс",
          className, methodName, (endTime - startTime));
      }

      return result;

    } catch (Throwable throwable) {
      long endTime = System.currentTimeMillis();
      log.error("Ошибка в: {}.{}() | Тип ошибки: {} | Сообщение: {} | Время выполнения: {} мс",
        className, methodName,
        throwable.getClass().getSimpleName(),
        throwable.getMessage(),
        (endTime - startTime));
      throw throwable;
    }
  }
}
