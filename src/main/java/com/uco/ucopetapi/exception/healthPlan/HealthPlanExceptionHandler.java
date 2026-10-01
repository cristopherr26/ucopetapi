package com.uco.ucopetapi.exception.healthPlan;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class HealthPlanExceptionHandler {

    @ExceptionHandler(HealthPlanNotFoundException.class)
    public ProblemDetail handleHealthPlanNotFound(HealthPlanNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CoverageNotFoundException.class)
    public ProblemDetail handleCoverageNotFound(CoverageNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ServiceNotFoundException.class)
    public ProblemDetail handleServiceNotFound(ServiceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(DuplicateCoverageException.class)
    public ProblemDetail handleDuplicateCoverage(DuplicateCoverageException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CoverageLimitExceededException.class)
    public ProblemDetail handleCoverageLimitExceeded(CoverageLimitExceededException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ServiceServiceException.class)
    public ProblemDetail handleServiceServiceException(ServiceServiceException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
