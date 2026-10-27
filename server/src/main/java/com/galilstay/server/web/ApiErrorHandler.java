package com.galilstay.server.web;

import com.galilstay.server.exception.BadLoginException;
import com.galilstay.server.exception.NotFoundException;
import com.galilstay.server.exception.RuleViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/** הופך חריגות לתשובת JSON אחידה: { status, message, fields, time } */
@RestControllerAdvice
public class ApiErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(NotFoundException e) {
        return body(HttpStatus.NOT_FOUND, e.getMessage(), Map.of());
    }

    @ExceptionHandler(RuleViolationException.class)
    public ResponseEntity<Map<String, Object>> rule(RuleViolationException e) {
        return body(HttpStatus.CONFLICT, e.getMessage(), Map.of());
    }

    @ExceptionHandler(BadLoginException.class)
    public ResponseEntity<Map<String, Object>> login(BadLoginException e) {
        return body(HttpStatus.UNAUTHORIZED, e.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException e) {
        Map<String, String> fields = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(f -> f.getField(), f -> String.valueOf(f.getDefaultMessage()), (a, b) -> a));
        String first = fields.values().stream().findFirst().orElse("נתונים לא תקינים");
        return body(HttpStatus.BAD_REQUEST, first, fields);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> unreadable(Exception e) {
        return body(HttpStatus.BAD_REQUEST, "הבקשה לא תקינה - יש לבדוק את הערכים שנשלחו", Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integrity(DataIntegrityViolationException e) {
        return body(HttpStatus.CONFLICT, "לא ניתן לבצע את הפעולה - הרשומה בשימוש", Map.of());
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message, Map<String, ?> fields) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(),
                "message", message,
                "fields", fields,
                "time", LocalDateTime.now().toString()
        ));
    }
}
