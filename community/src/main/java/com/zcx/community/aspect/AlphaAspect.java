package com.zcx.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Component
// 声明这是一个方面组件
@Aspect
public class AlphaAspect {

    // 定义切点
    // "execution(* com.zcx.community.service.*.*(..))"：描述哪些方法是要处理的目标
    // 第一个*：代表方法的返回值，什么返回值都行，不在乎。service包下所有业务组件的所有方法 的 所有参数 的 所有返回值 都要处理
    // com.zcx.community.service.*：service包下所有的 业务组件
    // com.zcx.community.service.*.*：service包下所有业务组件 的 所有方法
    // com.zcx.community.service.*.*(..)：service包下所有业务组件的所有方法 的 所有参数
    @Pointcut("execution(* com.zcx.community.service.*.*(..))")
    public void pointcut() {
    }

    // 定义通知
    // 在连接点之前要做啥
    // "pointcut()"：以此为切点
    @Before("pointcut()")
    public void before() {
        System.out.println("before");
    }

    // 在连接点之后要做啥
    @After("pointcut()")
    public void after() {
        System.out.println("after");
    }

    // 在连接点返回之后要做啥
    @AfterReturning("pointcut()")
    public void afterReturning() {
        System.out.println("afterReturning");
    }

    // 在连接点抛异常之后要做啥
    @AfterThrowing("pointcut()")
    public void afterThrowing() {
        System.out.println("afterThrowing");
    }

    // 在连接点前+后要做啥
    @Around("pointcut()")
    // 需要有返回值
    // 参数：joinPoint就是连接点
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("around before");
        // proceed()：调用目标组件
        Object object = joinPoint.proceed();
        System.out.println("around after");
        return object;
    }

}
