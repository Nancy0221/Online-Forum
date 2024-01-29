package com.zcx.community;

import com.zcx.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Autowired
    // 负责格式化email
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail() {
        mailClient.sendMail("1364052596@qq.com", "TEST", "Welcome.");
    }

    @Test
    // 使用Thymeleaf模板引擎的邮件内容
    public void testHtmlMail() {
        Context context = new Context();
        // username = ym
        context.setVariable("username", "ym");
        // "/mail/demo": 模板的路径
        // context：数据本身
        String content = templateEngine.process("/mail/demo", context);
        mailClient.sendMail("1364052596@qq.com", "HTML", content);
    }
}
