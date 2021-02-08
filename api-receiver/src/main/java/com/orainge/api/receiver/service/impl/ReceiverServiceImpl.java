package com.orainge.api.receiver.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.orainge.api.receiver.config.ReceiverConfig;
import com.orainge.api.receiver.service.ReceiverService;
import com.orainge.api.receiver.client.ReceiverClient;
import com.orainge.api.util.JSONUtil;
import com.orainge.api.vo.Message;
import com.orainge.api.vo.ResultStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * 接收端接收转发请求的服务实现类
 *
 * @author Eason Huang
 * @date 2021/1/3
 */
@Slf4j
@Service
public class ReceiverServiceImpl implements ReceiverService {
    @Autowired
    JSONUtil jsonUtil;

    @Autowired
    ReceiverConfig receiverConfig;

    @Autowired
    ReceiverClient receiverClient;

    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    /**
     * 转发请求并返回请求结果
     *
     * @return 请求结果
     */
    public void forward(HttpServletRequest request, HttpServletResponse response, Map<String, String> paramsMap) {
        // 首先得判断这个节点是否为接收端
        if (!receiverConfig.isEnable()) {
            // 如果未启用，则返回 404
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        // 请求路径
        String url = request.getRequestURI();

        try {
            String method = request.getMethod();
            ReceiverConfig.ReceiverApiList apiList = null;
            ReceiverConfig.ReceiverApi api = null;

            // 判断当前访问的路径是否被配置了
            for (ReceiverConfig.ReceiverApiList apiListItem : receiverConfig.getApi()) {
                for (ReceiverConfig.ReceiverApi apiItem : apiListItem.getUrls()) {
                    // 直接比较请求的 url 和 配置的 url 是否匹配，不处理前缀
                    if (ANT_PATH_MATCHER.match(apiItem.getUrl(), url)) {
                        // 当前访问路径匹配该配置文件
                        apiList = apiListItem;
                        api = apiItem;
                    }
                }
            }

            // 如果该请求路径没有被配置，则返回错误
            if (apiList == null) {
                handleError(response, ResultStatus.URL_NOT_MATCH);
                return;
            }

            // 检查请求方式是否被许可
            boolean isMethodPermitted = false;
            for (String methodItem : api.getMethod()) {
                if (method.equals(methodItem)) {
                    // 该请求方式在配置文件中
                    isMethodPermitted = true;
                    break;
                }
            }
            if (!isMethodPermitted) {
                handleError(response, ResultStatus.UNAUTHORIZED_REQUEST_METHOD);
                return;
            }

            // 转换 body
            byte[] body;
            try {
                body = StreamUtils.copyToByteArray(request.getInputStream());
            } catch (Exception e) {
                log.error("[接收端] - 请求体转换错误 [{}]: {}", url, e.getMessage());
                handleError(response, ResultStatus.ERROR);
                return;
            }

            // 封装 headers
            LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            Collections.list(request.getHeaderNames()).forEach(headerName -> {
                Enumeration<String> headersEnum = request.getHeaders(headerName);
                if (headersEnum != null) {
                    Collections.list(headersEnum).forEach(headerValue ->
                            headers.add(headerName, headerValue)
                    );
                }
            });

            // 转发请求
            Map<String, Object> resultMap = receiverClient.forward(apiList.getForwarder(), new Message()
                    .setParamsMap(paramsMap)
                    .setRequestURI(url)
                    .setHeaders(headers)
                    .setBody(body)
                    .setMethod(method)
                    .setRouteUrl(api.getHost())
                    .setPrefix(api.getPrefix())
                    .setUrlEncode(api.isUrlEncode())
            );

            if (resultMap == null) {
                // 如果请求结果为空，则返回错误信息
                // 日志已经在上一层输出了
                handleError(response, ResultStatus.ERROR);
            } else {
                try {
                    // 如果请求结果正常，则设置请求信息
                    response.setStatus((Integer) resultMap.get("statusCodeValue"));

                    // 设置 header
                    JSONObject resultHeaders = (JSONObject) resultMap.get("headers");
                    for (Map.Entry<String, Object> resultHeader : resultHeaders.entrySet()) {
                        JSONArray resultHeaderItems = (JSONArray) resultHeader.getValue();
                        resultHeaderItems.forEach(item -> response.addHeader(resultHeader.getKey(), item.toString()));
                    }

                    // 将请求结果写入 body
                    // Tips: 这里的 body 是 byte[] 经过 Base64 编码得到的，因此需要解码
                    // 当且仅当存在 body 时才写入 response
                    String bodyBase64 = (String) resultMap.get("body");
                    if (!StringUtils.isEmpty(bodyBase64)) {
                        writeBody(response, Base64.getDecoder().decode(bodyBase64));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("[接收端] - 解密请求结果错误 [{}]: {}", url, e.getMessage());
                    handleError(response, ResultStatus.ERROR);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[接收端] - 请求错误 [{}]: {}", url, e.getMessage());
            handleError(response, ResultStatus.ERROR);
        }
    }

    /**
     * 将响应类型设置为 JSON
     */
    private void handleError(HttpServletResponse response, ResultStatus resultStatus) {
        response.setHeader("Content-Type", "application/json;charset=utf-8");
        response.setStatus(resultStatus.getCode());
        writeBody(response, resultStatus.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 将响应结果写入 body
     */
    private void writeBody(HttpServletResponse response, byte[] body) {
        try {
            OutputStream stream = response.getOutputStream();
            stream.write(body);
            stream.close();
        } catch (IOException ignored) {
        }
    }
}