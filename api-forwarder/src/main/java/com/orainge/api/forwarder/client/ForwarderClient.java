package com.orainge.api.forwarder.client;

import com.orainge.api.client.RestTemplateClient;
import com.orainge.api.vo.Message;
import com.orainge.api.vo.ResultStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 转发端客户端<br/>
 * 使用反向代理的原理实现
 *
 * @author Eason Huang
 * @date 2021/1/1
 */
@Component
@Slf4j
public class ForwarderClient {
    @Autowired
    RestTemplateClient restTemplateClient;

    /**
     * <p>转发请求</p>
     * <p>
     * e.g 1:<br/>
     * url = /OPENAPI/**
     * routeUrl = https://abc.com/test<br/>
     * prefix = ""<br/>
     * 那么请求了 /OPENAPI/abc?a=1 则会转发为 https://abc.com/test/OPENAPI/abc?a=1
     * </p>
     *
     * <p>
     * e.g 2:<br/>
     * url = /OPENAPI/**
     * routeUrl = https://abc.com/test<br/>
     * prefix = /OPENAPI<br/>
     * 那么请求了 /OPENAPI/abc?a=1 则会转发为 https://abc.com/test/abc?a=1<br/>
     * 即请求地址中的 "/OPENAPI" 被替换掉了
     * </p>
     *
     * <p>
     * e.g 3:<br/>
     * url = /OPENAPI/**
     * routeUrl = https://abc.com/test<br/>
     * prefix = /OPEN<br/>
     * 那么请求了 /OPENAPI/abc?a=1 则会转发为 https://abc.com/test/API/abc?a=1
     * 即请求地址中的 "/OPEN" 被替换掉了
     * </p>
     *
     * @param paramsMap   查询参数，跟在 URL 后面的参数
     * @param requestURI  请求的相对路径
     * @param headers     请求头
     * @param body        请求体
     * @param method      请求方式
     * @param routeUrl    转发 URL
     * @param prefix      转发 URL 前缀
     * @param isUrlEncode 是否需要 URLEncode
     * @param clazz       请求结果 body 类型
     * @return 请求结果
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> ResponseEntity<T> redirect(Map<String, String> paramsMap,
                                          String requestURI,
                                          MultiValueMap<String, String> headers,
                                          byte[] body,
                                          String method,
                                          String routeUrl,
                                          String prefix,
                                          boolean isUrlEncode,
                                          Class<T> clazz) {
        try {
            if (StringUtils.isEmpty(routeUrl)) {
                throw new RuntimeException("转发 URL 为空");
            }

            // 创建 URL
            if (prefix == null) {
                prefix = "";
            }

            // 请求并返回结果
            String processUrl = requestURI.replaceAll(prefix, "");
            if (processUrl.indexOf("/") != 0) {
                processUrl = "/" + processUrl;
            }

            return restTemplateClient.exchange(
                    routeUrl + processUrl,
                    paramsMap,
                    body,
                    HttpMethod.resolve(method),
                    headers,
                    isUrlEncode,
                    clazz
            );
        } catch (Exception e) {
            log.error("反向代理客户端 - 请求错误: {}", e.getMessage());
            return new ResponseEntity(ResultStatus.REVERSE_PROXY_ERROR.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 转发请求
     *
     * @param message 解密后的转发请求体
     * @return 转发结果
     */
    public ResponseEntity<byte[]> forward(Message message) {
        return redirect(
                message.getParamsMap(),
                message.getRequestURI(),
                message.getHeaders(),
                message.getBody(),
                message.getMethod(),
                message.getRouteUrl(),
                message.getPrefix(),
                message.isUrlEncode(),
                byte[].class
        );
    }
}