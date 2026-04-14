package com.puneetchhabra.SMSConsumer.service;

import com.puneetchhabra.SMSConsumer.models.SendSmsResponse;
import com.puneetchhabra.SMSConsumer.models.SmsRequest;
import com.puneetchhabra.SMSConsumer.models.db.DeliveryLog;
import com.puneetchhabra.SMSConsumer.models.db.Notification;
import com.puneetchhabra.SMSConsumer.models.enums.Channel;
import com.puneetchhabra.SMSConsumer.models.enums.Status;
import com.puneetchhabra.SMSConsumer.repo.DeliveryLogRepository;
import com.puneetchhabra.SMSConsumer.repo.NotificationRepository;
import com.puneetchhabra.SMSConsumer.service.exceptions.NotificationNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsProcessingService {
    SmsService smsService;
    DeliveryLogRepository deliveryLogRepository;
    NotificationRepository notificationRepository;
    FailedNotificationsHandlerService failedNotificationsHandlerService;
    public SmsProcessingService(SmsService smsService, FailedNotificationsHandlerService failedNotificationsHandlerService,DeliveryLogRepository deliveryLogRepository, NotificationRepository notificationRepository){
        this.smsService = smsService;
        this.notificationRepository = notificationRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.failedNotificationsHandlerService = failedNotificationsHandlerService;
    }

    /*
        Send sms to 3rd party vendors.
        Upon sending, create a delivery log with given notification id and
        update the notification status with notification id in Notifications Table.
     */
    public void processSms(SmsRequest smsRequest) {
        SendSmsResponse response = sendSmsToVendors(smsRequest);

        try{
            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                Notification notification = notificationRepository.findById(smsRequest.getNotificationId())
                        .orElseThrow(() -> {
                            log.error("Notification with Id: " + smsRequest.getNotificationId() + " Not found");
                            return new NotificationNotFoundException("Notification with Id: " + smsRequest.getNotificationId() + " Not found");
                        });
                notification.setStatus(Status.sent);
                try{
                    notificationRepository.save(notification); //updated status
                    log.info("Status updated to SENT for Notification Id: " + smsRequest.getNotificationId());
                } catch (Exception exception){
                    log.error("Exception while updating status of Notification for smsRequest: {}", smsRequest);
                    log.error("Exception: {}", exception.toString());
                }
                try{
                    deliveryLogRepository.save(new DeliveryLog(notification, Channel.sms,Status.sent,""));
                } catch(Exception exception){
                    log.error("Exception while creating delivery log for Notification id {} for smsRequest: {}",notification.getId(), smsRequest);
                    log.error("Exception: {}", exception.toString());
                }
            }
        } catch (NotificationNotFoundException exception){
            log.error("Notification with Id: " + smsRequest.getNotificationId() + " Not found while trying to update notification status/ creating delivery log");
        }

    }

    private SendSmsResponse sendSmsToVendors(SmsRequest smsRequest) {
        SendSmsResponse sendSmsResponse =  smsService.sendSms(smsRequest);

        if(sendSmsResponse.getStatus() >= 200 && sendSmsResponse.getStatus() <300){
            sendSmsResponse.setMessage("Sms Sent");
            log.info("SmsRequest {} sent successfully",smsRequest.toString());
            return sendSmsResponse;
        } else {
            log.error("Failed to send SmsRequest {}. Message: {}",smsRequest.toString(), sendSmsResponse.getMessage());
            sendSmsResponse.setMessage("Something went wrong with SendGrid.");
            return sendSmsResponse;
        }
    }
}
