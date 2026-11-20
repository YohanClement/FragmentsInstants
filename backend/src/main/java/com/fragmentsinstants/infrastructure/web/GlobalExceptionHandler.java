package com.fragmentsinstants.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.fragmentsinstants.application.BusinessRuleException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler (BusinessRuleException.class)
    public ResponseEntity<BusinessRuleErrorResponse> businessResponse(BusinessRuleException e){
        BusinessRuleErrorResponse businessError = new BusinessRuleErrorResponse("REGLE_METIER_VIOLEE",e.getMessage());
        ResponseEntity<BusinessRuleErrorResponse> response = new ResponseEntity<>(businessError, HttpStatus.UNPROCESSABLE_CONTENT);
        return response;
    }
    
}
