package com.zcx.community.util;

import com.zcx.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，用于代替session对象
 * 
 * 在分布式的环境下，使用session存在共享数据的问题。通常的解决方案，是将共享数据存入数据库，
 *      所有的应用服务器都去数据库获取共享数据。对于每一次请求，开始时从数据库里取到数据，
 *      然后将其临时存放在本地的内存里，考虑到线程之间的隔离（比如同步机制：锁），所以用threadlocal，
 *      这样在本次请求的过程中，就可以随时获取到这份共享数据了。所以，session的替代方案是数据库，
 *      ThreadLocal只是打了个辅助。（本质是数据共享）
 */
@Component
public class HostHolder {
    // 泛型指代它里面存的什么对象
    // 它的逻辑就是：以线程为key存取值
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        // 隔离的方法：从每个thread中获得一个map对象，然后把数据存入这个对象中。每个线程的map对象都不同
        users.set(user);
    }

    public User getUser() {
        // 先获取对应的thread，然后从当前thread的map中取值
        return users.get();
    }

    // 请求结束的时候再把ThreadLocal中的用户清理掉
    public void clear() {
        // 先获取当前thread的map，再把值清理掉
        users.remove();
    }
}
