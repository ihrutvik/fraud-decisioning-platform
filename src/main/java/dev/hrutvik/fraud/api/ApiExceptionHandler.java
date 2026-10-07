package dev.hrutvik.fraud.api;

import dev.hrutvik.fraud.decision.DecisionNotFoundException;
import dev.hrutvik.fraud.decision.IdempotencyConflictException;
import dev.hrutvik.fraud.rules.RuleSetNotFoundException;
import dev.hrutvik.fraud.review.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IdempotencyConflictException.class)
    ResponseEntity<ApiError> conflict(){return response(HttpStatus.CONFLICT,"IDEMPOTENCY_CONFLICT","Key was already used with a different request");}
    @ExceptionHandler(DecisionNotFoundException.class)
    ResponseEntity<ApiError> notFound(){return response(HttpStatus.NOT_FOUND,"DECISION_NOT_FOUND","Decision was not found");}
    @ExceptionHandler(RuleSetNotFoundException.class)
    ResponseEntity<ApiError> ruleNotFound(){return response(HttpStatus.NOT_FOUND,"RULE_SET_NOT_FOUND","Rule set was not found");}
    @ExceptionHandler(ReviewNotFoundException.class)
    ResponseEntity<ApiError> reviewNotFound(){return response(HttpStatus.NOT_FOUND,"REVIEW_NOT_FOUND","Review case was not found");}
    @ExceptionHandler(ReviewConflictException.class)
    ResponseEntity<ApiError> reviewConflict(ReviewConflictException e){return response(HttpStatus.CONFLICT,"REVIEW_CONFLICT",e.getMessage());}
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> invalidRule(){return response(HttpStatus.BAD_REQUEST,"INVALID_RULE_SET","Rule-set thresholds are invalid");}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalid(){return response(HttpStatus.BAD_REQUEST,"INVALID_REQUEST","Request validation failed");}
    private ResponseEntity<ApiError> response(HttpStatus status,String code,String message){return ResponseEntity.status(status).body(new ApiError(code,message,Instant.now()));}
    record ApiError(String code,String message,Instant timestamp){}
}
