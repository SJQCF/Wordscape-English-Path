package com.sjqcf.service.impl;

import com.sjqcf.service.SendEmailService;
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
public class SendEmailServiceImpl implements SendEmailService {

    @Autowired
    JavaMailSender mailSender;

    @Override
    //ToEmail 接收验证码的邮箱
    public void sendCode(String ToEmail, String code) {

        // 创建一个MimeMessagePreparator对象,用于发送邮件
        MimeMessagePreparator preparator = new MimeMessagePreparator() {

            // 创建一个MimeMessage对象,用于设置邮件内容
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                mimeMessage.setRecipient(Message.RecipientType.TO,
                        new InternetAddress(ToEmail));
                mimeMessage.setFrom(new InternetAddress("19381027768@163.com"));
                mimeMessage.setText("您的验证码为：" + code + "，5分钟内有效");
            }
        };

        try {
            mailSender.send(preparator);
        }
        catch (MailException ex) {
            // simply log it and go on...
            System.err.println(ex.getMessage());
        }
    }
}
