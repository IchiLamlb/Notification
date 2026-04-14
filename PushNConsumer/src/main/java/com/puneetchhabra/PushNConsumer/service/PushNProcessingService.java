package com.puneetchhabra.PushNConsumer.service;

import com.puneetchhabra.PushNConsumer.models.PushNRequest;
import com.puneetchhabra.PushNConsumer.models.SendPushNResponse;
import com.puneetchhabra.PushNConsumer.models.db.DeliveryLog;
import com.puneetchhabra.PushNConsumer.models.db.Notification;
import com.puneetchhabra.PushNConsumer.models.enums.Channel;
import com.puneetchhabra.PushNConsumer.models.enums.Status;
import com.puneetchhabra.PushNConsumer.repo.DeliveryLogRepository;
import com.puneetchhabra.PushNConsumer.repo.NotificationRepository;
import com.puneetchhabra.PushNConsumer.service.exceptions.NotificationNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PushNProcessingService {
    PushNService pushNService;
    DeliveryLogRepository deliveryLogRepository;
    NotificationRepository notificationRepository;
    FailedNotificationsHandlerService failedNotificationsHandlerService;
    public PushNProcessingService(PushNService pushNService, DeliveryLogRepository deliveryLogRepository, FailedNotificationsHandlerService failedNotificationsHandlerService,NotificationRepository notificationRepository){
        this.pushNService = pushNService;
        this.notificationRepository = notificationRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.failedNotificationsHandlerService = failedNotificationsHandlerService;
    }

    /*
        Send push N to 3rd party vendors.
        Upon sending, create a delivery log with given notification id and
        update the notification status with notification id in Notifications Table.
     */
    public void processPushN(PushNRequest pushNRequest) {
        SendPushNResponse response = sendPushNToVendors(pushNRequest);

        try{
            if (response.getStatus() >= 200 && response.getStatus() < 300) {
                Notification notification = notificationRepository.findById(pushNRequest.getNotificationId())
                        .orElseThrow(() -> {
                            log.error("Notification with Id: " + pushNRequest.getNotificationId() + " Not found");
                            return new NotificationNotFoundException("Notification with Id: " + pushNRequest.getNotificationId() + " Not found");
                        });
                notification.setStatus(Status.sent);
                try{
                    notificationRepository.save(notification); //updated status
                    log.info("Status updated to SENT for Notification Id: " + pushNRequest.getNotificationId());
                } catch (Exception exception){
                    log.error("Exception while updating status of Notification for pushNRequest: {}", pushNRequest);
                    log.error("Exception: {}", exception.toString());
                }
                try{
                    deliveryLogRepository.save(new DeliveryLog(notification, Channel.push,Status.sent,""));
                } catch(Exception exception){
                    log.error("Exception while creating delivery log for Notification id {} for pushNRequest: {}",notification.getId(), pushNRequest);
                    log.error("Exception: {}", exception.toString());
                }
            } else {
                failedNotificationsHandlerService.handleFailedRequest(pushNRequest);
            }
        } catch (NotificationNotFoundException exception){
            log.error("Notification with Id: " + pushNRequest.getNotificationId() + " Not found while trying to update notification status/ creating delivery log");
        }

    }

    private SendPushNResponse sendPushNToVendors(PushNRequest pushNRequest) {
        SendPushNResponse sendPushNResponse =  pushNService.sendPushNotification(pushNRequest);

        if(sendPushNResponse.getStatus() >= 200 && sendPushNResponse.getStatus() <300){
            sendPushNResponse.setMessage("Push Notification Sent");
            log.info("PushNRequest {} sent successfully",pushNRequest.toString());
            return sendPushNResponse;
        } else {
            log.error("Failed to send PushNRequest {}. Message: {}",pushNRequest.toString(), sendPushNResponse.getMessage());
            sendPushNResponse.setMessage("Something went wrong with SendGrid.");
            return sendPushNResponse;
        }
    }
}
