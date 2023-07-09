package ru.practicum.shareit.common.handler;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.shareit.common.exception.*;

import javax.validation.ValidationException;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({NotFoundException.class,
            NotOwnerException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final RuntimeException e) {
        return new ErrorResponse(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ValidationException.class,
            NotValidException.class,
            NotAvailableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestExceptions(final RuntimeException e) {
        return new ErrorResponse(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        return new ErrorResponse(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBindException(final BindException e) {
        return new ErrorResponse(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        return new ErrorResponse(e.getClass().getSimpleName(), "Unknown state: " + e.getValue(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleServerErrorException(final Throwable e) {
        return new ErrorResponse(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @AllArgsConstructor
    static class ErrorResponse {
        String errorClass;
        String error;
        HttpStatus responseCodeStatus;

        public String getErrorClass() {
            return errorClass;
        }

        public String getError() {
            return error;
        }

        public HttpStatus getResponseCodeStatus() {
            return responseCodeStatus;
        }
    }
}