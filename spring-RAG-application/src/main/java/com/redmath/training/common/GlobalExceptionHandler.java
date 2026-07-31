package com.redmath.training.common;

import java.io.UncheckedIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  // 1. Fixed: Do not pass raw `e.getMessage()` to ApiError. Log it, return sanitized message.
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException e) {
    log.warn("Invalid client argument: {}", e.getMessage());
    return ResponseEntity.badRequest().body(new ApiError("Invalid request parameter or argument"));
  }

  // 2. Handles standard Spring @Valid validation failures
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e) {
    return ResponseEntity.badRequest().body(new ApiError("Invalid request payload"));
  }

  // 3. Handles missing parameters (e.g. missing "file" key in request)
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiError> handleMissingParams(MissingServletRequestParameterException e) {
    log.warn("Missing parameter: {}", e.getParameterName());
    return ResponseEntity.badRequest().body(new ApiError("Required parameter '%s' is missing".formatted(e.getParameterName())));
  }

  // 4. Handles oversized files / bad multipart data explicitly
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiError> handleMaxSizeException(MaxUploadSizeExceededException e) {
    log.warn("File upload size limit exceeded");
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
        .body(new ApiError("Uploaded file size exceeds maximum allowed limit"));
  }

  @ExceptionHandler(MultipartException.class)
  public ResponseEntity<ApiError> handleMultipartException(MultipartException e) {
    log.warn("Multipart parsing failure: {}", e.getMessage());
    return ResponseEntity.badRequest().body(new ApiError("Invalid multipart request or malformed upload data"));
  }

  // 5. Handles file I/O failures safely
  @ExceptionHandler(UncheckedIOException.class)
  public ResponseEntity<ApiError> handleIoError(UncheckedIOException e) {
    log.error("I/O failure during request processing", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiError("Failed to process file"));
  }

  // 6. Final safety net for all unhandled exceptions
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnexpected(Exception e) {
    log.error("Unhandled exception", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiError("Unexpected error occurred"));
  }
}