package ru.practicum.shareit.handler;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotOwnerException;

import javax.validation.ValidationException;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final RuntimeException e) {
        return new ErrorResponse(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleAlreadyExistException(final RuntimeException e) {
        return new ErrorResponse(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler({ValidationException.class,
            MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final RuntimeException e) {
        return new ErrorResponse(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotOwnerException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleNotOwnerException(final RuntimeException e) {
        return new ErrorResponse(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(final Throwable e) {
        return new ErrorResponse(e.getClass().getSimpleName(), e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @AllArgsConstructor
    static class ErrorResponse {
        String error;
        String errorMessage;
        HttpStatus responseCodeStatus;

        public String getError() {
            return error;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public HttpStatus getResponseCodeStatus() {
            return responseCodeStatus;
        }
    }
}