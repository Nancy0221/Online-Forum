package com.zcx.community.controller.interceptor;

import com.zcx.community.entity.LoginTicket;
import com.zcx.community.entity.User;
import com.zcx.community.service.UserService;
import com.zcx.community.util.CookieUtil;
import com.zcx.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    // 一开始就要找到这个user，因为我们可能会在后续随时随地都可能会用到这个user的数据，比如在Controller中或处理模板时
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从cookie中获取凭证ticket
        String ticket = CookieUtil.getValue(request, "ticket");
        // 不为null，说明已经登陆了
        if (ticket != null) {
            // 拿到login ticket的对象
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 检查它是否还有效
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                User user = userService.findUserById(loginTicket.getUserId());
                // 在本次请求中持有用户（暂存用户）
                // 在多线程中隔离的存这个对象
                hostHolder.setUser(user);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 得到当前thread持有的user
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            // 把user存进模板
            modelAndView.addObject("loginUser", user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 模板执行结束后，把hostHolder清掉
        hostHolder.clear();
    }
}
