package com.ichilamlb.PushNConsumer.service;

import com.ichilamlb.PushNConsumer.models.PushNRequest;
import com.ichilamlb.PushNConsumer.models.SendPushNResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PushNService {

    public SendPushNResponse sendPushNotification(PushNRequest pushNRequest){
        return new SendPushNResponse(202,"Push N sent successfully");
    }
}
