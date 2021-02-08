package com.orainge.api.forwarder.service;

import com.orainge.api.vo.Result;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletResponse;

/**
 * 节点之间通信 API 的服务接口
 *
 * @author Eason Huang
 * @date 2021/1/2
 */
public interface ForwarderService {
    /**
     * 接收来自接收端的转发请求
     */
    Result exchange(HttpServletResponse response, MultiValueMap<String, String> header, String body);
}