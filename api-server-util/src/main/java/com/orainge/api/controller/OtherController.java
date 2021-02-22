package com.orainge.api.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * 其它路径的 Controller<br/>
 * 可以通过继承该类实现自定义操作
 *
 * @author Eason Huang
 * @date 2021/1/23
 */
@RestController
@ConditionalOnMissingBean({OtherController.class})
public class OtherController {
    /**
     * 屏蔽 favicon.ico
     */
    @RequestMapping("/favicon.ico")
    public void favicon(HttpServletResponse response) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
    }
}