package com.puneetchhabra.EmailConsumer.service;

import com.puneetchhabra.EmailConsumer.models.EmailRequest;
import com.puneetchhabra.EmailConsumer.models.SendEmailResponse;
import com.puneetchhabra.EmailConsumer.models.db.DeliveryLog;
import com.puneetchhabra.EmailConsumer.models.db.Notification;
import com.puneetchhabra.EmailConsumer.models.enums.Channel;
import com.puneetchhabra.EmailConsumer.models.enums.Status;
import com.puneetchhabra.EmailConsumer.repo.DeliveryLogRepository;
import com.puneetchhabra.EmailConsumer.repo.NotificationRepository;
import com.puneetchhabra.EmailConsumer.service.exceptions.NotificationNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailProcessingService {
    EmailService emailService;
    DeliveryLogRepository deliveryLogRepository;
    NotificationRepository notificationRepository;
    FailedNotificationsHandlerService failedNotificationsHandlerService;
    public EmailProcessingService(EmailService emailService, DeliveryLogRepository deliveryLogRepository, NotificationRepository notificationRepository, FailedNotificationsHandlerService failedNotificationsHandlerService){
        this.emailService = emailService;
        this.notificationRepository = notificationRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.failedNotificationsHandlerService = failedNotificationsHandlerService;
    }

    /*
        Send email to 3rd party Email vendors.
        Upon sending, create a delivery log with given notification id and
        update the notification status with notification id in Notifications Table.
     */
    public void processEmail(EmailRequest emailRequest) {
        SendEmailResponse response = sendEmailToVendors(emailRequest);

        try{
            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                Notification notification = notificationRepository.findById(emailRequest.getNotificationId())
                        .orElseThrow(() -> {
                            log.error("Notification with Id: " + emailRequest.getNotificationId() + " Not found");
                            return new NotificationNotFoundException("Notification with Id: " + emailRequest.getNotificationId() + " Not found");
                        });
                notification.setStatus(Status.sent);
                try{
                    notificationRepository.save(notification); //updated status
                    log.info("Status updated to SENT for Notification Id: " + emailRequest.getNotificationId());
                } catch (Exception exception){
                    log.error("Exception while updating status of Notification for emailRequest: {}", emailRequest);
                    log.error("Exception: {}", exception.toString());
                }
                try{
                    deliveryLogRepository.save(new DeliveryLog(notification, Channel.email,Status.sent,""));
                } catch(Exception exception){
                    log.error("Exception while creating delivery log for Notification id {} for emailRequest: {}",notification.getId(), emailRequest);
                    log.error("Exception: {}", exception.toString());
                }
            } else {
                failedNotificationsHandlerService.handleFailedRequest(emailRequest);
            }
        } catch (NotificationNotFoundException exception){
            log.error("Notification with Id: " + emailRequest.getNotificationId() + " Not found while trying to update notification status/ creating delivery log");
        }

    }

    private SendEmailResponse sendEmailToVendors(EmailRequest emailRequest) {
        SendEmailResponse sendEmailResponse =  emailService.sendEmail(emailRequest);

        if(sendEmailResponse.getStatus() >= 200 && sendEmailResponse.getStatus() <300){
            sendEmailResponse.setMessage("Email Sent");
            log.info("EmailRequest {} sent successfully",emailRequest.toString());
            return sendEmailResponse;
        } else {
            log.error("Failed to send EmailRequest {}. Message: {}",emailRequest.toString(), sendEmailResponse.getMessage());
            sendEmailResponse.setMessage("Something went wrong with SendGrid.");
            return sendEmailResponse;
        }
    }
}
