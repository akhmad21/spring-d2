package com.example.demo.exception;

import com.example.demo.dto.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Format input tidak valid");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(errorMessage));
    }

    @ExceptionHandler(DuplicateKalimatException.class)
    public ResponseEntity<BaseResponse<Void>> handleDuplicateKalimat(DuplicateKalimatException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(BaseResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InvalidKalimatException.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidKalimat(InvalidKalimatException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BaseResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.error("Terjadi kesalahan internal: " + ex.getMessage()));
    }
}
