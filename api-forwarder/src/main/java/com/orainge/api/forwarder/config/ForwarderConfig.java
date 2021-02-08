package com.orainge.api.forwarder.config;

import com.orainge.api.vo.Credentials;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;

/**
 * 转发端的配置文件
 *
 * @author Eason Huang
 * @date 2021/1/2
 */
@Component
@ConfigurationProperties(prefix = "forwarder")
@Data
@Slf4j
public class ForwarderConfig {
    private boolean enable = false;
    private List<Credentials> whitelist = new LinkedList<>();

    /**
     * 项目初始化时配置文件检查
     */
    @PostConstruct
    private void init() {
        if (enable) {
            // 如果当前节点是 forwarder，则检查配置文件是否正确

            // 检查凭据是否设置正确
            if (whitelist == null || whitelist.isEmpty()) {
                throw new RuntimeException("[转发端] - 配置文件错误: 凭据设置不正确: 缺少凭据");
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append("[转发端] - 允许转发的接收端: ");

                int i = 1;
                for (Credentials credentials : whitelist) {
                    String id = credentials.getId();
                    if (StringUtils.isEmpty(id)) {
                        throw new RuntimeException("[转发端] - 配置文件错误: 凭据设置不正确: 缺少接收端 ID");
                    } else if (StringUtils.isEmpty(credentials.getSecurityKey())) {
                        throw new RuntimeException("[转发端] - 配置文件错误: 凭据设置不正确: 接收端 " + id + " 缺少预共享密钥");
                    }

                    builder.append(id);
                    if (i != whitelist.size()) {
                        builder.append(", ");
                        i++;
                    }
                }

                log.info(builder.toString());
            }
        } else {
            log.warn("[转发端] - 转发端未开启，该转发端将不接受任何转发请求");
        }
    }
}