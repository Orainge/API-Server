package com.orainge.api.receiver.util;

import com.orainge.api.receiver.config.ReceiverConfig;
import com.orainge.api.vo.AuthCode;
import com.orainge.api.util.encryption.TOTPUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

/**
 * 授权工具类<br/>
 * 目的是为了防止重放攻击
 *
 * @author Eason Huang
 * @date 2021/1/24
 */
@Component
public class NodeAuthenticationUtil {
    @Autowired
    TOTPUtil totpUtil;

    /**
     * 创建凭据
     *
     * @param headerMap      请求头
     * @param receiverConfig 接收端配置文件
     */
    public void addCredentials(MultiValueMap<String, String> headerMap, ReceiverConfig receiverConfig) {
        // 将节点 ID 添加到请求头中
        String receiverId = receiverConfig.getCredentials().getId();
        String secretKey = receiverConfig.getCredentials().getSecurityKey();
        headerMap.add(AuthCode.NODE_ID_HEADER_NAME, receiverId);
        headerMap.add(AuthCode.NODE_KEY_HEADER_NAME, totpUtil.generate(receiverId, secretKey));
    }

    /**
     * 获取加密密钥
     *
     * @param receiverConfig 接收端配置文件
     * @return 加密密钥
     */
    public String getEncryptionKey(ReceiverConfig receiverConfig) {
        return receiverConfig.getCredentials().getSecurityKey();
    }
}