package com.orainge.api.receiver.config;

import com.orainge.api.vo.Credentials;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 接收端的配置文件
 *
 * @author Eason Huang
 * @date 2021/1/2
 */
@Component
@ConfigurationProperties(prefix = "receiver")
@Data
@Slf4j
public class ReceiverConfig {
    private boolean enable = false;
    private Credentials credentials = null;
    private String userAgent = null;
    private List<ReceiverApiList> api = null;

    /**
     * 接收端的配置文件 - API 列表
     */
    @Data
    public static class ReceiverApiList {
        private String name;
        private String forwarder;
        private List<ReceiverApi> urls;
    }

    /**
     * 接收端的配置文件 - API
     */
    @Data
    public static class ReceiverApi {
        private String url;
        private String prefix = "";
        private String host;
        private boolean urlEncode = true;
        private List<String> method;
    }

    /**
     * 配置文件检查
     */
    @PostConstruct
    private void init() {
        if (enable) {
            // 如果当前节点是 receiver，则检查配置文件是否正确

            // 检查凭据是否设置正确
            if (credentials == null) {
                throw new RuntimeException("[接收端] - 配置文件错误: 凭据设置不正确: 缺少凭据");
            } else {
                String id = credentials.getId();
                if (StringUtils.isEmpty(id)) {
                    throw new RuntimeException("[接收端] - 配置文件错误: 凭据设置不正确: 缺少接收端 ID");
                } else if (StringUtils.isEmpty(credentials.getSecurityKey())) {
                    throw new RuntimeException("[接收端] - 配置文件错误: 凭据设置不正确: 接收端 " + id + " 缺少接收端预共享密钥");
                }
            }

            // 检查 User-Agent 是否设置
            if (StringUtils.isEmpty(userAgent)) {
                userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36";
                log.info("[接收端] - 未设置转发端 User-Agent [userAgent]，使用默认值: {}", userAgent);
            } else {
                log.info("[接收端] - 使用自定义的 User-Agent [userAgent]: {}", userAgent);
            }

            // 检查请求路径是否配置
            if (api == null || api.isEmpty()) {
                log.warn("[接收端] - 未设置任何 API，该转发端将不接受任何转发请求");
                enable = false;
            } else {
                api.forEach(item -> {
                    String name = item.getName();
                    if (StringUtils.isEmpty(name)) {
                        log.info("[接收端] - API 配置提示: 有分组未设置分组名称");
                    }

                    String forwarder = item.getForwarder();
                    if (StringUtils.isEmpty(forwarder)) {
                        throw new RuntimeException("[接收端] - API 配置错误: 分组 [" + name + "] 未配置转发端地址 \"forwarder\"");
                    } else {
                        String regex = "^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\\\\\\\/])+$";
                        if (!forwarder.matches(regex)) {
                            // 转发端地址以 http:// 或 https:// 开始
                            throw new RuntimeException("[接收端] - API 配置错误: 分组 [" + name + "] 转发端地址 \"forwarder\" 没有以 http:// 或 https:// 开始");
                        }
                    }

                    List<ReceiverApi> urls = item.getUrls();
                    if (urls == null || urls.isEmpty()) {
                        throw new RuntimeException("[接收端] - API 配置错误: 分组 [" + name + "] 转发地址 \"urls\" 未配置");
                    } else {
                        urls.forEach(urlItem -> {
                            String url = urlItem.getUrl();
                            if (StringUtils.isEmpty(url)) {
                                throw new RuntimeException("[接收端] - API 配置错误: 分组 [" + name + "] 转发地址 \"urls\" 已配置，但子项目 url 未配置");
                            }

                            if (StringUtils.isEmpty(urlItem.getHost())) {
                                throw new RuntimeException("[接收端] - API 配置错误: 分组 [" + name + "] 转发地址 [" + url + "] 未配置主机地址 \"host\" ");
                            }

                            List<String> method = urlItem.getMethod();
                            if (method == null || method.isEmpty()) {
                                throw new RuntimeException("[接收端] - API 配置错误: 分组 [" + name + "] 转发地址 [" + url + "] 未配置可接受的请求方式 \"method\"");
                            }
                        });
                    }
                });

                log.info("[接收端] - 接收端已启动: {}", credentials.getId());
            }
        } else {
            log.warn("[接收端] - 接收端未开启，该接收端将不接受任何转发请求");
        }
    }
}