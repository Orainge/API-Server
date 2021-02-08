package com.orainge.api.forwarder.util;

import com.orainge.api.vo.AuthCode;
import com.orainge.api.vo.Credentials;
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
public class NodeAuthenticationUtil {
    /**
     * 验证凭据是否合法
     *
     * @param headerMap 请求头
     * @return 如果请求合法，则返回对应的 ReceiverInfo，否则会返回 null
     */
    public Credentials verify(MultiValueMap<String, String> headerMap, List<Credentials> receiverInfoList) {
        String nodeId = headerMap.getFirst(AuthCode.NODE_ID_HEADER_NAME);
        if (StringUtils.isEmpty(nodeId)) {
            // 没有节点 ID
            return null;
        }

        // 查询请求的 node ID 在不在白名单中
        if (receiverInfoList == null || receiverInfoList.isEmpty()) {
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