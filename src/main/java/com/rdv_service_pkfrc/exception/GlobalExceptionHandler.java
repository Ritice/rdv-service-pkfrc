package com.rdv_service_pkfrc.exception;

import com.rdv_service_pkfrc.response.Response;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 NOT FOUND
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response<Void> handleNotFound(
            ResourceNotFoundException ex
    ) {

        return new Response<>(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                null,
                null
        );
    }


    // 409 CONFLICT
    @ExceptionHandler(ConflitRdvException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Response<Void> handleConflit(
            ConflitRdvException ex
    ) {

        return new Response<>(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                null,
                null
        );
    }

    @ExceptionHandler(DuplicateReferenceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Response<Void> handleDuplicate(
            DuplicateReferenceException ex
    ) {

        return new Response<>(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                null,
                null
        );
    }


    // 422 BUSINESS EXCEPTION
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Response<Void> handleBusiness(
            BusinessException ex
    ) {

        return new Response<>(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ex.getMessage(),
                null,
                null
        );
    }

    // 400 VALIDATION
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<Void> handleValidation(
            MethodArgumentNotValidException ex
    ) {

        Map<String, Serializable> errors =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .collect(Collectors.toMap(
                                FieldError::getField,
                                fieldError ->
                                        fieldError.getDefaultMessage() != null
                                                ? fieldError.getDefaultMessage()
                                                : "Valeur invalide",
                                (e1, e2) -> e1,
                                HashMap::new
                        ));

        return new Response<>(
                HttpStatus.BAD_REQUEST.value(),
                "Données invalides",
                null,
                errors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<Void> handleConstraintViolation(
            ConstraintViolationException ex
    ) {

        Map<String, Serializable> errors =
                ex.getConstraintViolations()
                        .stream()
                        .collect(Collectors.toMap(
                                violation -> violation.getPropertyPath().toString(),
                                violation -> violation.getMessage(),
                                (e1, e2) -> e1,
                                HashMap::new
                        ));

        return new Response<>(
                HttpStatus.BAD_REQUEST.value(),
                "Contrainte de validation",
                null,
                errors
        );
    }


    // 409 OPTIMISTIC LOCK
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Response<Void> handleOptimisticLock(
            ObjectOptimisticLockingFailureException ex
    ) {

        return new Response<>(
                HttpStatus.CONFLICT.value(),
                "Une modification concurrente a été détectée.",
                null,
                null
        );
    }


    // 409 DATABASE INTEGRITY
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Response<Void> handleIntegrity(
            DataIntegrityViolationException ex
    ) {

        return new Response<>(
                HttpStatus.CONFLICT.value(),
                "Une contrainte d'intégrité a été violée.",
                null,
                null
        );
    }


    // 500 INTERNAL SERVER ERROR
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response<Void> handleGeneric(
            Exception ex
    ) {

        return new Response<>(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Une erreur interne est survenue.",
                null,
                null
        );
    }
}
