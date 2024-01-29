package com.zcx.community.controller.advice;

import com.zcx.community.util.CommunityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

// Controller的全局配置类
// annotations = Controller.class: 只扫描带有Controller注解的bean
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    // 表示这个方法是处理所有异常的方法
    // {Exception.class}：是所有异常的父类，代表所有异常都放到此方法来处理
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常：" + e.getMessage());
        // 每个stackTraceElement记录了一条异常
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            logger.error(stackTraceElement.toString());
        }
        // 判断是普通请求还是异步请求
        // 普通请求：可重定向到错误页面
        // 异步请求：需要返回JSON，而不是一个网页
        String xRequestedWith = request.getHeader("x-requested-with");
        // XMLHttpRequest：代表是异步请求，因为它是以XML的形式来访问的，因此它希望我们返回的是xml，只有异步请求才希望我们返回xml给它
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            // application/plain：向浏览器返回的是普通的字符串，它可以使json格式。浏览器得到它后需要人为的把它转换为json
            response.setContentType("application/plain;charset=utf-8");
            // 向浏览器输出json
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtils.getJSONString(-1, "服务器异常"));
        } else {
            // 否则，重定向到error页面
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }

}
