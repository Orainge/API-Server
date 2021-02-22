package com.orainge.api.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义 MVC 转换器<br/>
 * 在保留默认的 MVC 的转换器的基础上，设置 FastJson 为 JSON 数据的转换器 <br/>
 * 可以通过继承该类实现自定义的操作
 */
@Configuration
@ConditionalOnMissingBean(DefaultMvcConfig.class)
public class DefaultMvcConfig implements WebMvcConfigurer {
    /**
     * 添加 Fastjson 转换器
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(fastJsonHttpMessageConverter());
    }

    @Bean
    @ConditionalOnMissingBean(FastJsonHttpMessageConverter.class)
    public FastJsonHttpMessageConverter fastJsonHttpMessageConverter() {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();

        converter.setFastJsonConfig(fastJsonConfig());
        converter.setDefaultCharset(StandardCharsets.UTF_8);

        // 添加反序列化支持的 content-type 类型
        List<MediaType> mediaTypeList = new ArrayList<>();
        mediaTypeList.add(MediaType.APPLICATION_JSON); // application/json
        mediaTypeList.add(MediaType.parseMediaType("application/json;charset=UTF-8"));
        converter.setSupportedMediaTypes(mediaTypeList);
        return converter;
    }

    /**
     * FastJson 的默认配置 Bean
     */
    @Bean
    @ConditionalOnBean(FastJsonHttpMessageConverter.class)
    public FastJsonConfig fastJsonConfig() {
        FastJsonConfig config = new FastJsonConfig();
        config.setSerializerFeatures(
                // 保留map空的字段
                SerializerFeature.WriteMapNullValue,
                // 将String类型的null转成""
                SerializerFeature.WriteNullStringAsEmpty,
                // 将Number类型的null转成0
                SerializerFeature.WriteNullNumberAsZero,
                // 将List类型的null转成[]
                SerializerFeature.WriteNullListAsEmpty,
                // 将Boolean类型的null转成false
                SerializerFeature.WriteNullBooleanAsFalse,
                // 避免循环引用
                SerializerFeature.DisableCircularReferenceDetect
        );
        return config;
    }
}