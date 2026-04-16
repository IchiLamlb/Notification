package com.ichilamlb.NotificationProcessorPriority3.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ichilamlb.NotificationProcessorPriority3.models.db.DeliveryLog;
import com.ichilamlb.NotificationProcessorPriority3.models.db.Notification;
import com.ichilamlb.NotificationProcessorPriority3.models.db.User;
import com.ichilamlb.NotificationProcessorPriority3.models.enums.Channel;
import com.ichilamlb.NotificationProcessorPriority3.models.enums.Status;
import com.ichilamlb.NotificationProcessorPriority3.models.requests.EmailRequest;
import com.ichilamlb.NotificationProcessorPriority3.models.requests.PushNRequest;
import com.ichilamlb.NotificationProcessorPriority3.models.requests.SmsRequest;
import com.ichilamlb.NotificationProcessorPriority3.repo.DeliveryLogRepository;
import com.ichilamlb.NotificationProcessorPriority3.repo.NotificationRepository;
import com.ichilamlb.NotificationProcessorPriority3.service.exceptions.DuplicateNotificationFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static com.ichilamlb.NotificationProcessorPriority3.constants.Constants.*;

@Service
@Slf4j
public class SendNotificationService {
    KafkaTemplate<String , String> kafkaTemplate;
    ObjectMapper objectMapper;
    NotificationRepository notificationRepository;
    DeliveryLogRepository deliveryLogRepository;
    NotificationHelperService notificationHelperService;



    public SendNotificationService(KafkaTemplate<String, String> kafkaTemplate,
                                   NotificationRepository notificationRepository, DeliveryLogRepository deliveryLogRepository,
                                   ObjectMapper objectMapper, NotificationHelperService notificationHelperService){
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.notificationRepository = notificationRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.notificationHelperService = notificationHelperService;


    }



    public void sendSmsRequest(SmsRequest smsRequest, User user) {
        Notification notification = null;
        try{
            notification = notificationRepository.save(new Notification(user, Channel.sms, smsRequest.getMessage(), objectMapper.writeValueAsString(smsRequest), notificationHelperService.getSmsHash(smsRequest, user.getId())));
            smsRequest.setNotificationId(notification.getId());
        } catch (JsonProcessingException e){
            log.error("Exception parsing requestContent to String: {}", e.toString());
        }
        catch (Exception e){
            if(e.toString().contains("Duplicate entry")){
                throw new DuplicateNotificationFoundException("Duplicate notification request. "+smsRequest.toString());
            } else {
                throw e;
            }
        }

        boolean isSmsAllowed = notificationHelperService.isNotificationAllowed_PreferenceCheck(user.getId(), Channel.sms);
        if(isSmsAllowed){
            try {
                log.info("Preference: SMS is allowed acc to preferences. UserId: {}, SmsRequest: {}",user.getId(),smsRequest);
                String notificationString = prepareMessage(smsRequest);
                kafkaTemplate.send(SMS_TOPIC, PRIORITY_KEY_FOR_PARTITIONS, notificationString);
                deliveryLogRepository.save(new DeliveryLog(notification, Channel.sms, Status.pending,"Scheduled to kafka"));
                log.info("SMS sent to kafka. Delivery Log updated. UserId: {}, SmsRequest: {}",user.getId(),smsRequest);
            } catch (JsonProcessingException e) {
                log.error("Error in parsing sms notification {} for forwarding to Kafka.\n {}", smsRequest.toString(), e.toString());
            } catch (Exception e) {
                log.error("Failed to forward sms notification {}, to Kafka: \n{}", smsRequest.toString(), e.toString());
            }
        } else{
            log.info("Preference: Not sending SMS as per user preferences. UserId: {}, SmsRequest: {}",user.getId(),smsRequest);
            deliveryLogRepository.save(new DeliveryLog(notification, Channel.sms, Status.failed,"Not sending notification as per user: "+user.getId()+" preferences"));
        }
    }

    public void sendPushNRequest(PushNRequest pushNRequest, User user) {
        Notification notification = null;
        try {
            // 1. Tạo nội dung tóm tắt để lưu vào DB (title + body)
            String summaryContent = pushNRequest.getTitle() + " " + pushNRequest.getBody();

            // 2. Chuyển object request thành JSON String để lưu vào cột content trong DB
            String jsonContent = objectMapper.writeValueAsString(pushNRequest);

            // 3. Lấy mã Hash để chống gửi trùng (Idempotency)
            String hashCode = notificationHelperService.getPushNHash(pushNRequest, user.getId());

            // 4. Lưu vào bảng notifications
            notification = notificationRepository.save(new Notification(
                    user,
                    Channel.push,
                    summaryContent,
                    jsonContent,
                    hashCode
            ));

            // 5. QUAN TRỌNG: Gán ID vừa sinh ra từ DB ngược lại cho request để gửi sang Kafka
            pushNRequest.setNotificationId(notification.getId());

            // 6. Gửi vào Kafka Topic dành cho Push
            // kafkaTemplate.send(Constants.PUSH_TOPIC, pushNRequest);
            log.info("Notification saved and ID assigned: {}", notification.getId());

        } catch (JsonProcessingException e) {
            log.error("Lỗi parse JSON: {}", e.getMessage());
        } catch (Exception e) {
            if (e.toString().contains("Duplicate entry")) {
                throw new DuplicateNotificationFoundException("Thông báo trùng lặp: " + pushNRequest.toString());
            } else {
                log.error("Lỗi hệ thống khi lưu Notification: {}", e.getMessage());
                throw e;
            }
        }

        boolean isPushNAllowed = notificationHelperService.isNotificationAllowed_PreferenceCheck(user.getId(), Channel.push);
        if(isPushNAllowed){
            try {
                log.info("Preference: PushN is allowed acc to preferences. UserId: {}, PushNRequest: {}",user.getId(),pushNRequest);
                String notificationString = prepareMessage(pushNRequest);
                kafkaTemplate.send(PUSH_N_TOPIC, PRIORITY_KEY_FOR_PARTITIONS, notificationString);
                deliveryLogRepository.save(new DeliveryLog(notification, Channel.push, Status.pending,"Scheduled to kafka"));
                log.info("Push Notification sent to kafka. Delivery log updated. UserId: {}, PushNRequest: {}",user.getId(),pushNRequest);
            } catch (JsonProcessingException e) {
                log.error("Error in parsing Push notification {} for forwarding to Kafka.\n {}", pushNRequest.toString(), e.toString());
            } catch (Exception e) {
                log.error("Failed to forward Push notification {}, to Kafka: \n{}", pushNRequest.toString(), e.toString());
            }
        } else {
            log.info("Preference: Not sending Push Notification as per user preferences. UserId: {}, PushNRequest: {}",user.getId(),pushNRequest);
            deliveryLogRepository.save(new DeliveryLog(notification, Channel.push, Status.failed,"Not sending notification as per user: "+user.getId()+" preferences"));
        }
    }

    public void sendEmailRequest(EmailRequest emailRequest, User user) {
        Notification notification = null;
        try{
            notification = notificationRepository.save(new Notification(user, Channel.email, "emailSubject: " + emailRequest.getEmailSubject() + " message: " + emailRequest.getMessage() + " attachments: " + Arrays.toString(emailRequest.getEmailAttachments())
                    , objectMapper.writeValueAsString(emailRequest), notificationHelperService.getEmailHash(emailRequest, user.getId())));
            emailRequest.setNotificationId(notification.getId());
        } catch (JsonProcessingException e){
            log.error("Exception parsing requestContent to String: {}", e.toString());
        } catch (Exception e){
            if(e.toString().contains("Duplicate entry")){
                throw new DuplicateNotificationFoundException("Duplicate notification request. "+emailRequest.toString());
            } else {
                throw e;
            }
        }

        boolean isEmailAllowed = notificationHelperService.isNotificationAllowed_PreferenceCheck(user.getId(), Channel.email);
        if(isEmailAllowed){
            try {
                log.info("Preference: Email is allowed acc to preferences. UserId: {}, EmailRequest: {}",user.getId(),emailRequest);
                String notificationString = prepareMessage(emailRequest);
                kafkaTemplate.send(EMAIL_TOPIC, PRIORITY_KEY_FOR_PARTITIONS, notificationString);
                deliveryLogRepository.save(new DeliveryLog(notification, Channel.email, Status.pending,"Scheduled to kafka"));
                log.info("Email is sent to kafka. Delivery Log updated. UserId: {}, EmailRequest: {}",user.getId(),emailRequest);
            } catch (JsonProcessingException e) {
                log.error("Error in parsing Email notification {} for forwarding to Kafka.\n {}", emailRequest.toString(), e.toString());
            } catch (Exception e) {
                log.error("Failed to forward Email notification {}, to Kafka: \n{}", emailRequest.toString(), e.toString());
            }
        } else {
            log.info("Preference: Not sending Email Notification as per user preferences. UserId: {}, EmailRequest: {}",user.getId(),emailRequest);
            deliveryLogRepository.save(new DeliveryLog(notification, Channel.email, Status.failed,"Not sending notification as per user: "+user.getId()+" preferences"));
        }
    }



    private <T> String  prepareMessage(T request) throws JsonProcessingException {
        //Need to send Request as String data to kafka

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(request);
    }


}
