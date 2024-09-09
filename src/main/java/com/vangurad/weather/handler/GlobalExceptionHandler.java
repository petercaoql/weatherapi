package com.vangurad.weather.handler;

import com.vangurad.weather.dto.response.ApiErrorResponse;
import com.vangurad.weather.exception.ExternalApiException;
import com.vangurad.weather.exception.InvalidApiKeyException;
import com.vangurad.weather.exception.RateLimitException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidApiKeyException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleInvalidApiKeyException(InvalidApiKeyException ex) {
        log.warn("Invalid API key: {}", ex.getMessage());
        return new ApiErrorResponse(HttpStatus.UNAUTHORIZED.toString(), ex.getMessage());
    }

    @ExceptionHandler(RateLimitException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ApiErrorResponse handleRateLimitException(RateLimitException ex) {
        log.info("Rate limit exceeded: {}", ex.getMessage());
        return new ApiErrorResponse(HttpStatus.TOO_MANY_REQUESTS.toString(), ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleConstraintViolationExceptionException(ConstraintViolationException ex) {
        String validationErrors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", validationErrors);
        return new ApiErrorResponse(HttpStatus.BAD_REQUEST.toString(), validationErrors);
    }

    @ExceptionHandler(ExternalApiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleExternalApiExceptionException(ExternalApiException ex) {
        log.error("External API error: {}", ex.getMessage(), ex);
        return new ApiErrorResponse(HttpStatus.BAD_REQUEST.toString(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleGenericException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.toString(), "Internal server error");
    }
}
