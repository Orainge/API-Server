package com.orainge.api.receiver.client;

import com.orainge.api.receiver.config.ReceiverConfig;
import com.orainge.api.receiver.util.NodeAuthenticationUtil;
import com.orainge.api.util.JSONUtil;
import com.orainge.api.client.RestTemplateClient;
import com.orainge.api.util.encryption.EncryptionUtil;
import com.orainge.api.vo.Message;
import com.orainge.api.vo.Result;
import com.orainge.api.vo.ResultStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * 转发端客户端<br/>
 * 负责与接收端通信
 *
 * @author Eason Huang
 * @date 2021/1/2
 */
@Slf4j
@Component
public class ReceiverClient {
    @Autowired
    EncryptionUtil encryptionUtil;

    @Autowired
    JSONUtil jsonUtil;

    @Autowired
    NodeAuthenticationUtil nodeAuthenticationUtil;

    @Autowired
    RestTemplateClient restTemplateClient;

    @Autowired
    ReceiverConfig receiverConfig;

    /**
     * 将请求转交给 Forwarder 并获取请求结果
     *
     * @param forwarder 需要提交的转发节点
     * @param message   交换的信息体
     * @return 请求结果
     */
    @SuppressWarnings({"unchecked"})
    public Map<String, Object> forward(String forwarder, Message message) {
        try {
            // 日志输出
            log.info("[接收端] - 收到转发请求: {}", jsonUtil.toJSONString(message));

            // 获取数据加密的密钥
            String encryptionKey = nodeAuthenticationUtil.getEncryptionKey(receiverConfig);

            // 执行加密操作
            String exchangeBody = encryptionUtil.encrypt(jsonUtil.toJSONString(message), encryptionKey);

            if (exchangeBody == null) {
                throw new RuntimeException("请求信息加密失败 [" + message.getRequestURI() + "]");
            }

            // 创建请求头
            MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();

            // 修改客户端 User-Agent
            headerMap.add("User-Agent", receiverConfig.getUserAgent());

            // 添加验证凭据
            nodeAuthenticationUtil.addCredentials(headerMap, receiverConfig);

            // 连接节点进行转发
            String res = restTemplateClient.exchange(
                    forwarder + "/exchange",
                    null,
                    exchangeBody,
                    HttpMethod.POST,
                    headerMap,
                    true
            );

            if (res == null) {
                throw new RuntimeException("请求错误 [" + message.getRequestURI() + "]: 请求结果为空");
            }

            // 获取请求结果
            Result result = jsonUtil.parseObject(res, Result.class);

            if (!result.getCode().equals(ResultStatus.SUCCESS.getCode())) {
                throw new RuntimeException("请求错误 [" + message.getRequestURI() + "]: " + result.getMessage());
            }

            // 执行解密操作并返回结果
            try {
                return jsonUtil.parseObject(encryptionUtil.decrypt(result.getData(), encryptionKey), Map.class);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("请求错误 [" + message.getRequestURI() + "]: 解密失败: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("[接收端] - 转发错误 [{}]: {}", jsonUtil.toJSONString(forwarder), e.getMessage());
            return null;
        }
    }
}