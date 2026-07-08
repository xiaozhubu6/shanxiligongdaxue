package com.java1234.config;

import com.java1234.entity.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public R handleRuntimeException(RuntimeException e) {
        return R.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R handleException(Exception e) {
        return R.error("系统错误，请联系管理员");
    }
} 