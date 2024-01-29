package com.zcx.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 声明这个注解要写在方法上
@Target(ElementType.METHOD)
// 声明这个注解有效的时长是：程序运行的时候
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {
    // 什么都不用做，打上这个标签就行
}
