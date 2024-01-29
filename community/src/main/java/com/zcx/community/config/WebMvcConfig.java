package com.zcx.community.config;

import com.zcx.community.controller.interceptor.AlphaInterceptor;
import com.zcx.community.controller.interceptor.LoginRequiredInterceptor;
import com.zcx.community.controller.interceptor.LoginTicketInterceptor;
import com.zcx.community.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Override
    // 在某一个方法里注册拦截器
    // Spring在调用的时候会把registry传进来，我们利用它去注册alphaInterceptor
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册一个拦截器
        // excludePathPatterns()：哪些路径不需要进行拦截
        // addPathPatterns()：哪些路径需要拦截
        registry.addInterceptor(alphaInterceptor)
                // 访问静态资源，没有业务逻辑的不用拦截
                // 无需拦截static下的所有css，就js，png等files
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.html")
                .addPathPatterns("/register", "/login");

        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.html");

        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.html");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg", "/**/*.html");
    }
}
