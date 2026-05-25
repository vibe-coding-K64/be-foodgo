package com.example.be_foodgo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> xuLyBusinessException(BusinessException ex) {
        log.warn("Business exception - Mã lỗi: {}, Thông báo: {}", ex.getErrorCode(), ex.getMessage());
        ApiResponse<Void> response = ApiResponse.thatError(
                ex.getStatus().value(),
                ex.getMessage()
        );
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> xuLyValidationException(MethodArgumentNotValidException ex) {
        String thongBao = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", thongBao);
        ApiResponse<Void> response = ApiResponse.thatError(
                HttpStatus.BAD_REQUEST.value(),
                "Dữ liệu không hợp lệ: " + thongBao
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> xuLyException(Exception ex) {
        log.error("Lỗi không xử lý được: {}", ex.getMessage(), ex);
        ApiResponse<Void> response = ApiResponse.thatError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
