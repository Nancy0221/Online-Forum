package com.zcx.community.service;

import com.zcx.community.dao.AlphaDao;
import com.zcx.community.dao.DiscussPostMapper;
import com.zcx.community.dao.UserMapper;
import com.zcx.community.entity.DiscussPost;
import com.zcx.community.entity.User;
import com.zcx.community.util.CommunityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
//@Scope("prototype")
public class AlphaService {

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    @Qualifier("anotherAlphaDaoImpl")
    private AlphaDao alphaDao;

    public AlphaService() {
        System.out.println("实例化AlphaService");
    }

    @PostConstruct
    public void init() {
        System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("销毁AlphaService");
    }

    public String find() {
        return alphaDao.select();
    }

    // 开启事务管理，隔离级别：READ_COMMITTED；事务的传播机制：REQUIRED
    // 事务的传播机制：解决事务互相调用的交叉问题
    // 1. REQUIRED：支持当前事务（当前事务是调用者，不是被调用者）。如果外部事务不存在，就创建新事务（按照被调用者的来）
    // 2. REQUIRES_NEW：创建新事务放B，并暂停当前事务（外部事物）
    // 3. NESTED：如果当前存在事务（外部事物），则嵌套在该事务中执行（A调B，B有独立的提交和回滚）；否则，和REQUIRED一样

    // 声明式事务
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save01() {
        User user = new User();
        user.setUsername("ym");
        user.setSalt(CommunityUtils.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtils.md5("123" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("Hello");
        post.setContent("新人报道！");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        int age = 10 / 0;
        return "ok";
    }

    // 编程式事务：可以控制整个事务中的一小部分
    public Object save02() {
        // 设置隔离级别
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        // 设置传播方式
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        // 执行事务，要传播一个回调接口，泛型是期望这个接口返回的类型
        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            // 整个方法返回的值，会被transactionTemplate返回
            public Object doInTransaction(TransactionStatus status) {
                // 开始写执行逻辑
                User user = new User();
                user.setUsername("zcx");
                user.setSalt(CommunityUtils.generateUUID().substring(0, 5));
                user.setPassword(CommunityUtils.md5("123" + user.getSalt()));
                user.setEmail("zcx@qq.com");
                user.setHeaderUrl("http://image.nowcoder.com/head/999t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("你好");
                post.setContent("我是新人！");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                int age = 10 / 0;
                return "ok";
            }
        });
    }
}
