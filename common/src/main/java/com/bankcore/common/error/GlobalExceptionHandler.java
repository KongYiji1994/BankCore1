package com.bankcore.common.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {
        log.warn("business error code={}, message={}", ex.getErrorCode().getCode(), ex.getMessage());
        ApiError apiError = new ApiError(ex.getErrorCode().getCode(), ex.getMessage(), currentTrace());
        return new ResponseEntity<ApiError>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiError> handleBadRequest(Exception ex) {
        log.warn("validation error: {}", ex.getMessage());
        ApiError apiError = new ApiError(ErrorCode.INVALID_REQUEST.getCode(), ex.getMessage(), currentTrace());
        return new ResponseEntity<ApiError>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("unexpected error", ex);
        ApiError apiError = new ApiError(ErrorCode.INTERNAL_ERROR.getCode(), "Internal server error", currentTrace());
        return new ResponseEntity<ApiError>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String currentTrace() {
        return MDC.get(TraceIdFilter.TRACE_ID_KEY);
    }
}
