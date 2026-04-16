package com.ichilamlb.NotificationProcessorPriority1.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ichilamlb.NotificationProcessorPriority1.models.Content;
import com.ichilamlb.NotificationProcessorPriority1.models.NotificationRequest;
import com.ichilamlb.NotificationProcessorPriority1.models.PushNotification;
import com.ichilamlb.NotificationProcessorPriority1.models.enums.Channel;
import com.ichilamlb.NotificationProcessorPriority1.models.requests.EmailRequest;
import com.ichilamlb.NotificationProcessorPriority1.models.requests.PushNRequest;
import com.ichilamlb.NotificationProcessorPriority1.models.requests.SmsRequest;
import com.ichilamlb.NotificationProcessorPriority1.models.db.Template;
import com.ichilamlb.NotificationProcessorPriority1.models.db.User;
import com.ichilamlb.NotificationProcessorPriority1.repo.TemplateRepository;
import com.ichilamlb.NotificationProcessorPriority1.repo.UserRepository;
import com.ichilamlb.NotificationProcessorPriority1.service.exceptions.DuplicateNotificationFoundException;
import com.ichilamlb.NotificationProcessorPriority1.service.exceptions.TemplateNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.ArrayList;
import java.util.Map;

@Service
@Slf4j
public class NotificationProcessingService {
    ObjectMapper objectMapper;
    TemplateRepository templateRepository;
    UserRepository userRepository;
    SendNotificationService sendNotificationService;

    public NotificationProcessingService(ObjectMapper objectMapper,TemplateRepository templateRepository, UserRepository userRepository, SendNotificationService sendNotificationService){
        this.objectMapper = objectMapper;
        this.templateRepository = templateRepository;
        this.userRepository = userRepository;
        this.sendNotificationService = sendNotificationService;
    }

    public void processNotification(NotificationRequest notificationRequest) {

        if (notificationRequest.getContent().isUsingTemplates()){
            prepareMessageFromTemplate(notificationRequest);
        }

        //Channel validation done at Notification Service
        ArrayList<Channel> channels = getChannels(notificationRequest.getChannels());
        Long userId = Long.parseLong(notificationRequest.getRecipient().getUserId());
        try {
            //Get user from DB
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("User with userId: " + userId + " Not found");
                        return new UserPrincipalNotFoundException("User with userId: " + userId + " Not found");
                    });

            if(channels.contains(Channel.email)){
                try {
                    prepareAndSendEmailNotification(notificationRequest, user.getEmail(), user);
                } catch (Exception exception){
                    log.error("Unexpected Exception while processing Email Notification Request: {}", notificationRequest);
                    log.error("Exception: {}", exception.toString());
                }
            }
            if(channels.contains(Channel.sms)){
                try{
                    prepareAndSendSMSNotification(notificationRequest.getContent().getMessage(),user.getPhone(),user);
                }catch (Exception exception){
                    log.error("Unexpected Exception while processing SMS Notification Request: {}", notificationRequest);
                    log.error("Exception: {}", exception.toString());
                }

            }
            if(channels.contains(Channel.push)){
                try{
                    prepareAndSendPushNotification(notificationRequest,user);
                }catch (Exception exception){
                    log.error("Unexpected Exception while processing Push Notification Request: {}", notificationRequest);
                    log.error("Exception: {}", exception.toString());
                }
            }
        } catch (UserPrincipalNotFoundException e) {
            log.error("User with userId: " + userId + " Not found for Notification Request: "+notificationRequest);
        }
    }

    private void prepareMessageFromTemplate(NotificationRequest notificationRequest) {
        String templateName = notificationRequest.getContent().getTemplateName();
        try {
            Template usedTemplate = templateRepository.findByName(templateName)
                    .orElseThrow(() -> new TemplateNotFoundException("Template: " + templateName + " Not found"));

            Map<String, String> placeholdersInRequest = notificationRequest.getContent().getPlaceholders();
            // Giả sử placeholders trong DB lưu dạng ["name", "otp"]
            String[] requiredPlaceholders = objectMapper.readValue(usedTemplate.getPlaceholders(), String[].class);

            // 1. Cập nhật Message (Nội dung)
            String updatedMessage = replacePlaceholdersInMessageContent(usedTemplate.getContent(), placeholdersInRequest, requiredPlaceholders);
            notificationRequest.getContent().setMessage(updatedMessage);

            // 2. CẬP NHẬT TIÊU ĐỀ (SUBJECT) - Quan trọng để không bị null tiêu đề
            if (usedTemplate.getSubject() != null) {
                String updatedSubject = replacePlaceholdersInMessageContent(usedTemplate.getSubject(), placeholdersInRequest, requiredPlaceholders);
                notificationRequest.getContent().setEmailSubject(updatedSubject);
            }

        } catch (Exception e) {
            log.error("Lỗi khi chuẩn bị message từ template: {}", e.getMessage());
        }
    }

    private String replacePlaceholdersInMessageContent(String content, Map<String, String> placeholdersInRequest, String[] requiredPlaceholders) {
        if (content == null) return "";
        for (String s : requiredPlaceholders) {
            String value = placeholdersInRequest.getOrDefault(s, "");
            // Sửa lại để hỗ trợ cả {name} và ${name} cho chắc chắn
            content = content.replace("{" + s + "}", value);
            content = content.replace("${" + s + "}", value);
        }
        return content;
    }

    // Trong module NotificationProcessorPriority1
    private void prepareAndSendEmailNotification(NotificationRequest notificationRequest, String email, User user) {
        Content content = notificationRequest.getContent();

        // Mapping dữ liệu sang EmailRequest để gửi cho EmailConsumer
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setEmailId(email); // Lấy email từ đối tượng User trong DB
        emailRequest.setMessage(content.getMessage());

        // Xử lý tiêu đề: Nếu content chưa có subject, lấy từ DB Template hoặc để mặc định
        String subject = content.getEmailSubject();
        if (subject == null || subject.isEmpty()) {
            subject = "Welcome to Our Service"; // Hoặc lấy từ Template
        }
        emailRequest.setEmailSubject(subject);

        // Gán ID để đồng bộ
        emailRequest.setNotificationId(notificationRequest.getNotificationId());

        try {
            sendNotificationService.sendEmailRequest(emailRequest, user);
            log.info("Đã chuyển tiếp thông báo ID {} tới Kafka Email Topic", notificationRequest.getNotificationId());
        } catch (DuplicateNotificationFoundException e) {
            log.error("Thông báo trùng lặp: {}", e.getMessage());
        }
    }

    private void prepareAndSendPushNotification(NotificationRequest notificationRequest, User user) {
        Content content = notificationRequest.getContent();
        PushNotification pushContent = content.getPushNotification();

        // 1. Xử lý an toàn Title và Body (Lấy từ Template nếu PushNotification null)
        String title = (pushContent != null && pushContent.getTitle() != null)
                ? pushContent.getTitle() : "Thông báo mới";
        String body = content.getMessage(); // Message đã được render từ template ở hàm trên

        // 2. Xử lý an toàn Action URL
        String actionUrl = "";
        if (pushContent != null && pushContent.getAction() != null) {
            actionUrl = pushContent.getAction().getUrl();
        }

        // 3. Khởi tạo Request và GÁN ĐẦY ĐỦ CÁC TRƯỜNG
        PushNRequest pushNRequest = new PushNRequest();
        pushNRequest.setNotificationId(notificationRequest.getNotificationId()); // Sửa lỗi ID null
        pushNRequest.setRecipientToken(notificationRequest.getRecipient().getFcmToken()); // Gửi token sang Consumer
        pushNRequest.setTitle(title);
        pushNRequest.setBody(body);
        // Nếu PushNRequest của bạn có trường actionUrl, hãy gán nó:
        // pushNRequest.setActionUrl(actionUrl);

        try {
            log.info("Đang chuyển tiếp Push Notification ID: {} tới Kafka", notificationRequest.getNotificationId());
            sendNotificationService.sendPushNRequest(pushNRequest, user);
        } catch (DuplicateNotificationFoundException e) {
            log.error("Thông báo Push bị trùng lặp (Idempotency chặn): {}", e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi không xác định khi gửi Push: {}", e.getMessage());
        }
    }

    private void prepareAndSendSMSNotification(String message, String phone, User user) {
        SmsRequest smsRequest = new SmsRequest(phone,message);
        try{
            sendNotificationService.sendSmsRequest(smsRequest, user);
        }catch (DuplicateNotificationFoundException duplicateNotificationFoundException){
            log.error("Duplicate SMS Request. "+duplicateNotificationFoundException.toString());
        }
    }


    private ArrayList<Channel> getChannels(String[] channels) {
        ArrayList<Channel> channelList = new ArrayList<>();
        for (String s: channels){
            if(s.equals("email")){
                channelList.add(Channel.email);
            } else if(s.equals("sms")){
                channelList.add(Channel.sms);
            } else if(s.equals("push")){
                channelList.add(Channel.push);
            }
        }
        return channelList;
    }
}
