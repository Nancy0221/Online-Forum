package com.zcx.community.controller.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AlphaInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AlphaInterceptor.class);

    @Override
    // 在Controller之前执行：返回false -> 取消该请求，不执行Controller；否则执行Controller
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // handler: 是拦截的目标（方法）
        logger.debug("preHandle: " + handler.toString());
        // 可以在这里加塞处理请求，因此这里有request和response传进来
        return true;
    }

    @Override
    // 在Controller之后、模板引擎之前执行
    // 主要逻辑处理完了，需要把所有的东西传递给模板引擎，因此这里有modelAndView传进来
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        logger.debug("postHandle: " + handler.toString());
    }

    @Override
    // 在模板引擎执行完以后执行
    // 如果调用模板时有异常，可以通过ex获取异常信息，并对它做一些处理
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        logger.debug("afterCompletion: " + handler.toString());
    }
}
