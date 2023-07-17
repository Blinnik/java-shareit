package ru.practicum.shareit.common.handler;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.shareit.common.exception.NotAvailableException;
import ru.practicum.shareit.common.exception.NotFoundException;
import ru.practicum.shareit.common.exception.NotOwnerException;
import ru.practicum.shareit.common.exception.NotValidException;

import javax.validation.ValidationException;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler({NotFoundException.class,
            NotOwnerException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final RuntimeException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler({ValidationException.class,
            NotValidException.class,
            NotAvailableException.class,
            BindException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestException(final Exception e) {
        if (e.getClass().equals(MethodArgumentTypeMismatchException.class)) {
            MethodArgumentTypeMismatchException typeMismatchException = (MethodArgumentTypeMismatchException) e;
            return new ErrorResponse("Unknown state: " + typeMismatchException.getValue());
        }

        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleServerErrorException(final Throwable e) {
        return new ErrorResponse(e.getMessage());
    }

    @AllArgsConstructor
    static class ErrorResponse {
        String error;

        public String getError() {
            return error;
        }
    }
}