package com.orainge.api.forwarder.controller;

import com.orainge.api.forwarder.service.ForwarderService;
import com.orainge.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * 节点之间通信 API 的 Controller
 *
 * @author Eason Huang
 * @date 2021/1/2
 */
@RestController
public class ForwarderController {
    @Autowired
    ForwarderService forwarderService;

    /**
     * 接收来自接收端的转发请求
     */
    @PostMapping("/exchange")
    public @ResponseBody
    Result exchange(HttpServletResponse response,
                    @RequestHeader(required = false) MultiValueMap<String, String> header,
                    @RequestBody(required = false) String body) {
        if (StringUtils.isEmpty(body)) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }
        return forwarderService.exchange(response, header, body);
    }

    /**
     * 只允许 POST 方式进行数据交换，其它方式统一返回成 404
     */
    @RequestMapping("/exchange")
    public void exchange(HttpServletResponse response) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
    }
}
