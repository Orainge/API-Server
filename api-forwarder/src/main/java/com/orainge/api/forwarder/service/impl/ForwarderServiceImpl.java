package com.orainge.api.forwarder.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.orainge.api.forwarder.config.ForwarderConfig;
import com.orainge.api.forwarder.client.ForwarderClient;
import com.orainge.api.forwarder.service.ForwarderService;
import com.orainge.api.forwarder.util.NodeAuthenticationUtil;
import com.orainge.api.util.encryption.EncryptionUtil;
import com.orainge.api.vo.Credentials;
import com.orainge.api.vo.Message;
import com.orainge.api.vo.Result;
import com.orainge.api.vo.ResultStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletResponse;

/**
 * 节点之间通信 API 的服务实现类
 *
 * @author Eason Huang
 * @date 2021/1/3
 */
@Service
@Slf4j
public class ForwarderServiceImpl implements ForwarderService {
    @Autowired
    NodeAuthenticationUtil nodeAuthenticationUtil;

    @Autowired
    EncryptionUtil encryptionUtil;

    @Autowired
    ForwarderClient forwarderClient;

    @Autowired
    ForwarderConfig forwarderConfig;

    /**
     * 接收来自接收端的转发请求
     */
    public Result exchange(HttpServletResponse response, MultiValueMap<String, String> header, String body) {
        // 首先得判断这个节点是否为转发端
        if (!forwarderConfig.isEnable()) {
            // 如果未启用，则返回 404
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // 判断请求是否为授权的请求
        Credentials credentials = nodeAuthenticationUtil.verify(header, forwarderConfig.getWhitelist());
        if (credentials == null) {
            // 无节点信息或节点认证错误
            return ResultStatus.NOT_AUTHORIZED_NOTE.toResult();
        }

        // 准备密钥
        String encryptKey = nodeAuthenticationUtil.getEncryptionKey(credentials);

        // 解密请求体
        body = encryptionUtil.decrypt(body, encryptKey);
        if (body == null) {
            return ResultStatus.INCORRECT_REQUEST.toResult();
        }

        Message message;
        try {
            message = JSONObject.parseObject(body, Message.class);
        } catch (Exception e) {
            log.error("[转发端] - 请求错误: 转发请求体不合法: {}", e.getMessage());
            return ResultStatus.INCORRECT_REQUEST.toResult();
        }

        // 转发请求
        log.info("[转发端] - 收到转发请求[{}]: {}", credentials.getId(), JSON.toJSONString(message));
        ResponseEntity<byte[]> forwardResult = forwarderClient.forward(message);
        if (forwardResult == null) {
            log.error("[转发端] - 请求错误: 转发结果为空");
            return ResultStatus.REVERSE_PROXY_ERROR.toResult();
        }

        // 将转发结果加密后返回
        // Tips: 这里 byte[] 类型的 body 已经经过 Base64 编码了，因此在转发端只需要解码就可以还原成 byte[]
        String result = encryptionUtil.encrypt(JSON.toJSONString(forwardResult), encryptKey);

        if (result == null) {
            log.error("[转发端] - 请求错误: 转发结果加密失败");
            return ResultStatus.REVERSE_PROXY_ERROR.toResult();
        } else {
            return ResultStatus.SUCCESS.toResult().setData(result);
        }
    }
}