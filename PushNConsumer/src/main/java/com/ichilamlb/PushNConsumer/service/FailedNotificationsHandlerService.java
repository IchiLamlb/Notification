package com.ichilamlb.PushNConsumer.service;

import com.ichilamlb.PushNConsumer.models.PushNRequest;
import org.springframework.stereotype.Service;

@Service
public class FailedNotificationsHandlerService {
    public void handleFailedRequest(PushNRequest pushNRequest){
        //implement retry strategy or logging for failed notifications
    }
}
