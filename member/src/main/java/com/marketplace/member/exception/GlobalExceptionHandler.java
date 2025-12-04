package com.marketplace.member.exception;

import com.marketplace.member.model.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();

    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      errors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }

    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("success", false);
    responseBody.put("message", "Validation failed");
    responseBody.put("errors", errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, Object>> handleDuplicateResource(RuntimeException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("success", false, "message", ex.getMessage()));
  }

  @ExceptionHandler(MemberNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleMemberNotFound(MemberNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.<Void>builder()
            .success(false)
            .message(ex.getMessage())
            .data(null)
            .build());
  }
}
