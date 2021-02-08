package com.orainge.api.receiver.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 接收端接收转发请求的服务接口
 *
 * @author Eason Huang
 * @date 2021/1/23
 */
public interface ReceiverService {
    /**
     * 转发请求并返回请求结果
     */
    void forward(HttpServletRequest request, HttpServletResponse response, Map<String, String> requestParam);
}