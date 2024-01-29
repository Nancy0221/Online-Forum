package com.zcx.community.service;

import com.zcx.community.dao.UserMapper;
import com.zcx.community.entity.LoginTicket;
import com.zcx.community.entity.User;
import com.zcx.community.util.CommunityConstants;
import com.zcx.community.util.CommunityUtils;
import com.zcx.community.util.MailClient;
import com.zcx.community.util.RedisKeyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstants {

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    // 项目的路径名
    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    public Map<String, Object> register(User user) {

        Map<String, Object> map = new HashMap<>();

        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }
        // 验证账号
        User user1 = userMapper.selectByName(user.getUsername());
        if (user1 != null) {
            map.put("usernameMsg", "账号已存在");
            return map;
        }
        // 验证邮箱
        User user2 = userMapper.selectByEmail(user.getEmail());
        if (user2 != null) {
            map.put("emailMsg", "该邮箱已被注册");
            return map;
        }
        // 注册用户
        // 生成salt，取5位
        user.setSalt(CommunityUtils.generateUUID().substring(0, 5));
        // 加密密码
        user.setPassword(CommunityUtils.md5(user.getPassword() + user.getSalt()));
        // 默认普通用户
        user.setType(0);
        // 没有激活
        user.setStatus(0);
        // 激活码：用工具随机生成一个
        user.setActivationCode(CommunityUtils.generateUUID());
        // 随机头像
        // %d：占位符，这里是个数字，用随机生成的数字替代 [0, 1000)
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        // 注册时间
        user.setCreateTime(new Date());
        // 添加到数据库里
        userMapper.insertUser(user);

        // 创建激活邮件
        Context context = new Context();
        // email = user.getEmail()
        context.setVariable("email", user.getEmail());
        // 用户的激活路径：http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        // 把封装好的context传给模板，获得封装好的html邮件
        String content = templateEngine.process("/mail/activation", context);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 发送邮件
                mailClient.sendMail(user.getEmail(), "账号激活", content);
            }
        });
        thread.start();

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.currentThread().sleep(1000 * 60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                User user3 = userMapper.selectById(user.getId());
                if (user3 !=null && user3.getStatus() != 1) {
                    userMapper.deleteById(user3.getId());
                    clearCache(user3.getId());
                }
            }
        });
        thread1.start();

        // 如果map是空的，就代表注册过程中没有问题
        return map;
    }

    // 返回激活的状态
    public int activation(int userId, String code) {
        // 拿到user
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            return ACTIVATION_TIMEOUT;
        }
        // 已经是1了，说明重复激活了
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)){
            // status除了0就是1，更新该用户的status
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        // 验证账号是否存在
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在");
            return map;
        }
        // 验证状态：存在的话，是否已激活？
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活");
            return map;
        }
        // 验证密码
        password = CommunityUtils.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码错误");
            return map;
        }
        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtils.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey = RedisKeyUtils.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisKeyUtils.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    public Map<String, Object> getCode(String email) {
        HashMap<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(email)) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        User user = userMapper.selectByEmail(email);
        if (user == null) {
            map.put("emailMsg", "该邮箱没有绑定账号");
            return map;
        }

        String code = CommunityUtils.generateUUID().substring(0, 4);
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        context.setVariable("code", code);
        String content = templateEngine.process("/mail/forget", context);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mailClient.sendMail(user.getEmail(), "忘记密码", content);
            }
        });
        thread.start();

        map.put("code", code);
        return map;
    }

    public boolean resetPassword(String email, String password) {
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            return false;
        }
        userMapper.updatePassword(user.getId(), CommunityUtils.md5(password + user.getSalt()));
        clearCache(user.getId());
        return true;
    }

    public LoginTicket findLoginTicket(String ticket) {
//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtils.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

//    public int updatePreviousLoginTicket(String ticket, int userId) {
//        return loginTicketMapper.updatePreviousLoginTicket(ticket, userId);
//    }

    public int updateHeader(int userId, String headerUrl) {
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
//        return userMapper.updateHeader(userId, headerUrl);
    }

    public Map<String, Object> updatePassword(User user, String oldPassword, String newPassword, String confirmPassword) {
        Map<String, Object> map = new HashMap<>();

        if (user == null) {
            map.put("oldPasswordMsg", "未登录，无法修改密码");
            return map;
        }

        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "原密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空");
            return map;
        }
        if (!newPassword.equals(confirmPassword)) {
            map.put("confirmPasswordMsg", "确认密码不一致");
            return map;
        }
        oldPassword = CommunityUtils.md5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)) {
            map.put("oldPasswordMsg", "原密码输入有误");
            return map;
        }
        newPassword = CommunityUtils.md5(newPassword + user.getSalt());
        userMapper.updatePassword(user.getId(), newPassword);
        clearCache(user.getId());
        return map;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    private User getCache(int userId) {
        String userKey = RedisKeyUtils.getUserKey(userId);
        User user = (User) redisTemplate.opsForValue().get(userKey);
        return user;
    }

    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtils.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    private void clearCache(int userId) {
        String userKey = RedisKeyUtils.getUserKey(userId);
        redisTemplate.delete(userKey);
    }
}
