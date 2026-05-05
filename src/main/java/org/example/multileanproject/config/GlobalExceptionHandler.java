package org.example.multileanproject.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. BẮT LỖI VALIDATION (DTO)
    // Ví dụ: SĐT sai định dạng, Email rỗng...
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // Lấy lỗi đầu tiên gặp phải để hiển thị cho gọn
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put("error", errorMessage); // Key là "error" để Frontend dễ lấy
        });

        // Trả về message lỗi đầu tiên
        // Ví dụ trả về chuỗi: "Số điện thoại không hợp lệ..."
        String firstErrorMsg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(firstErrorMsg);
    }

    // 2. BẮT LỖI PHÂN QUYỀN - phải đứng TRƯỚC RuntimeException vì AccessDeniedException extends RuntimeException
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(errorBody(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện hành động này."));
    }

    // 3. BẮT LỖI LOGIC (Service)
    // Ví dụ: Trùng Email, Trùng SĐT...
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) {
            msg = "Đã xảy ra lỗi runtime.";
        }
        HttpStatus status = msg.contains("không có quyền") ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(errorBody(status, msg));
    }

    // 3.5 BẮT route/static resource không tồn tại (trả 404 thay vì 500)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFound(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody(HttpStatus.NOT_FOUND, "Không tìm thấy tài nguyên."));
    }

    // 4. BẮT CÁC LỖI KHÁC (Hệ thống)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex) {
        String msg = "Lỗi hệ thống: " + ex.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody(HttpStatus.INTERNAL_SERVER_ERROR, msg));
    }

    // Trả về cùng shape với Spring's ResponseStatusException: {status, error, message}
    private java.util.Map<String, Object> errorBody(HttpStatus status, String message) {
        java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}
