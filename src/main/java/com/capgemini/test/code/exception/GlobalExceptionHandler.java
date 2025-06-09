package com.capgemini.test.code.exception;

import feign.FeignException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //El feign lanza la excepcion del dni tras la validacion en api externa y yo la recojo
    @ExceptionHandler(FeignException.Conflict.class)
    public ResponseEntity<Map<String, Object>> handleFeignConflict(FeignException.Conflict ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code", 409, "message", "error validation dni"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError().getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("code", 400, "message", message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(DataIntegrityViolationException ex) {
        String message = "error validation phone"; // mensaje que tú quieras
        if (ex.getMessage().contains("users_phone_key")) {
            message = "El teléfono ya está registrado";
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code", 409, "message", message));
    }

    //El email lo valido yo en el serviceimpl, y recojo tambien la excepcion
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("code", ex.getStatusCode().value(), "message", ex.getReason()));
    }
}
