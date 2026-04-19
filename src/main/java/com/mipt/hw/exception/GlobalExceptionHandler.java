package com.mipt.hw.exception;

import com.mipt.hw.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @Value("${spring.profiles.active:dev}")
  private String activeProfile;

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
    MethodArgumentNotValidException ex, HttpServletRequest request) {

    Map<String, Object> details = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      details.put(fieldName, errorMessage);
    });

    ErrorResponse errorResponse = ErrorResponse.builder()
      .timestamp(Instant.now())
      .status(HttpStatus.BAD_REQUEST.value())
      .error("Validation Failed")
      .message("Invalid request parameters")
      .path(request.getRequestURI())
      .details(details)
      .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
    ConstraintViolationException ex, HttpServletRequest request) {

    Map<String, Object> details = ex.getConstraintViolations().stream()
      .collect(Collectors.toMap(
        violation -> violation.getPropertyPath().toString(),
        ConstraintViolation::getMessage,
        (msg1, msg2) -> msg1 + "; " + msg2
      ));

    ErrorResponse errorResponse = ErrorResponse.builder()
      .timestamp(Instant.now())
      .status(HttpStatus.BAD_REQUEST.value())
      .error("Constraint Violation")
      .message("Invalid request parameters")
      .path(request.getRequestURI())
      .details(details)
      .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParams(
    MissingServletRequestParameterException ex, HttpServletRequest request) {

    Map<String, Object> details = new HashMap<>();
    details.put("parameter", ex.getParameterName());
    details.put("type", ex.getParameterType());

    ErrorResponse errorResponse = ErrorResponse.builder()
      .timestamp(Instant.now())
      .status(HttpStatus.BAD_REQUEST.value())
      .error("Missing Parameter")
      .message(ex.getMessage())
      .path(request.getRequestURI())
      .details(details)
      .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
    HttpMessageNotReadableException ex, HttpServletRequest request) {

    ErrorResponse errorResponse = ErrorResponse.builder()
      .timestamp(Instant.now())
      .status(HttpStatus.BAD_REQUEST.value())
      .error("Malformed JSON Request")
      .message("Request body is not readable or has invalid format")
      .path(request.getRequestURI())
      .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(
    IllegalArgumentException ex, HttpServletRequest request) {

    ErrorResponse errorResponse = ErrorResponse.builder()
      .timestamp(Instant.now())
      .status(HttpStatus.BAD_REQUEST.value())
      .error("Bad Request")
      .message(ex.getMessage())
      .path(request.getRequestURI())
      .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(TaskNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleTaskNotFound(
    TaskNotFoundException ex, HttpServletRequest request) {

    ErrorResponse errorResponse = ErrorResponse.builder()
      .timestamp(Instant.now())
      .status(HttpStatus.NOT_FOUND.value())
      .error("Task Not Found")
      .message(ex.getMessage())
      .path(request.getRequestURI())
      .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(AttachmentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAttachmentNotFound(
    AttachmentNotFoundException ex, HttpServletRequest request) {

    ErrorResponse errorResponse = ErrorResponse.builder()
      .timestamp(Instant.now())
      .status(HttpStatus.NOT_FOUND.value())
      .error("Attachment Not Found")
      .message(ex.getMessage())
      .path(request.getRequestURI())
      .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoHandlerFound(
    NoHandlerFoundException ex, HttpServletRequest request) {

    Map<String, Object> details = new HashMap<>();
    details.put("httpMethod", ex.getHttpMethod());
    details.put("url", ex.getRequestURL());

    ErrorResponse errorResponse = ErrorResponse.builder()
      .timestamp(Instant.now())
      .status(HttpStatus.NOT_FOUND.value())
      .error("Endpoint Not Found")
      .message("The requested endpoint does not exist")
      .path(request.getRequestURI())
      .details(details)
      .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
    Exception ex, HttpServletRequest request) {

    ErrorResponse.ErrorResponseBuilder builder = ErrorResponse.builder()
      .timestamp(Instant.now())
      .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
      .error("Internal Server Error")
      .path(request.getRequestURI());

    if ("dev".equals(activeProfile)) {
      builder.message(ex.getMessage());
      Map<String, Object> details = new HashMap<>();
      details.put("exception", ex.getClass().getName());
      details.put("stackTrace", ex.getStackTrace());
      builder.details(details);
    } else {
      builder.message("An unexpected error occurred. Please try again later.");
    }

    ErrorResponse errorResponse = builder.build();
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
