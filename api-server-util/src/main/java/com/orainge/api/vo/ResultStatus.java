package com.orainge.api.vo;

import com.alibaba.fastjson.JSON;
import org.springframework.http.HttpStatus;

/**
 * 常见结果状态枚举类
 *
 * @author Eason Huang
 * @date 2021/1/16
 */
public enum ResultStatus {
    /**
     * 请求成功
     */
    SUCCESS(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase()),
    /**
     * 内部错误
     */
    ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()),
    /**
     * URL 不匹配
     */
    URL_NOT_MATCH(HttpStatus.FORBIDDEN.value(), "Request path is not configured!"),
    /**
     * 未授权的请求方式
     */
    UNAUTHORIZED_REQUEST_METHOD(HttpStatus.FORBIDDEN.value(), "Unauthorized request method!"),
    /**
     * 反向代理错误
     */
    REVERSE_PROXY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Reverse proxy error!"),
    /**
     * 未授权的节点访问
     */
    NOT_AUTHORIZED_NOTE(HttpStatus.FORBIDDEN.value(), "Request from unauthorized node!"),
    /**
     * 密文解密失败，返回[不正确的请求]提示
     */
    INCORRECT_REQUEST(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Incorrect request!");

    private final int code;
    private final String message;

    ResultStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Result toResult() {
        return new Result().setCode(code).setMessage(message);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(new Result().setCode(code).setMessage(message));
    }
}