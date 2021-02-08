package com.orainge.api.handler;

import com.orainge.api.vo.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局统一异常处理
 *
 * @author Eason Huang
 * @date 2021/1/21
 */
@RestControllerAdvice
@ConditionalOnMissingBean(GlobalExceptionHandler.class)
public class GlobalExceptionHandler {
    /**
     * 默认异常处理
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        Result result = new Result();
        result.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        result.setMessage(e.getMessage());
        return result;
    }
}
