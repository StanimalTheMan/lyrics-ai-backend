package com.jiggycode.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,String> badJson(HttpMessageNotReadableException ex) {
        return Map.of("error", "Malformed JSON: " + ex.getMostSpecificCause().getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,String> badValidation(MethodArgumentNotValidException ex) {
        var f = ex.getBindingResult().getFieldError();
        return Map.of("error", f != null ? f.getField() + ": " + f.getDefaultMessage() : "Validation failed");
    }

    @ExceptionHandler({ org.springframework.security.access.AccessDeniedException.class,
            org.springframework.security.core.userdetails.UsernameNotFoundException.class })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String,String> authErrors(Exception ex) {
        return Map.of("error", ex.getMessage());
    }
}

