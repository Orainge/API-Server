package com.orainge.api.receiver;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import java.util.Objects;

/**
 * SpringBoot 启动类
 *
 * @author Eason Huang
 * @date 2021/1/8
 */
@SpringBootApplication
public class ApiReceiverApplication extends SpringBootServletInitializer {
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource[]{
                new ClassPathResource("receiver-config.yml")
        });
        pspc.setProperties(Objects.requireNonNull(yaml.getObject()));
        return pspc;
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ApiReceiverApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ApiReceiverApplication.class, args);
    }
}