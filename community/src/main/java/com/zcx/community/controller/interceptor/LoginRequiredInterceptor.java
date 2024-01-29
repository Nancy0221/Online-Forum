package com.zcx.community.controller.interceptor;

import com.zcx.community.annotation.LoginRequired;
import com.zcx.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    // 应该在访问该路径前确定该用户是否已登录
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // HandlerHandler：SpringMVC提供的一个类型，如果说拦截到的是一个方法的话，那么这个对象就是HandlerMethod类型
        if (handler instanceof HandlerMethod) {
            // 给handler做转型
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            // 从该方法对象上获取它的注解，我们需要的注解类型就是LoginRequired
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            // 如果取到了，并且该用户未登录，就让它登录，并拒绝他的请求
            if (loginRequired != null && hostHolder.getUser() == null) {
                // 用response重定向，因为这个方法是接口声明的，因此不能像Controller一样直接返回一个模板
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}
