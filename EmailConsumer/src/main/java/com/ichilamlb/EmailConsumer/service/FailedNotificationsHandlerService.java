package com.ichilamlb.EmailConsumer.service;

import com.ichilamlb.EmailConsumer.models.EmailRequest;
import org.springframework.stereotype.Service;

@Service
public class FailedNotificationsHandlerService {
    public void handleFailedRequest(EmailRequest emailRequest){
        //implement retry strategy or logging for failed notifications
    }
}
