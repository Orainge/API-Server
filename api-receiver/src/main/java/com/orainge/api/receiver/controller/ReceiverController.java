package com.orainge.api.receiver.controller;

import com.orainge.api.receiver.service.ReceiverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 接收端接收转发请求的 Controller
 *
 * @author Eason Huang
 * @date 2021/1/2
 */
@Controller
public class ReceiverController {
    @Autowired
    ReceiverService receiverService;

    /**
     * 接受全部的请求
     */
    @RequestMapping("/**")
    public void forward(HttpServletRequest request,
                        HttpServletResponse response,
                        @RequestParam(required = false) Map<String, String> requestParam) {
        receiverService.forward(request, response, requestParam);
    }
}
