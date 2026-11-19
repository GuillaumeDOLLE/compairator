package com.will.compairator.configuration;

import com.will.compairator.ai.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleConcurrentUpdate(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "L’évaluation a été modifiée simultanément. Réessayez."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        // Do not log or echo rejected values: registration includes a password.
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Les champs de la demande sont invalides.");
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("Request body is malformed or contains an unsupported value");
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Le JSON est invalide ou contient une valeur non prise en charge.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(AiProviderCallException.class)
    public ResponseEntity<ProblemDetail> handleAiProviderCall(AiProviderCallException ex) {
        log.error("AI provider call failed", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(pd);
    }

    @ExceptionHandler(AiProviderInvalidResponseException.class)
    public ResponseEntity<ProblemDetail> handleAiProviderInvalidResponse(AiProviderInvalidResponseException ex) {
        log.error("The AI provider returned an invalid response", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "The AI provider returned an invalid response");
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(pd);
    }

    @ExceptionHandler(InvalidProviderConfigurationException.class)
    public ResponseEntity<ProblemDetail> handleInvalidProviderConfiguration(InvalidProviderConfigurationException ex) {
        log.error("The provider configuration is either invalid or missing", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }

    @ExceptionHandler(InvalidPropertyFormatException.class)
    public ResponseEntity<ProblemDetail> handleInvalidPropertyFormat(InvalidPropertyFormatException ex) {
        log.error("The properties file has a format issue", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }

    @ExceptionHandler(MandatoryApplicationPropertyFileNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleMandatoryApplicationPropertyFileNotFound(MandatoryApplicationPropertyFileNotFoundException ex) {
        log.error("The property file has not been found", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }

}
