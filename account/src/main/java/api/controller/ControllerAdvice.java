package api.controller;

import api.exception.AccountAlreadyCreatedException;
import api.exception.AlreadyProcessedException;
import api.exception.NotEnoughBalanceException;
import api.exception.NotFoundException;
import api.exception.UserHasNoAccountException;
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
public class ControllerAdvice {

    @ExceptionHandler(AccountAlreadyCreatedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> accountAlreadyCreatedExceptionHandler() {
        return createErrorResponse("Account already created");
    }

    @ExceptionHandler(NotEnoughBalanceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> notEnoughBalanceExceptionHandler() {
        return createErrorResponse("Not enough balance");
    }

    @ExceptionHandler(UserHasNoAccountException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> userHasNoAccountExceptionHandler() {
        return createErrorResponse("User has no account");
    }

    @ExceptionHandler(AlreadyProcessedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> alreadyProcessedExceptionHandler() {
        return createErrorResponse("Already processed");
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> notFoundExceptionHandler() {
        return createErrorResponse("Not found");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Map<String, Object> httpRequestMethodNotSupportedHandler() {
        return createErrorResponse("Method not allowed");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> methodArgumentNotValidHandler(MethodArgumentNotValidException exception) {
        return createErrorResponse("Invalid request");
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> httpMessageNotReadableHandler(HttpMessageNotReadableException exception) {
        return createErrorResponse("Invalid request");
    }

    private static Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", errorMessage);
        return response;
    }
}
