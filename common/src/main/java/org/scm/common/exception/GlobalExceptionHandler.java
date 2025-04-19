package org.scm.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<?> handleBizException(BizException e) {
        return ResponseEntity.badRequest().body(Map.of(
            "code", e.getCode(),
            "message", e.getMessage()
        ));
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "code", "500",
                "message", "Server Error!!!"
        ));
    }
}
