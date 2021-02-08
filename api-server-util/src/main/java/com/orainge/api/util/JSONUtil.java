package com.orainge.api.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * JSON 工具类
 *
 * @author Eason Huang
 * @date 2021/1/3
 */
@Component
@ConditionalOnMissingBean(JSONUtil.class)
public class JSONUtil {
    @Autowired(required = false)
    FastJsonConfig fastJsonConfig;

    @PostConstruct
    public void init() {
        if (fastJsonConfig == null) {
            fastJsonConfig = new FastJsonConfig();
            fastJsonConfig.setSerializerFeatures(
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
        }
    }

    public String toJSONString(Object obj) {
        return JSON.toJSONString(obj, false);
    }

    public <T> T parseObject(String text, Class<T> clazz) {
        return JSONObject.parseObject(text, clazz);
    }
}
