package api.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
class ControllerAdvice {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> methodArgumentNotValidHandler(MethodArgumentNotValidException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "Invalid JSON body");
        return response;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> httpMessageNotReadableHandler(HttpMessageNotReadableException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "Invalid JSON body");
        return response;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Map<String, Object> httpRequestMethodNotSupportedHandler(HttpRequestMethodNotSupportedException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "Method not supported");
        return response;
    }
}
