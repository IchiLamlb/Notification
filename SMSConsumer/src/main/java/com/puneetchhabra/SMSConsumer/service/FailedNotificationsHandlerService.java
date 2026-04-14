package com.puneetchhabra.SMSConsumer.service;

import com.puneetchhabra.SMSConsumer.models.SmsRequest;
import org.springframework.stereotype.Service;

@Service
public class FailedNotificationsHandlerService {
    public void handleFailedRequest(SmsRequest smsRequest){
        //implement retry strategy or logging for failed notifications
    }
}
