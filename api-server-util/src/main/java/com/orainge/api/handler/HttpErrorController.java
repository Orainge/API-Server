package com.orainge.api.handler;

import com.orainge.api.vo.Result;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * 复写 /error 的处理方式
 *
 * @author Eason Huang
 * @date 2021/1/21
 */
@RestController
@ConditionalOnMissingBean(HttpErrorController.class)
@AutoConfigureBefore(ErrorMvcAutoConfiguration.class)
public class HttpErrorController implements ErrorController {
    private final static String ERROR_PATH = "/error";

    @ResponseBody
    @RequestMapping(path = ERROR_PATH)
    public Result error(HttpServletResponse response) {
        // 如果是 404，直接返回空内容
        if (HttpStatus.NOT_FOUND.value() == response.getStatus()) {
            return null;
        }

        Result result = new Result();
        result.setCode(response.getStatus());
        result.setMessage(HttpStatus.valueOf(response.getStatus()).getReasonPhrase());
        return result;
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }
}