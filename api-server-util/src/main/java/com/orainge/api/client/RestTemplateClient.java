package com.orainge.api.client;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 请求客户端
 *
 * @author Eason Huang
 * @date 2021/1/22
 */
@Slf4j
@Component
@ConditionalOnMissingBean(RestTemplateClient.class)
public class RestTemplateClient {
    @Autowired
    @Qualifier("defaultRestTemplate")
    private RestTemplate restTemplate;

    @Value("${http-client.debug: false}")
    private boolean isDebug;

    /**
     * 执行请求
     *
     * @param url         请求地址
     * @param paramsMap   请求参数
     * @param body        请求体
     * @param method      请求方式（GET/POST/...)
     * @param headerMap   请求头 Map
     * @param isUrlEncode URL是否需要 URLEncode
     * @return 请求结果
     */
    public String exchange(String url, Map<String, String> paramsMap, Object body, HttpMethod method, MultiValueMap<String, String> headerMap, boolean isUrlEncode) {
        ResponseEntity<String> exchangeResponseEntity = exchange(url, paramsMap, body, method, headerMap, isUrlEncode, String.class);
        if (exchangeResponseEntity != null) {
            return exchangeResponseEntity.getBody();
        } else {
            return null;
        }
    }

    /**
     * 执行请求
     *
     * @param url         请求地址
     * @param paramsMap   请求参数
     * @param body        请求体
     * @param method      请求方式（GET/POST/...)
     * @param headerMap   请求头 Map
     * @param isUrlEncode URL是否需要 URLEncode
     * @param clazz       结果类型
     * @return 请求结果
     */
    public <T> ResponseEntity<T> exchange(String url, Map<String, String> paramsMap, Object body, HttpMethod method, MultiValueMap<String, String> headerMap, boolean isUrlEncode, Class<T> clazz) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            String exchangeUrl = url;
            String showUrl = url;
            HttpEntity<String> request;
            Map<String, Object> paramsKv = new HashMap<>();
//            headers.setContentType(MediaType.APPLICATION_JSON);

            // 根据情况添加 header
            headers.addAll(headerMap);

            // 根据情况添加 body
            if (body == null) {
                request = new HttpEntity<>(headers);
            } else {
                request = new HttpEntity<>(JSON.toJSONString(body), headers);
            }

            // 根据情况添加 paramsKv
            if (paramsMap != null && !paramsMap.isEmpty()) {
                // 判断是否需要 URLEncode
                if (isUrlEncode) {
                    // 需要 URLEncode，直接使用默认方法
                    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                    paramsMap.forEach(map::add);
                    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
                    builder.queryParams(map);
                    exchangeUrl = builder.toUriString();
                    if (isDebug) {
                        showUrl = exchangeUrl;
                    }
                } else {
                    // 不需要URLEncode，手动追加参数
                    StringBuilder appendParamsKv = new StringBuilder();
                    appendParamsKv.append("?");

                    int i = 1;
                    paramsMap.forEach((key, value) -> {
                        String paramKey = UUID.randomUUID().toString().substring(0, 4);
                        appendParamsKv.append(key);
                        appendParamsKv.append("={");
                        appendParamsKv.append(paramKey);
                        appendParamsKv.append("}");
                        paramsKv.put(paramKey, value);
                        if (i != paramsMap.size()) {
                            appendParamsKv.append("&");
                        }
                    });

                    exchangeUrl = url + appendParamsKv.toString();

                    if (isDebug) {
                        showUrl = exchangeUrl;
                        for (Map.Entry<String, Object> entry : paramsKv.entrySet()) {
                            showUrl = showUrl.replaceAll("\\{[" + entry.getKey() + "^}]*\\}", entry.getValue().toString());
                        }
                    }
                }

            }

            if (isDebug) {
                log.debug("发起请求: {} {}", method.toString(), showUrl);
            }

            ResponseEntity<T> responseEntity = restTemplate.exchange(exchangeUrl, method, request, clazz, paramsKv);

            if (isDebug) {
                log.debug("{} 请求结果: {}", method.toString(), responseEntity.getBody());
            }

            // 返回请求结果
            return responseEntity;
        } catch (Exception e) {
            log.error("{} 请求出错: url: {}, 错误原因: {}", method.toString(), url, e.getMessage());
            return null;
        }
    }

    /**
     * 自定义 urldecode 编码
     *
     * @param params 参数
     * @return 编码后的参数
     */
    private String urlDecode(String params) {
        params = params.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
        params = params.replaceAll("\\+", "%2B");
        return params;
    }
}
