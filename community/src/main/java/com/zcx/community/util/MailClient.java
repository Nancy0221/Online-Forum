package com.zcx.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

@Component
public class MailClient {

    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender javaMailSender;

    // 把这个值注入进from中
    @Value("${spring.mail.username}")
    private String from;

    public void sendMail(String to, String subject, String content) {
        // 此时mimeMailMessage是空的，需要用mimeMessageHelper来辅助构建它
        MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMailMessage);
        try {
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            // true：支持html文本（即使content是html的字符串也能识别），否则只会认为content是普通文本
            mimeMessageHelper.setText(content, true);
            javaMailSender.send(mimeMessageHelper.getMimeMessage());
        } catch (Exception e) {
            throw new RuntimeException("发送邮件失败，服务器发生异常", e);
        }
    }

}
