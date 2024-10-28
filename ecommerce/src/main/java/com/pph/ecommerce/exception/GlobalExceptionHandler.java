package com.pph.ecommerce.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import static org.springframework.http.HttpStatus.*;

import java.time.LocalDateTime;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({AppException.class})
    public ErrorResponse handleAppException(AppException exception, WebRequest request) {
        ErrorResponse response = new ErrorResponse();
        response.setError(NOT_FOUND.getReasonPhrase());
        response.setStatus(NOT_FOUND.value());
        response.setMessage(exception.getMessage());
        response.setPath(request.getDescription(false).replace("uri=", ""));
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}
