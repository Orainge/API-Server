package com.orainge.api.vo;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 返回结果类
 *
 * @author Eason Huang
 * @date 2021/1/11
 */
@Data
@Accessors(chain = true)
public class Result {
    private Integer code;
    private String message;
    private String data;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}