package com.shopmall.common.advice;

import com.shopmall.common.enums.ExceptionEnum;
import com.shopmall.common.exception.SmException;
import com.shopmall.common.vo.ExceptionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CommonExceptionHandler {
    @ExceptionHandler(SmException.class)
    public ResponseEntity<ExceptionResult> handleException(SmException e) {
        return ResponseEntity.status(e.getExceptionEnum().getCode()).body(new ExceptionResult(e.getExceptionEnum()));
    }
}
