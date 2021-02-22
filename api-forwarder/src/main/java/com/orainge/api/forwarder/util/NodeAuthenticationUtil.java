package com.orainge.api.forwarder.util;

import com.orainge.api.util.encryption.TOTPUtil;
import com.orainge.api.vo.AuthCode;
import com.orainge.api.vo.Credentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 授权工具类<br/>
 * 目的是为了防止重放攻击
 *
 * @author Eason Huang
 * @date 2021/1/4
 */
@Component
@Slf4j
public class NodeAuthenticationUtil {
    @Autowired
    TOTPUtil totpUtil;

    /**
     * 验证凭据是否合法
     *
     * @param headerMap 请求头
     * @return 如果请求合法，则返回对应的 ReceiverInfo，否则会返回 null
     */
    public Credentials verify(MultiValueMap<String, String> headerMap, List<Credentials> receiverInfoList) {
        String nodeId = headerMap.getFirst(AuthCode.NODE_ID_HEADER_NAME);
        String nodeKey = headerMap.getFirst(AuthCode.NODE_KEY_HEADER_NAME);
        if (StringUtils.isEmpty(nodeId) || StringUtils.isEmpty(nodeKey)) {
            // 没有节点 ID 或 动态验证码
            log.warn("[转发端] - 节点验证错误: 没有节点 ID 或动态验证码");
            return null;
        }

        // 可允许转发的转发端 ID 列表为空
        if (receiverInfoList == null || receiverInfoList.isEmpty()) {
            log.warn("[转发端] - 节点验证错误[{}]: 可允许转发的转发端 ID 列表为空", nodeId);
            return null;
        }

        // 查询 node ID
        Credentials credentials = null;
        for (Credentials info : receiverInfoList) {
            if (nodeId.equals(info.getId())) {
                credentials = info;
                break;
            }
        }

        if (credentials == null) {
            log.warn("[转发端] - 节点验证错误[{}]: 查询请求的节点 ID 在不在白名单中", nodeId);
            return null;
        }

        // 校验请求的动态验证码是否符合要求
        boolean verifyResult = totpUtil.verify(credentials.getId(), credentials.getSecurityKey(), nodeKey);
        if (!verifyResult) {
            // 动态验证码不正确
            log.warn("[转发端] - 节点验证错误[{}]: 动态验证码不正确", nodeId);
            return null;
        }

        // 返回查询到的结果
        return credentials;
    }

    /**
     * 获取加密密钥
     *
     * @param credentials 转发端的信息
     * @return 加密密钥
     */
    public String getEncryptionKey(Credentials credentials) {
        return credentials.getSecurityKey();
    }
}