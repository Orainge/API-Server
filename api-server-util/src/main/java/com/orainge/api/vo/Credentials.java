package com.orainge.api.vo;

import lombok.Data;

/**
 * 节点之间的认证凭据
 *
 * @author Eason Huang
 * @date 2021/1/6
 */
@Data
public class Credentials {
    /**
     * 接收端 ID
     */
    private String id;

    /**
     * 预共享密钥
     */
    private String securityKey;
}
