package com.masterbank.masterbank.notifications.service;


import com.masterbank.masterbank.authUsers.entity.User;
import com.masterbank.masterbank.enums.NotificationType;
import com.masterbank.masterbank.notifications.dtos.NotificationDTO;
import com.masterbank.masterbank.notifications.entity.Notification;
import com.masterbank.masterbank.notifications.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Override
    @Async
    public void sendEmail(NotificationDTO notificationDTO, User user) {

        try{
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            mimeMessageHelper.setTo(notificationDTO.getRecipient());
            mimeMessageHelper.setSubject(notificationDTO.getSubject());

            if (notificationDTO.getTemplateName() != null) {
                Context context = new Context();
                context.setVariables(notificationDTO.getTemplateVariables());
                String htmlContent = templateEngine.process(notificationDTO.getTemplateName(), context);
                mimeMessageHelper.setText(htmlContent, true);
            } else {
                mimeMessageHelper.setText(notificationDTO.getBody(), true);
            }
            javaMailSender.send(mimeMessage);
            log.info("Sent email to ");

//            Notification notificationToSave = Notification.builder()
//                    .recipient(notificationDTO.getRecipient())
//                    .subject(notificationDTO.getSubject())
//                    .body(notificationDTO.getBody())
//                    .type(NotificationType.EMAIL)
//                    .user(user)
//                    .build();
//            notificationRepository.save(notificationToSave);

        }catch (MessagingException e){
            log.error(e.getMessage());
        }
    }
}
